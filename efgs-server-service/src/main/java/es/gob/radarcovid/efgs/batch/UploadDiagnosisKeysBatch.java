/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.batch;

import es.gob.radarcovid.efgs.business.UploadExposedService;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao;
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "application.efgs.upload-diagnosis-keys.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
@Slf4j
public class UploadDiagnosisKeysBatch {

    private static final String UPLOAD_JOB_NAME = "uploadDiagnosisKeys";

    private final BatchJobExecutionDao batchJobExecutionDao;
    private final UploadExposedService uploadExposedService;

    @Scheduled(cron = "${application.efgs.upload-diagnosis-keys.batching.cron}", zone = "Europe/Madrid")
    @SchedulerLock(name = "uploadDiagnosisKeys", lockAtLeastFor = "PT30S",
            lockAtMostFor = "${application.efgs.upload-diagnosis-keys.batching.lock-limit}")
    public void efgsUploadDiagnosisKeysScheduledTask() {
        MDC.put(Constants.TRACKING, "UPLOAD_DIAGNOSIS_KEYS");
        log.info("UploadDiagnosisKeysBatch process started");

        BatchJobExecutionDto batchJobExecutionDto = batchJobExecutionDao.startBatchJob(UPLOAD_JOB_NAME);
        BatchJobStatusEnum status = BatchJobStatusEnum.COMPLETED;

        try {
            uploadExposedService.uploadDiagnosisKeys(batchJobExecutionDto);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            status = BatchJobStatusEnum.FAILED;
            batchJobExecutionDto.setMessage(e.getMessage());
        } finally {
            batchJobExecutionDto.setStatus(status);
            batchJobExecutionDao.endBatchJob(batchJobExecutionDto);
        }

        log.info("UploadDiagnosisKeysBatch Process finished");
    }

}
