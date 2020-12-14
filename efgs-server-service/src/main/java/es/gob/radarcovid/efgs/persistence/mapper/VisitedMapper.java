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

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;

import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.entity.VisitedEntity;

@Mapper(componentModel = "spring")
public abstract class VisitedMapper {
	
	@Autowired
	private EfgsProperties efgsProperties;
    
	public String entityToValue(VisitedEntity entity) {
		return entity.getCountry();
	}
    
    @Mappings({
    	@Mapping(target = "exposedId", ignore = true),
        @Mapping(target = "gaenExposed", ignore = true),
        @Mapping(target = "country", source = "value")
    })
	public abstract VisitedEntity valueToEntity(String value);
    
	public Set<String> protocolStringListToList(ProtocolStringList protocolStringList) {
        if (protocolStringList == null) {
            return null;
        }
		return protocolStringList.asByteStringList().stream()
				.map(this::byteStringToValue)
				.filter(efgsProperties.getCountryList()::contains)
				.collect(Collectors.toSet());
	}
	
	public String byteStringToValue(ByteString byteString) {
		return byteString.toStringUtf8().trim();
	}

}
