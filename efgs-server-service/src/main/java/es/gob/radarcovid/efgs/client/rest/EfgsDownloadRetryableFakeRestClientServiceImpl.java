/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.client.rest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.protobuf.ByteString;

import es.gob.radarcovid.efgs.client.EfgsDownloadClientService;
import es.gob.radarcovid.efgs.client.model.EfgsDownload;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKey;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch;
import eu.interop.federationgateway.model.EfgsProto.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsDownloadRetryableFakeRestClientServiceImpl implements EfgsDownloadClientService {
	
	private static final int MAX_DIAGNOSISKEYS = 10;
	private static final int KEY_LENGTH = 16;
	
	private final EfgsProperties efgsProperties;
	private final Map<String, DiagnosisKeyBatch> diaganosisKeyBatchMap;
	
	@Override
	public Optional<EfgsDownload> download(LocalDate date, String batchTag) {
		log.debug("Entering EfgsDownloadClientServiceImpl.download('{}', '{}')", date, batchTag);
		
		String newBatchTag = batchTag != null ? batchTag : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		DiagnosisKeyBatch diagnosisKeyBatch = diaganosisKeyBatchMap.computeIfAbsent(newBatchTag, this::generateRandomDiagnosisKeyBatch);
		EfgsDownload.EfgsDownloadBuilder builder = EfgsDownload.builder()
				.batchTag(newBatchTag)
				.diagnosisKeyBatch(diagnosisKeyBatch);
		Optional<EfgsDownload> result  = Optional.of(builder.build());
		
		log.debug("Leaving EfgsDownloadClientServiceImpl.download with: {} results", result.map(d -> d.getDiagnosisKeyBatch().getKeysList().size()).orElse(0));
		return result;
	}
	
	private DiagnosisKeyBatch generateRandomDiagnosisKeyBatch(String batchTag) {
		DiagnosisKeyBatch.Builder builder = DiagnosisKeyBatch.newBuilder();
		for (int i = 0; i < new Random().nextInt(MAX_DIAGNOSISKEYS) + 1; i++) {
			DiagnosisKey diagnosisKey = DiagnosisKey.newBuilder()
					.setKeyData(ByteString.copyFrom(RandomStringUtils.randomAlphanumeric(KEY_LENGTH).getBytes()))
					.setRollingStartIntervalNumber(Math.toIntExact(Instant.now().getEpochSecond()/600))
					.setRollingPeriod(144)
					.setTransmissionRiskLevel(1)
					.setOrigin(efgsProperties.getCountry())
					.setReportType(ReportType.CONFIRMED_TEST)
					.setDaysSinceOnsetOfSymptoms(0)
					.build();
			builder.addKeys(diagnosisKey);
		}
		return builder.build();
	}

}
