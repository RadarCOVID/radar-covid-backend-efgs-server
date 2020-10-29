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

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import es.gob.radarcovid.efgs.persistence.DownloadExecutionDao;
import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity;
import es.gob.radarcovid.efgs.persistence.entity.DownloadExecutionEntity;
import es.gob.radarcovid.efgs.persistence.mapper.DownloadExecutionMapper;
import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto;
import es.gob.radarcovid.efgs.persistence.repository.DownloadExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadExecutionDaoImpl implements DownloadExecutionDao {

	private final DownloadExecutionRepository repository;
	private final DownloadExecutionMapper mapper;

	@Override
	public Optional<DownloadExecutionDto> findLastDownloadSuccess() {
		return repository.findFirstBySuccessOrderByDownloadDateDescCreatedAtDesc(true).map(mapper::entityToDto);
	}

	@Override
	public boolean saveDownload(Long batchJobExecutionId, DownloadExecutionDto downloadExecutionDto) {
		BatchJobExecutionEntity batchJobEntity = new BatchJobExecutionEntity();
		batchJobEntity.setId(batchJobExecutionId);

		DownloadExecutionEntity entity = mapper.dtoToEntity(downloadExecutionDto);
		entity.setBatchJobExecution(batchJobEntity);
		entity.setCreatedAt(LocalDateTime.now());
		repository.save(entity);
		return true;
	}

}
