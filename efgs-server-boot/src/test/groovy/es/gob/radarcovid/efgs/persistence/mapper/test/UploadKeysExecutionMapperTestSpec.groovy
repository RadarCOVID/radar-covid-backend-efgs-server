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
import es.gob.radarcovid.efgs.persistence.entity.UploadKeysExecutionEntity
import es.gob.radarcovid.efgs.persistence.mapper.UploadKeysExecutionMapper
import es.gob.radarcovid.efgs.persistence.model.UploadKeysExecutionDto
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class UploadKeysExecutionMapperTestSpec extends Specification {

	@Autowired
	private UploadKeysExecutionMapper mapper

	@Unroll
	def 'entity id [#entity.id] to dto'(UploadKeysExecutionEntity entity) {
		given:
		def dto = mapper.entityToDto(entity)

		expect:
		dto.id == entity.id
		dto.batchId == entity.batchJobExecution.id
		dto.uploadDate == entity.uploadDate
		dto.batchTag == entity.batchTag
		dto.batchSignature == entity.batchSignature
		dto.message == entity.message
		dto.success == entity.success

		where:
		entity << [
			createEntity(1, 1, LocalDateTime.now(), null, null, null, true),
			createEntity(2, 1, LocalDateTime.now(), 'batch-tag-1', '', '', false),
			createEntity(3, 2, LocalDateTime.now(), 'batch-tag-2', 'batch-tag-3', 'uploaded OK', true)
		]
	}

	@Unroll
	def 'dto id [#dto.id] to entity'(UploadKeysExecutionDto dto) {
		given:
		def entity = mapper.dtoToEntity(dto)

		expect:
		entity.id == null
		entity.batchJobExecution.id == dto.batchId
		entity.uploadDate == dto.uploadDate
		entity.batchTag == dto.batchTag
		entity.batchSignature == dto.batchSignature
		entity.message == dto.message
		entity.success == dto.success

		where:
		dto << [
			createDto(1, 1, LocalDateTime.now(), null, null, null, true),
			createDto(2, 1, LocalDateTime.now(), 'batch-tag-1', '', '', false),
			createDto(3, 2, LocalDateTime.now(), 'batch-tag-2', 'batch-tag-3', 'uploaded OK', true)
		]
	}

	def createEntity(Long id, Long batchJobExecutionId, LocalDateTime uploadDate, String batchTag, String batchSignature,
			String message, boolean success) {
		def batchJobExecution = new BatchJobExecutionEntity()
		batchJobExecution.setId(batchJobExecutionId)
		def entity = new UploadKeysExecutionEntity()
		entity.setId(id)
		entity.setBatchJobExecution(batchJobExecution)
		entity.setUploadDate(uploadDate)
		entity.setBatchTag(batchTag)
		entity.setBatchSignature(batchSignature)
		entity.setMessage(message)
		entity.setSuccess(success)
		return entity
	}

	def createDto(Long id, Long batchJobExecutionId, LocalDateTime uploadDate, String batchTag, String batchSignature,
			String message, boolean success) {
		def dto = new UploadKeysExecutionDto()
		dto.setId(id)
		dto.setBatchId(batchJobExecutionId)
		dto.setUploadDate(uploadDate)
		dto.setBatchTag(batchTag)
		dto.setBatchSignature(batchSignature)
		dto.setMessage(message)
		dto.setSuccess(success)
		return dto
	}
}
