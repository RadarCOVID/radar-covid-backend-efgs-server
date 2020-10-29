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

import java.time.LocalDateTime;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity;
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto;
import eu.interop.federationgateway.model.EfgsProto;

@Mapper(componentModel = "spring")
public abstract class GaenExposedMapper {

	@Autowired
	private EfgsProperties efgsProperties;

	@Mappings({
			@Mapping(target = "countryOrigin", ignore = true),
			@Mapping(target = "reportType", ignore = true),
			@Mapping(target = "daysSinceOnset", ignore = true)
	})
	public abstract GaenExposedDto entityToDto(GaenExposedEntity entity);
	
	@Mapping(target = "id", ignore = true)
	public abstract GaenExposedEntity dtoToEntity(GaenExposedDto dto);

	@AfterMapping
	protected void setInternalValues(GaenExposedEntity entity, @MappingTarget GaenExposedDto dto) {
		Integer daysSinceOnsetOfSymptoms = 1;
		if (entity.getDaysSinceOnset() != null) {
			daysSinceOnsetOfSymptoms = entity.getDaysSinceOnset();
		}
		dto.setDaysSinceOnset(daysSinceOnsetOfSymptoms);
		if (StringUtils.isEmpty(entity.getCountryOrigin())) {
			dto.setCountryOrigin(efgsProperties.getCountry());
		} else {
			dto.setCountryOrigin(entity.getCountryOrigin());
		}
		dto.setReportType(efgsProperties.getUploadDiagnosisKeys().getDefaultValues().getReportType());
	}

	public EfgsProto.DiagnosisKey dtoToDiagnosisKey(GaenExposedDto gaenExposedDto) {
		return EfgsProto.DiagnosisKey.newBuilder()
				.setKeyData(ByteString.copyFrom(Base64.getDecoder().decode(gaenExposedDto.getKey())))
				.setRollingStartIntervalNumber(gaenExposedDto.getRollingStartNumber())
				.setRollingPeriod(gaenExposedDto.getRollingPeriod())
				.setTransmissionRiskLevel(gaenExposedDto.getTransmissionRiskLevel())
				.setOrigin(gaenExposedDto.getCountryOrigin())
				.setReportType(gaenExposedDto.getReportType())
				.setDaysSinceOnsetOfSymptoms(gaenExposedDto.getDaysSinceOnset())
				.addAllVisitedCountries(efgsProperties.getUploadDiagnosisKeys().getCountryList())
				.build();
	}
	
	@Mappings({
		@Mapping(target = "key", ignore = true),
		@Mapping(target = "receivedAt", ignore = true),
		@Mapping(target = "rollingStartNumber", source = "rollingStartIntervalNumber"),
		@Mapping(target = "countryOrigin", source = "origin"),
		@Mapping(target = "daysSinceOnset", ignore = true),
		@Mapping(target = "efgsSharing", ignore = true),
		@Mapping(target = "batchTag", ignore = true)
	})
	public abstract GaenExposedDto diagnosisKeyToDto(EfgsProto.DiagnosisKey diagnosisKey);
	
	@AfterMapping
	protected void setInternalValues(EfgsProto.DiagnosisKey diagnosisKey, @MappingTarget GaenExposedDto dto) {
		dto.setKey(Base64.getEncoder().encodeToString(diagnosisKey.getKeyData().toByteArray()));
		dto.setReceivedAt(LocalDateTime.now());
		dto.setDaysSinceOnset(normalizeDSOS(diagnosisKey.getDaysSinceOnsetOfSymptoms()));
		dto.setEfgsSharing(true);
	}

	private int normalizeDSOS(int dsos) {
		int intervalDuration = Math.round((float) dsos / 100);
		int offset = intervalDuration * 100;
		return dsos - offset;
	}

}
