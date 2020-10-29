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

import es.gob.radarcovid.efgs.persistence.entity.UploadKeysExecutionEntity;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysExecutionDto;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class UploadKeysExecutionMapper {

    @Mapping(source = "batchJobExecution.id", target = "batchId")
    public abstract UploadKeysExecutionDto entityToDto(UploadKeysExecutionEntity entity);

    @Mappings({
            @Mapping(source = "batchId", target = "batchJobExecution.id"),
            @Mapping(target = "id", ignore = true)
    })
    public abstract UploadKeysExecutionEntity dtoToEntity(UploadKeysExecutionDto dto);

    @AfterMapping
    protected void setInternalValues(UploadKeysExecutionDto dto, @MappingTarget UploadKeysExecutionEntity entity) {
        String message = StringUtils.abbreviate(dto.getMessage(), UploadKeysExecutionEntity.MESSAGE_MAX_LENGTH);
        entity.setMessage(message);
    }

}
