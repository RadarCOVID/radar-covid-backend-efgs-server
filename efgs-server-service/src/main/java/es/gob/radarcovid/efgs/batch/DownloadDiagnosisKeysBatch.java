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

import es.gob.radarcovid.efgs.business.DownloadExposedService;
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

@ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
@Slf4j
public class DownloadDiagnosisKeysBatch {

	private static final String DOWNLOAD_JOB_NAME = "downloadDiagnosisKeys";

	private final DownloadExposedService downloadExposedService;
	private final BatchJobExecutionDao batchJobExecutionDao;

    @Scheduled(cron = "${application.efgs.download-diagnosis-keys.batching.cron}", zone = "Europe/Madrid")
	@SchedulerLock(name = DOWNLOAD_JOB_NAME, lockAtLeastFor = "PT30S", 
		lockAtMostFor = "${application.efgs.download-diagnosis-keys.batching.lock-limit}")
	public void efgsDownloadDiagnosisKeysScheduledTask() {
		MDC.put(Constants.TRACKING, "DOWNLOAD_DIAGNOSIS_KEYS");
		log.info("DownloadDiagnosisKeysBatch process started");
		
		BatchJobExecutionDto jobExecution = batchJobExecutionDao.startBatchJob(DOWNLOAD_JOB_NAME);
		BatchJobStatusEnum status = BatchJobStatusEnum.COMPLETED;
		try {
			downloadExposedService.downloadDiagnosisKeys(jobExecution);
		} catch (Exception e) {
			log.error("Exception: {}", e.getMessage(), e);
			status = BatchJobStatusEnum.FAILED;
			jobExecution.setMessage(e.getMessage());
		} finally {
			jobExecution.setStatus(status);
			batchJobExecutionDao.endBatchJob(jobExecution);
		}
		
		log.info("DownloadDiagnosisKeysBatch process finished");
	}

}
