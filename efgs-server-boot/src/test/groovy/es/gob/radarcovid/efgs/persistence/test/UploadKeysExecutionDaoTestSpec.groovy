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

import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao
import es.gob.radarcovid.efgs.persistence.UploadKeysExecutionDao
import es.gob.radarcovid.efgs.persistence.model.UploadKeysExecutionDto
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles("test")
class UploadKeysExecutionDaoTestSpec extends Specification {

	@Autowired
	private UploadKeysExecutionDao uploadKeysExecutionDao

	@Autowired
	private BatchJobExecutionDao batchJobExecutionDao

	def setup() {
		batchJobExecutionDao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'save upload keys batchTag [#dto.batchTag]'(UploadKeysExecutionDto dto) {
		given:
		def batchJob = batchJobExecutionDao.startBatchJob('batch-job')

		expect:
		uploadKeysExecutionDao.saveUploadKeys(batchJob.id, dto)

		where:
		dto << [
			createDto('batch-tag-1', 'batch-signature-1', 'upload OK', true),
			createDto('batch-tag-2', 'batch-signature-2', null, false)
		]
	}

	def createDto(String batchTag, String batchSignature, String message, boolean success) {
		def dto = new UploadKeysExecutionDto()
		dto.setUploadDate(LocalDateTime.now())
		dto.setBatchTag(batchTag)
		dto.setBatchSignature(batchSignature)
		dto.setMessage(message)
		dto.setSuccess(success)
		return dto
	}
}
