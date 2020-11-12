/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.runner;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import es.gob.radarcovid.efgs.business.DownloadExposedService;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao;
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.enabled", havingValue = "true")
@Component
@Order(2)
@AllArgsConstructor
@Slf4j
public class DownloadDiagnosisKeysRunner extends EfgsRunner {
	
	private static final String DOWNLOAD_JOB_NAME = "downloadDiagnosisKeys";

	private final DownloadExposedService downloadExposedService;
	private final BatchJobExecutionDao batchJobExecutionDao;
	
	@Override
	public String jobName() {
		return DOWNLOAD_JOB_NAME;
	}

	@Override
	public void run() {
		MDC.put(Constants.TRACKING, "DOWNLOAD_DIAGNOSIS_KEYS");
		log.info("DownloadDiagnosisKeysRunner started");

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

		log.info("DownloadDiagnosisKeysRunner finished");
		MDC.remove(Constants.TRACKING);
	}

}
