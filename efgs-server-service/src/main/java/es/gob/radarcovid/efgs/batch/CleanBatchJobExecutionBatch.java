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

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.gob.radarcovid.efgs.business.CleanBatchJobService;
import es.gob.radarcovid.efgs.etc.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@ConditionalOnProperty(name = "application.efgs.clean-batch-job.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanBatchJobExecutionBatch {

	private static final String CLEAN_BATCH_JOB_NAME = "cleanBatchJobExecution";

	private final CleanBatchJobService cleanBatchJobService;

    @Scheduled(cron = "${application.efgs.clean-batch-job.batching.cron}", zone = "Europe/Madrid")
	@SchedulerLock(name = CLEAN_BATCH_JOB_NAME, lockAtLeastFor = "PT30S", 
		lockAtMostFor = "${application.efgs.clean-batch-job.batching.lock-limit}")
	public void efgsCleanBatchJobScheduledTask() {
    	MDC.put(Constants.TRACKING, "CLEAN_BATCH_JOB");
		log.info("CleanBatchJobBatch process started");
		cleanBatchJobService.cleanBatchJobExecution();		
		log.info("CleanBatchJobBatch process finished");
	}

}
