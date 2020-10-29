/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.impl;

import es.gob.radarcovid.efgs.persistence.UploadKeysExecutionDao;
import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity;
import es.gob.radarcovid.efgs.persistence.entity.UploadKeysExecutionEntity;
import es.gob.radarcovid.efgs.persistence.mapper.UploadKeysExecutionMapper;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysExecutionDto;
import es.gob.radarcovid.efgs.persistence.repository.UploadKeysExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadKeysExecutionDaoImpl implements UploadKeysExecutionDao {

    private final UploadKeysExecutionRepository repository;
    private final UploadKeysExecutionMapper mapper;

    @Override
    public boolean saveUploadKeys(Long batchJobExecutionId, UploadKeysExecutionDto uploadKeysExecutionDto) {
        BatchJobExecutionEntity batchJobExecutionEntity = new BatchJobExecutionEntity();
        batchJobExecutionEntity.setId(batchJobExecutionId);

        UploadKeysExecutionEntity entity = mapper.dtoToEntity(uploadKeysExecutionDto);
        entity.setBatchJobExecution(batchJobExecutionEntity);
        repository.save(entity);
        return true;
    }
}
