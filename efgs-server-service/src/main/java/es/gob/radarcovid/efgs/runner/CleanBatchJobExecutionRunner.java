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

import es.gob.radarcovid.efgs.business.CleanBatchJobService;
import es.gob.radarcovid.efgs.etc.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(name = "application.efgs.clean-batch-job.enabled", havingValue = "true")
@Component
@Order(3)
@AllArgsConstructor
@Slf4j
public class CleanBatchJobExecutionRunner extends EfgsRunner {
	
	private static final String CLEAN_BATCH_JOB_NAME = "cleanBatchJobExecution";
	
	private final CleanBatchJobService cleanBatchJobService;
	
	@Override
	public String jobName() {
		return CLEAN_BATCH_JOB_NAME;
	}

	@Override
	public void run() {
    	MDC.put(Constants.TRACKING, "CLEAN_BATCH_JOB");
		log.info("CleanBatchJobExecutionRunner started");
		cleanBatchJobService.cleanBatchJobExecution();		
		log.info("CleanBatchJobExecutionRunner finished");
		MDC.remove(Constants.TRACKING);
	}

}
