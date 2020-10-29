/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.business.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import es.gob.radarcovid.common.annotation.Loggable;
import es.gob.radarcovid.efgs.business.CleanBatchJobService;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanBatchJobServiceImpl implements CleanBatchJobService {

	private final EfgsProperties efgsProperties;
	private final BatchJobExecutionDao batchJobExecutionDao;

	@Loggable
	@Override
	public void cleanBatchJobExecution() {
		
		log.debug("Executing scheduler task cleanBatchJobServiceImpl.cleanBatchJobExecution");
		
		LocalDateTime date = LocalDateTime.now().minusMonths(efgsProperties.getCleanBatchJob().getRetentionMonths());
		int removed = batchJobExecutionDao.deleteAllBefore(date);
		
		log.debug("Leaving cleanBatchJobServiceImpl.cleanBatchJobExecution. Removed ({})", removed);
				
	}

}
