/**
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.mapper.test

import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.etc.EfgsProperties
import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto
import eu.interop.federationgateway.model.EfgsProto.ReportType
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class GaenExposedMapperTestSpec extends Specification {

	@Autowired
	private GaenExposedMapper mapper

	@Autowired
	private EfgsProperties efgsProperties

	@Unroll
	def 'entity id [#entity.id] to dto'(GaenExposedEntity entity) {
		given:
		def dto = mapper.entityToDto(entity)

		expect:
		dto.key == entity.key
		dto.rollingStartNumber == entity.rollingStartNumber
		dto.rollingPeriod == entity.rollingPeriod
		dto.transmissionRiskLevel == entity.transmissionRiskLevel
		dto.receivedAt == entity.receivedAt
		dto.countryOrigin == efgsProperties.country
		dto.reportType == efgsProperties.uploadDiagnosisKeys.defaultValues.reportType
		dto.daysSinceOnset == (entity.daysSinceOnset != null ? entity.daysSinceOnset : 1)
		dto.efgsSharing == entity.efgsSharing

		where:
		entity << [
			createEntity(1, 'key1', 13, 5, 8, LocalDateTime.now(), 11, true),
			createEntity(2, 'key2', 13, 144, 3, LocalDateTime.now(), -11, true),
			createEntity(3, 'key3', null, null, null, LocalDateTime.now(), null, false)
		]
	}

	@Unroll
	def 'dto key [#dto.key] to entity'(GaenExposedDto dto) {
		given:
		def entity = mapper.dtoToEntity(dto)

		expect:
		entity.id == null
		entity.key == dto.key
		entity.rollingStartNumber == dto.rollingStartNumber
		entity.rollingPeriod == dto.rollingPeriod
		entity.transmissionRiskLevel == dto.transmissionRiskLevel
		entity.receivedAt == dto.receivedAt
		entity.countryOrigin == dto.countryOrigin
		entity.reportType == dto.reportType
		entity.daysSinceOnset == dto.daysSinceOnset
		entity.efgsSharing == dto.efgsSharing

		where:
		dto << [
			createDto('key1', 13, 5, 8, LocalDateTime.now(), 'ES', ReportType.CONFIRMED_TEST, 11, true),
			createDto('key2', 13, 144, 3, LocalDateTime.now(), 'DE', ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, -11, true),
			createDto('key3', null, null, null, LocalDateTime.now(), null, ReportType.SELF_REPORT, null, false)
		]
	}

	def createEntity(Integer id, String key, Integer rollingStartNumber, Integer rollingPeriod, Integer transmissionRiskLevel,
			LocalDateTime receivedAt, Integer daysSinceOnset, Boolean efgsSharing) {
		def entity = new GaenExposedEntity()
		entity.setId(id)
		entity.setKey(key)
		entity.setRollingStartNumber(rollingStartNumber)
		entity.setRollingPeriod(rollingPeriod)
		entity.setTransmissionRiskLevel(transmissionRiskLevel)
		entity.setReceivedAt(receivedAt)
		entity.setDaysSinceOnset(daysSinceOnset)
		entity.setEfgsSharing(efgsSharing)
		return entity
	}

	def createDto(String key, Integer rollingStartNumber, Integer rollingPeriod, Integer transmissionRiskLevel,
			LocalDateTime receivedAt, String countryOrigin, ReportType reportType, Integer daysSinceOnset, Boolean efgsSharing) {
		def dto = new GaenExposedDto()
		dto.setKey(key)
		dto.setRollingStartNumber(rollingStartNumber)
		dto.setRollingPeriod(rollingPeriod)
		dto.setTransmissionRiskLevel(transmissionRiskLevel)
		dto.setReceivedAt(receivedAt)
		dto.setCountryOrigin(countryOrigin)
		dto.setReportType(reportType)
		dto.setDaysSinceOnset(daysSinceOnset)
		dto.setEfgsSharing(efgsSharing)
		return dto
	}
}
