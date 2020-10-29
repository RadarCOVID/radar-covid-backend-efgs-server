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

import java.time.LocalDate
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity
import es.gob.radarcovid.efgs.persistence.entity.DownloadExecutionEntity
import es.gob.radarcovid.efgs.persistence.mapper.DownloadExecutionMapper
import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class DownloadExecutionMapperTestSpec extends Specification {

	@Autowired
	private DownloadExecutionMapper mapper

	@Unroll
	def 'entity id [#entity.id] to dto'(DownloadExecutionEntity entity) {
		given:
		def dto = mapper.entityToDto(entity)

		expect:
		dto.id == entity.id
		dto.batchId == entity.batchJobExecution.id
		dto.createdAt == entity.createdAt
		dto.downloadDate == entity.downloadDate
		dto.batchTag == entity.batchTag
		dto.nextBatchTag == entity.nextBatchTag
		dto.message == entity.message
		dto.success == entity.success

		where:
		entity << [
			createEntity(1, 1, LocalDateTime.now(), LocalDate.now(), null, null, null, true),
			createEntity(2, 1, LocalDateTime.now(), LocalDate.now().minusDays(4), 'batch-tag-1', '', '', false),
			createEntity(3, 2, LocalDateTime.now(), LocalDate.now().minusDays(10), 'batch-tag-2', 'batch-tag-3', 'downloaded OK', true)
		]
	}

	@Unroll
	def 'dto id [#dto.id] to entity'(DownloadExecutionDto dto) {
		given:
		def entity = mapper.dtoToEntity(dto)

		expect:
		entity.id == null
		entity.batchJobExecution.id == dto.batchId
		entity.createdAt == dto.createdAt
		entity.downloadDate == dto.downloadDate
		entity.batchTag == dto.batchTag
		entity.nextBatchTag == dto.nextBatchTag
		entity.message == dto.message
		entity.success == dto.success

		where:
		dto << [
			createDto(1, 1, LocalDateTime.now(), LocalDate.now(), null, null, null, true),
			createDto(2, 1, LocalDateTime.now(), LocalDate.now().minusDays(4), 'batch-tag-1', '', '', false),
			createDto(3, 2, LocalDateTime.now(), LocalDate.now().minusDays(10), 'batch-tag-2', 'batch-tag-3', 'downloaded OK', true)
		]
	}

	def createEntity(Long id, Long batchJobExecutionId, LocalDateTime createdAt, LocalDate downloadDate, String batchTag, String nextBatchTag,
			String message, boolean success) {
		def batchJobExecution = new BatchJobExecutionEntity()
		batchJobExecution.setId(batchJobExecutionId)
		def entity = new DownloadExecutionEntity()
		entity.setId(id)
		entity.setBatchJobExecution(batchJobExecution)
		entity.setCreatedAt(createdAt)
		entity.setDownloadDate(downloadDate)
		entity.setBatchTag(batchTag)
		entity.setNextBatchTag(nextBatchTag)
		entity.setMessage(message)
		entity.setSuccess(success)
		return entity
	}

	def createDto(Long id, Long batchJobExecutionId, LocalDateTime createdAt, LocalDate downloadDate, String batchTag, String nextBatchTag,
			String message, boolean success) {
		def dto = new DownloadExecutionDto()
		dto.setId(id)
		dto.setBatchId(batchJobExecutionId)
		dto.setCreatedAt(createdAt)
		dto.setDownloadDate(downloadDate)
		dto.setBatchTag(batchTag)
		dto.setNextBatchTag(nextBatchTag)
		dto.setMessage(message)
		dto.setSuccess(success)
		return dto
	}
}
