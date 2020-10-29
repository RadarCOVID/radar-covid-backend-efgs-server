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

import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity
import es.gob.radarcovid.efgs.persistence.mapper.BatchJobExecutionMapper
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class BatchJobExecutionMapperTestSpec extends Specification {

	@Autowired
	private BatchJobExecutionMapper mapper

	@Unroll
	def 'entity id [#entity.id] to dto'(BatchJobExecutionEntity entity) {
		given:
		def dto = mapper.entityToDto(entity)

		expect:
		dto.id == entity.id
		dto.jobName == entity.jobName
		dto.startTime == entity.startTime
		dto.endTime == entity.endTime
		dto.message == entity.message
		dto.status == entity.status

		where:
		entity << [
			createEntity(1, 'clean-job', LocalDateTime.now(), null, null, BatchJobStatusEnum.STARTED),
			createEntity(2, 'upload-job', LocalDateTime.now(), LocalDateTime.now().plusMinutes(2), 'upload message', BatchJobStatusEnum.FAILED),
			createEntity(3, 'download-job', LocalDateTime.now(), LocalDateTime.now().plusMinutes(3), 'download message', BatchJobStatusEnum.COMPLETED)
		]
	}

	@Unroll
	def 'dto id [#dto.id] to entity'(BatchJobExecutionDto dto) {
		given:
		def entity = mapper.dtoToEntity(dto)

		expect:
		entity.id == null
		entity.jobName == dto.jobName
		entity.startTime == dto.startTime
		entity.endTime == dto.endTime
		entity.message == dto.message
		entity.status == dto.status

		where:
		dto << [
			createDto(1, 'clean-job', LocalDateTime.now(), null, null, BatchJobStatusEnum.STARTED),
			createDto(2, 'upload-job', LocalDateTime.now(), LocalDateTime.now().plusMinutes(2), 'upload message', BatchJobStatusEnum.FAILED),
			createDto(3, 'download-job', LocalDateTime.now(), LocalDateTime.now().plusMinutes(3), 'download message', BatchJobStatusEnum.COMPLETED)
		]
	}

	def createEntity(Long id, String jobName, LocalDateTime startTime, LocalDateTime endTime, String message, BatchJobStatusEnum status) {
		def entity = new BatchJobExecutionEntity()
		entity.setId(id)
		entity.setJobName(jobName)
		entity.setStartTime(startTime)
		entity.setEndTime(endTime)
		entity.setMessage(message)
		entity.setStatus(status)
		return entity
	}

	def createDto(Long id, String jobName, LocalDateTime startTime, LocalDateTime endTime, String message, BatchJobStatusEnum status) {
		def dto = new BatchJobExecutionDto()
		dto.setId(id)
		dto.setJobName(jobName)
		dto.setStartTime(startTime)
		dto.setEndTime(endTime)
		dto.setMessage(message)
		dto.setStatus(status)
		return dto
	}
}
