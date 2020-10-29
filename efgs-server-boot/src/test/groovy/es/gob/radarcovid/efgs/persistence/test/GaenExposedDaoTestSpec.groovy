/**
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.test

import java.time.Instant
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.persistence.GaenExposedDao
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto
import es.gob.radarcovid.efgs.persistence.repository.GaenExposedEntityRepository
import eu.interop.federationgateway.model.EfgsProto.ReportType
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles("test")
class GaenExposedDaoTestSpec extends Specification {

	@Autowired
	private GaenExposedDao gaenExposedDao

	@Autowired
	private GaenExposedEntityRepository repository

	def 'save all'(List dtos) {
		given:
		repository.deleteAll()
		
		expect:
		gaenExposedDao.saveAll(dtos)

		where:
		dtos << [
			Arrays.asList(
			createDto('dGVzdEtleTAxQnl0ZXMtLQ==', 'ES'),
			createDto('dGVzdEtleTAyQnl0ZXMtLQ==', 'ES'),
			createDto('dGVzdEtleTAzQnl0ZXMtLQ==', 'DE'))
		]
	}

	@Unroll
	def 'find by country [#country]'(String country, int page, int size, int result) {
		expect:
		gaenExposedDao.findPendingByCountry(country, page, size).size() == result

		where:
		country | page | size | result
		'ES'    | 0    | 10   | 2
		'ES'    | 1    | 1    | 1
		'DE'    | 0    | 10   | 1
	}

	def createDto(String key, String country) {
		def dto = new GaenExposedDto()
		dto.setKey(key)
		dto.setRollingStartNumber((Instant.now().getEpochSecond()/600).intValue())
		dto.setRollingPeriod(144)
		dto.setTransmissionRiskLevel(1)
		dto.setReceivedAt(LocalDateTime.now())
		dto.setCountryOrigin(country)
		dto.setReportType(ReportType.CONFIRMED_TEST)
		dto.setDaysSinceOnset(0)
		dto.setEfgsSharing(true)
		return dto
	}
}
