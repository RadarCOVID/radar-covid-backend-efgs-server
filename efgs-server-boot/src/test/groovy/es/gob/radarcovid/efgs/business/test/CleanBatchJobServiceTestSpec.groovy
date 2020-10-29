/**
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.business.test

import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.business.CleanBatchJobService
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao
import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity
import es.gob.radarcovid.efgs.persistence.repository.BatchJobExecutionRepository
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class CleanBatchJobServiceTestSpec extends Specification {

	@Autowired
	CleanBatchJobService service

	@Autowired
	BatchJobExecutionRepository repository

	@Autowired
	BatchJobExecutionDao dao

	def setup() {
		dao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'clean batch job with months [#months]'(int months, boolean result) {
		given:
		BatchJobExecutionEntity entity_before = repository.save(createBatchJobExecutionEntity(LocalDateTime.now().minusMonths(months)))

		when:
		service.cleanBatchJobExecution()
		def entity_after = repository.findById(entity_before.id)

		then:
		entity_after.present == result

		where:
		months | result
		7      | false
		6      | false
		5      | true
	}

	def createBatchJobExecutionEntity(LocalDateTime startTime) {
		def entity = new BatchJobExecutionEntity()
		entity.setJobName('old-batch-job')
		entity.setStartTime(startTime)
		entity.setStatus(BatchJobStatusEnum.STARTED)
		return entity
	}
}
