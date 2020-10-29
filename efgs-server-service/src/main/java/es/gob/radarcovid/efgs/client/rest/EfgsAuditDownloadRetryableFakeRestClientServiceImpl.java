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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.gob.radarcovid.efgs.client.EfgsAuditDownloadClientService;
import es.gob.radarcovid.efgs.client.model.AuditEntry;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureUtils;
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsAuditDownloadRetryableFakeRestClientServiceImpl implements EfgsAuditDownloadClientService {
	
	private final EfgsProperties efgsProperties;
	private final SignatureGenerator signatureGenerator;
	private final Map<String, DiagnosisKeyBatch> diaganosisKeyBatchMap;

	@Override
	public Optional<List<AuditEntry>> auditDownload(LocalDate date, String batchTag) {
		log.debug("Entering EfgsAuditDownloadRetryableRestClientServiceImpl.auditDownload('{}', '{}')", date, batchTag);
		Optional<List<AuditEntry>> result = Optional.ofNullable(diaganosisKeyBatchMap.remove(batchTag)).map(this::generateAuditsEntries);
		log.debug("Leaving EfgsAuditDownloadRetryableRestClientServiceImpl.auditDownload with: {} results", result.map(List::size).orElse(0));
		return result;
	}

	private List<AuditEntry> generateAuditsEntries(DiagnosisKeyBatch diagnosisKeyBatch) {
		try {
			AuditEntry audit = new AuditEntry();
			audit.setCountry(efgsProperties.getCountry());
			audit.setBatchSignature(signatureGenerator.getSignatureForBytes(BatchSignatureUtils.generateBytesToVerify(diagnosisKeyBatch)));
			return Arrays.asList(audit);
		} catch (Exception e) {
			return new ArrayList<>();
		}		
	}
	
}
