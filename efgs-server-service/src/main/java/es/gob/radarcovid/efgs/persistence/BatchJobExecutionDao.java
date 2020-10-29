/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence;

import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

public interface BatchJobExecutionDao {
	
    @Transactional
    BatchJobExecutionDto startBatchJob(String job);
    
    @Transactional
    boolean endBatchJob(BatchJobExecutionDto dto);
    
    @Transactional
    int deleteAllBefore(LocalDateTime date);

}
