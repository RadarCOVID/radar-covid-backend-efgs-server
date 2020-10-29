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

import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao;
import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity;
import es.gob.radarcovid.efgs.persistence.mapper.BatchJobExecutionMapper;
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;
import es.gob.radarcovid.efgs.persistence.repository.BatchJobExecutionRepository;
import es.gob.radarcovid.efgs.persistence.repository.DownloadExecutionRepository;
import es.gob.radarcovid.efgs.persistence.repository.UploadKeysExecutionRepository;
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchJobExecutionDaoImpl implements BatchJobExecutionDao {

    private final BatchJobExecutionMapper mapper;
    private final BatchJobExecutionRepository repository;
    private final DownloadExecutionRepository downloadExecutionRepository;
    private final UploadKeysExecutionRepository uploadKeysExecutionRepository;

    @Override
    public BatchJobExecutionDto startBatchJob(String job) {
        BatchJobExecutionEntity entity = new BatchJobExecutionEntity();
        entity.setJobName(job);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(BatchJobStatusEnum.STARTED);
        return mapper.entityToDto(repository.save(entity));
    }

    @Override
    public boolean endBatchJob(BatchJobExecutionDto dto) {
        repository.endBatchJobExecutionById(dto.getId(), LocalDateTime.now(),
                                            StringUtils.abbreviate(dto.getMessage(),
                                                                   BatchJobExecutionEntity.MESSAGE_MAX_LENGTH),
                                            dto.getStatus());
        return true;
    }

    @Override
    public int deleteAllBefore(LocalDateTime date) {
        downloadExecutionRepository.deleteByBatchJobExecutionStartTime(date);
        uploadKeysExecutionRepository.deleteByBatchJobExecutionStartTime(date);
        return repository.deleteByStartTimeBefore(date);
    }

}
