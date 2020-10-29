/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import es.gob.radarcovid.efgs.persistence.entity.DownloadExecutionEntity;
import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto;

@Mapper(componentModel = "spring")
public abstract class DownloadExecutionMapper {
	
	@Mapping(source = "batchJobExecution.id", target = "batchId")
    public abstract DownloadExecutionDto entityToDto(DownloadExecutionEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "batchId", target = "batchJobExecution.id"),
    })
    public abstract DownloadExecutionEntity dtoToEntity(DownloadExecutionDto dto);

    @AfterMapping
    protected void setInternalValues(DownloadExecutionDto dto, @MappingTarget DownloadExecutionEntity entity) {
        String message = StringUtils.abbreviate(dto.getMessage(), DownloadExecutionEntity.MESSAGE_MAX_LENGTH);
        entity.setMessage(message);
    }
}
