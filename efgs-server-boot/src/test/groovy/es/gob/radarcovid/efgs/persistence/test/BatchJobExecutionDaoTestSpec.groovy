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
import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity
import es.gob.radarcovid.efgs.persistence.repository.BatchJobExecutionRepository
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles("test")
class BatchJobExecutionDaoTestSpec extends Specification {

	@Autowired
	private BatchJobExecutionDao batchJobExecutionDao
	
	@Autowired
	private BatchJobExecutionRepository repository
	
	def setup() {
		batchJobExecutionDao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'start job [#job]'(String job) {
		given:
		def startTime = LocalDateTime.now()
		def dto = batchJobExecutionDao.startBatchJob(job)

		expect:
		dto.id != null
		dto.jobName == job
		dto.startTime >= startTime
		dto.startTime <= LocalDateTime.now()
		dto.status == BatchJobStatusEnum.STARTED

		where:
		job              | _
		'batch-job-name' | _
	}

	@Unroll
	def 'end job [#job]'(String job, BatchJobStatusEnum status, String message) {
		given:
		def dto = batchJobExecutionDao.startBatchJob(job)
		dto.setMessage(message)
		dto.setStatus(status)
		
		expect:
		batchJobExecutionDao.endBatchJob(dto)

		where:
		job           | status                       | message
		'batch-job-1' | BatchJobStatusEnum.COMPLETED | 'OK message'
		'batch-job-2' | BatchJobStatusEnum.FAILED    | 'KO message'
	}

	@Unroll
	def 'delete all before [#date]'(LocalDateTime date) {
		given:
		repository.save(createBatchJobExecutionEntity(date.minusDays(1)))
		repository.save(createBatchJobExecutionEntity(date.plusDays(1)))
		
		expect:
		batchJobExecutionDao.deleteAllBefore(date) == 1

		where:
		date                               | _
		LocalDateTime.now().minusMonths(1) | _
	}
	
	def createBatchJobExecutionEntity(LocalDateTime startTime) {
		def entity = new BatchJobExecutionEntity()
		entity.setJobName('old-batch-job')
		entity.setStartTime(startTime)
		entity.setStatus(BatchJobStatusEnum.STARTED)
		return entity
	}
}
