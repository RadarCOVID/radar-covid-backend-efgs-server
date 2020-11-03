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

import es.gob.radarcovid.common.exception.EfgsCodeError
import es.gob.radarcovid.common.exception.EfgsServerException
import es.gob.radarcovid.efgs.business.UploadExposedService
import es.gob.radarcovid.efgs.client.impl.EfgsUploadDiagnosisKeysClientServiceImpl
import es.gob.radarcovid.efgs.persistence.BatchJobExecutionDao
import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto
import es.gob.radarcovid.efgs.persistence.model.UploadKeysPayloadDto
import es.gob.radarcovid.efgs.persistence.repository.GaenExposedEntityRepository
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureUtils
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureVerifier
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator
import eu.interop.federationgateway.model.EfgsProto
import eu.interop.federationgateway.model.EfgsProto.ReportType
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles('test')
class UploadExposedServiceTestSpec extends Specification {

	@Autowired
	UploadExposedService service

	@Autowired
	GaenExposedEntityRepository repository

	@Autowired
	BatchJobExecutionDao batchJobExecutionDao

	@Autowired
	GaenExposedMapper mapper

	@Autowired
	SignatureGenerator signatureGenerator

	@Autowired
	BatchSignatureVerifier batchSignatureVerifier

	@SpringBean
	EfgsUploadDiagnosisKeysClientServiceImpl efgsUploadDiagnosisKeysClient = Mock()

	def setup() {
		batchJobExecutionDao.deleteAllBefore(LocalDateTime.now())
	}

	@Unroll
	def 'upload diagnosis keys [#key]'(String key, int rollingStartNumber, String originCountry, boolean efgsSharing, boolean result) {
		given:
		BatchJobExecutionDto batchDto = batchJobExecutionDao.startBatchJob('upload-test')
		GaenExposedEntity entity_before = repository.save(createExposedEntity(key, rollingStartNumber, originCountry, efgsSharing))

		and:
		efgsUploadDiagnosisKeysClient.uploadDiagnosisKeys(_) >> Optional.of("OK")

		when:
		service.uploadDiagnosisKeys(batchDto)
		def entity_after = repository.findById(entity_before.id)

		then:
		entity_after.present
		(entity_after.get().batchTag != null) == result

		where:
		key                | rollingStartNumber                 | originCountry | efgsSharing  | result
		'testKey31Bytes--' | Instant.now().getEpochSecond()/600 | 'ES'          | true         | true
		'testKey32Bytes--' | 0                                  | 'ES'          | true         | false
		'testKey33Bytes--' | Instant.now().getEpochSecond()/600 | 'DE'          | true         | false
		'testKey34Bytes--' | Instant.now().getEpochSecond()/600 | 'ES'          | false        | false
	}

	def 'upload diagnosis keys with exception'() {
		given:
		BatchJobExecutionDto batchDto = batchJobExecutionDao.startBatchJob('upload-test')
		repository.save(createExposedEntity('testKey35Bytes--' , (Instant.now().getEpochSecond()/600).intValue(), 'ES', true))

		and:
		efgsUploadDiagnosisKeysClient.uploadDiagnosisKeys(_) >> { throw new HttpClientErrorException(HttpStatus.BAD_REQUEST) }

		when:
		service.uploadDiagnosisKeys(batchDto)

		then:
		EfgsServerException exception = thrown()
		exception.code == EfgsCodeError.UPLOAD_DIAGNOSIS_KEYS
	}

	def 'signature'(String key, int rollingStartNumber, String originCountry, boolean efgsSharing, boolean result) {
		given:
		GaenExposedEntity entity_before = createExposedEntity(key, rollingStartNumber, originCountry, efgsSharing)
		GaenExposedDto gaenExposedDto = mapper.entityToDto(entity_before)
		List<GaenExposedDto> gaenExposedDtoList = new ArrayList<>()
		gaenExposedDtoList.add(gaenExposedDto)

		UploadKeysPayloadDto uploadKeysPayload = new UploadKeysPayloadDto();
		uploadKeysPayload.setOriginalKeys(gaenExposedDtoList);
		List<EfgsProto.DiagnosisKey> diagnosisKeyList = new ArrayList<>();
		gaenExposedDtoList.stream().map(mapper::dtoToDiagnosisKey).forEach(diagnosisKeyList::add);
		uploadKeysPayload.setDiagnosisKeyBatch(EfgsProto.DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeyList).build());

		when:
		uploadKeysPayload.setBatchSignature(signatureGenerator.getSignatureForBytes(
				BatchSignatureUtils.generateBytesToVerify(uploadKeysPayload.getDiagnosisKeyBatch())));

		then:
		batchSignatureVerifier.verify(uploadKeysPayload.diagnosisKeyBatch, uploadKeysPayload.batchSignature)

		where:
		key                | rollingStartNumber                 | originCountry | efgsSharing  | result
		'testKey31Bytes--' | Instant.now().getEpochSecond()/600 | 'ES'          | true         | true

	}

	def createExposedEntity(String key, int rollingStartNumber, String originCountry, boolean efgsSharing) {
		def GaenExposedEntity dto = new GaenExposedEntity()
		dto.setKey(Base64.getEncoder().encodeToString(key.getBytes('UTF-8')))
		dto.setRollingStartNumber(rollingStartNumber.intValue())
		dto.setRollingPeriod(144)
		dto.setTransmissionRiskLevel(0)
		dto.setReceivedAt(LocalDateTime.now())
		dto.setCountryOrigin(originCountry)
		dto.setReportType(ReportType.CONFIRMED_TEST)
		dto.setDaysSinceOnset(0)
		dto.setEfgsSharing(efgsSharing)
		return dto
	}
}
