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

import java.time.LocalDate
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao
import es.gob.radarcovid.efgs.persistence.DownloadExecutionDao
import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles("test")
class DownloadExecutionDaoTestSpec extends Specification {

	private static final LocalDate DOWNLOAD_DATE = LocalDate.now().minusDays(20)
	private static final String BATCHTAG_1 = 'batch-tag-1'
	private static final String BATCHTAG_2 = 'batch-tag-2'

	@Autowired
	private DownloadExecutionDao downloadExecutionDao

	@Autowired
	private BatchJobExecutionDao batchJobExecutionDao

	def setup() {
		batchJobExecutionDao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'save download downloadDate [#dto.downloadDate] : batchTag [#dto.batchTag]'(DownloadExecutionDto dto) {
		given:
		def batchJob = batchJobExecutionDao.startBatchJob('batch-job')

		expect:
		downloadExecutionDao.saveDownload(batchJob.id, dto)

		where:
		dto << [
			createDto(DOWNLOAD_DATE, BATCHTAG_1, BATCHTAG_2, 'downloaded OK', true),
			createDto(DOWNLOAD_DATE, BATCHTAG_2, null, null, false)
		]
	}

	def 'find last download success'() {
		given:
		def batchJob = batchJobExecutionDao.startBatchJob('batch-job')
		downloadExecutionDao.saveDownload(batchJob.id, createDto(DOWNLOAD_DATE, BATCHTAG_1, null, 'downloaded OK', true))
		def lastSuccess = downloadExecutionDao.findLastDownloadSuccess()

		expect:
		lastSuccess.present
		lastSuccess.get().downloadDate == DOWNLOAD_DATE
		lastSuccess.get().batchTag == BATCHTAG_1
	}

	def createDto(LocalDate downloadDate, String batchTag, String nextBatchTag, String message, boolean success) {
		def dto = new DownloadExecutionDto()
		dto.setDownloadDate(downloadDate)
		dto.setBatchTag(batchTag)
		dto.setNextBatchTag(nextBatchTag)
		dto.setMessage(message)
		dto.setSuccess(success)
		return dto
	}
}
