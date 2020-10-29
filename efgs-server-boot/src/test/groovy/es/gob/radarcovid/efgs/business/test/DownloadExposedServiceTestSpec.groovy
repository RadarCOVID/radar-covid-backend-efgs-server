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

import java.time.LocalDate
import java.time.LocalDateTime

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException

import com.google.protobuf.ByteString

import es.gob.radarcovid.common.exception.EfgsCodeError
import es.gob.radarcovid.common.exception.EfgsServerException
import es.gob.radarcovid.efgs.business.DownloadExposedService
import es.gob.radarcovid.efgs.client.impl.EfgsAuditDownloadClientServiceImpl
import es.gob.radarcovid.efgs.client.impl.EfgsDownloadClientServiceImpl
import es.gob.radarcovid.efgs.client.model.AuditEntry
import es.gob.radarcovid.efgs.client.model.EfgsDownload
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao
import es.gob.radarcovid.efgs.persistence.DownloadExecutionDao
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto
import es.gob.radarcovid.efgs.persistence.repository.GaenExposedEntityRepository
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureUtils
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKey
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch
import eu.interop.federationgateway.model.EfgsProto.ReportType
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles('test')
class DownloadExposedServiceTestSpec extends Specification {

	private static final String COUNTRY = 'ES'

	private static final LocalDate DAY1 = LocalDate.now().minusDays(4)
	private static final LocalDate DAY2 = LocalDate.now().minusDays(3)
	private static final LocalDate DAY3 = LocalDate.now().minusDays(2)
	private static final LocalDate DAY4	= LocalDate.now().minusDays(1)

	@Autowired
	DownloadExposedService service

	@Autowired
	GaenExposedEntityRepository repository

	@Autowired
	BatchJobExecutionDao batchJobExecutionDao
	
	@Autowired
	DownloadExecutionDao downloadExecutionDao

	@Autowired
	SignatureGenerator signatureGenerator

	@SpringBean
	EfgsDownloadClientServiceImpl efgsDownloadClientService = Mock()

	@SpringBean
	EfgsAuditDownloadClientServiceImpl efgsAuditDownloadClientService = Mock()

	def setup() {
		batchJobExecutionDao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'download diagnosis keys with downloadDate [#downloadDate] : batchTag [#batchTag]'(LocalDate downloadDate, String batchTag,
			String nextBatchTag, String[] keys, String[] keysNext, int result) {
		given:
		BatchJobExecutionDto batchDto = batchJobExecutionDao.startBatchJob('download-test')

		and:
		efgsDownloadClientService.download(downloadDate, null) >> Optional.of(createEfgsDownload(batchTag, nextBatchTag, keys))
		efgsDownloadClientService.download(downloadDate, batchTag) >> Optional.of(createEfgsDownload(batchTag, nextBatchTag, keys))
		efgsDownloadClientService.download(downloadDate, nextBatchTag) >> Optional.of(createEfgsDownload(nextBatchTag, null, keysNext))
		efgsDownloadClientService.download(_, _) >> Optional.empty()
		
		and:
		efgsAuditDownloadClientService.auditDownload(downloadDate, batchTag) >> Optional.of(createAuditEntries(keys))
		efgsAuditDownloadClientService.auditDownload(downloadDate, nextBatchTag) >> Optional.of(createAuditEntries(keysNext))

		when:
		service.downloadDiagnosisKeys(batchDto)
		
		def gaenEntity = new ArrayList()
		if (batchTag != null) {
			gaenEntity = repository.findByCountryOriginAndEfgsSharingAndBatchTag(COUNTRY, true, batchTag)
		}
		
		def gaenEntityNext = new ArrayList()
		if (nextBatchTag != null) {
			gaenEntityNext = repository.findByCountryOriginAndEfgsSharingAndBatchTag(COUNTRY, true, nextBatchTag)
		}
		
		def lastSuccess = downloadExecutionDao.findLastDownloadSuccess()

		then:
		gaenEntity.size() + gaenEntityNext.size() == result
		lastSuccess.present
		lastSuccess.get().downloadDate == LocalDate.now()
		lastSuccess.get().success 

		where:
		downloadDate | batchTag       | nextBatchTag   | keys                                     | keysNext                                 | result
		DAY1         | 'batch-tag-11' | null           | ['testKey11Bytes--', 'testKey12Bytes--'] | []                                       | 2
		DAY2         | 'batch-tag-21' | 'batch-tag-22' | ['testKey13Bytes--']                     | ['testKey14Bytes--']                     | 2
		DAY3         | 'batch-tag-31' | 'batch-tag-32' | ['testKey15Bytes--', 'testKey16Bytes--'] | ['testKey17Bytes--', 'testKey11Bytes--'] | 4
		DAY4         | 'batch-tag-41' | null           | []                                       | []                                       | 0
	}

	def 'download diagnosis keys with exception'() {
		given:
		BatchJobExecutionDto batchDto = batchJobExecutionDao.startBatchJob('download-test')
		efgsDownloadClientService.download(_, _) >> { throw new HttpClientErrorException(HttpStatus.BAD_REQUEST) }

		when:
		service.downloadDiagnosisKeys(batchDto)

		then:
		EfgsServerException exception = thrown()
		exception.code == EfgsCodeError.DOWNLOAD_DIAGNOSIS_KEYS
	}

	def createEfgsDownload(String batchTag, String nextBatchTag, String... keys) {
		return EfgsDownload.builder().batchTag(batchTag).nextBatchTag(nextBatchTag).diagnosisKeyBatch(createKeys(keys)).build()
	}

	def createAuditEntries(String... keys) {
		AuditEntry audit = new AuditEntry()
		audit.setCountry(COUNTRY)
		audit.setBatchSignature(signatureGenerator.getSignatureForBytes(BatchSignatureUtils.generateBytesToVerify(createKeys(keys))))
		return Arrays.asList(audit)
	}

	def createKeys(String... keys) {
		DiagnosisKeyBatch.Builder builder = DiagnosisKeyBatch.newBuilder()
		Arrays.asList(keys).forEach { key ->
			String keyBase64 = Base64.getEncoder().encodeToString(key.getBytes('UTF-8'))
			DiagnosisKey diagnosisKey = DiagnosisKey.newBuilder()
					.setKeyData(ByteString.copyFrom(Base64.getDecoder().decode(keyBase64)))
					.setRollingStartIntervalNumber(1)
					.setRollingPeriod(1)
					.setTransmissionRiskLevel(1)
					.setOrigin(COUNTRY)
					.setReportType(ReportType.CONFIRMED_TEST)
					.setDaysSinceOnsetOfSymptoms(0)
					.build()
			builder.addKeys(diagnosisKey)
		}
		return builder.build()
	}
}
