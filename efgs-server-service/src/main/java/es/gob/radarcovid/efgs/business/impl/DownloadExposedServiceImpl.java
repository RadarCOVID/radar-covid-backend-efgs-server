/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.business.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import es.gob.radarcovid.common.annotation.Loggable;
import es.gob.radarcovid.common.exception.EfgsCodeError;
import es.gob.radarcovid.common.exception.EfgsServerException;
import es.gob.radarcovid.efgs.business.DownloadExposedService;
import es.gob.radarcovid.efgs.client.EfgsAuditDownloadClientService;
import es.gob.radarcovid.efgs.client.EfgsDownloadClientService;
import es.gob.radarcovid.efgs.client.model.AuditEntry;
import es.gob.radarcovid.efgs.client.model.EfgsDownload;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.DownloadExecutionDao;
import es.gob.radarcovid.efgs.persistence.GaenExposedDao;
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper;
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;
import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto;
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto;
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureVerifier;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKey;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DownloadExposedServiceImpl implements DownloadExposedService {

	private final EfgsProperties efgsProperties;
	private final EfgsDownloadClientService downloadClientService;
	private final EfgsAuditDownloadClientService efgsAuditDownloadClientService;
	private final DownloadExecutionDao downloadExecutionDao;
	private final GaenExposedDao gaenExposedDao;
	private final BatchSignatureVerifier batchSignatureVerifier;
	private final GaenExposedMapper gaenExposedMapper;

	@Loggable
	@Override
	public void downloadDiagnosisKeys(BatchJobExecutionDto batchJobExecutionDto) {

		log.debug("Executing scheduler task downloadServiceImpl.downloadKeysTask");

		LocalDate now = LocalDate.now();
		LocalDate downloadDate = now.minusDays(efgsProperties.getRetentionDays());
		String nextBatchTag = null;

		Optional<DownloadExecutionDto> lastSuccessDownload = downloadExecutionDao.findLastDownloadSuccess();
		if (lastSuccessDownload.isPresent() && !lastSuccessDownload.get().getDownloadDate().isBefore(downloadDate)) {
			downloadDate = lastSuccessDownload.get().getDownloadDate();
			nextBatchTag = getNextBatchTag(downloadDate, lastSuccessDownload.get().getBatchTag(), lastSuccessDownload.get().getNextBatchTag());

			if (StringUtils.isEmpty(nextBatchTag)) {
				log.info("NextBatchTag not found for date ({}), batchtag ({})", downloadDate, lastSuccessDownload.get().getBatchTag());
				downloadDate = downloadDate.plusDays(1);
			}
		}
		
		boolean success = true;
		while (!downloadDate.isAfter(now) && success) {
			success = downloadDiagnosisKeys(batchJobExecutionDto, downloadDate, nextBatchTag);
			downloadDate = downloadDate.plusDays(1);
			nextBatchTag = null;
		}

		log.debug("Leaving downloadServiceImpl.downloadKeysTask");

	}

	private boolean downloadDiagnosisKeys(BatchJobExecutionDto batchJobExecutionDto, LocalDate downloadDate, String nextBatchTag) {
		boolean success = true;
		int countNextBatchTag = 0;
		int maxCountNextBatchTag = efgsProperties.getDownloadDiagnosisKeys().getMaximumDownloadNextBatchTag();
		do {
			String batchTag = null;
			String message = null;
			Optional<EfgsDownload> efgsDownload = Optional.empty();
			try {
				
				efgsDownload = downloadClientService.download(downloadDate, nextBatchTag);
				if (efgsDownload.isPresent()) {
					
					nextBatchTag = efgsDownload.get().getNextBatchTag();
					batchTag = efgsDownload.get().getBatchTag();
					DiagnosisKeyBatch diagnosisKeyBatch = efgsDownload.get().getDiagnosisKeyBatch();
					if (!diagnosisKeyBatch.getKeysList().isEmpty()) {
						
						int saved = saveAllDiagnosisKeys(diagnosisKeyBatch, downloadDate, batchTag);
						log.info("Saved {} valid keys from {} downloaded", saved, diagnosisKeyBatch.getKeysList().size());
						message = String.format("Saved %d valid keys from %d downloaded", saved, diagnosisKeyBatch.getKeysList().size());
						
					} else {
						log.info("Empty keys for date ({}) and batchtag ({})", downloadDate, batchTag);
						message = String.format("Empty keys for date (%s) and batchtag (%s)", downloadDate, batchTag);
					}
					
				} else {
					log.info("Diagnosis keys not found for date ({})", downloadDate);
					message = String.format("Diagnosis keys not found for date (%s)", downloadDate);
					nextBatchTag = null;
				}
				
			} catch (Exception e) {
				success = false;
				message = e.getMessage();
				log.error("Exception downloading keys for date ({}): {}", downloadDate, e.getMessage(), e);
				throw new EfgsServerException(EfgsCodeError.DOWNLOAD_DIAGNOSIS_KEYS, String.format("Error downloading keys for date (%s): %s", 
						downloadDate, e.getMessage()));
			} finally {
				DownloadExecutionDto downloadExecutionDto = new DownloadExecutionDto();
				downloadExecutionDto.setDownloadDate(downloadDate);
				downloadExecutionDto.setBatchTag(batchTag);
				downloadExecutionDto.setNextBatchTag(nextBatchTag);
				downloadExecutionDto.setMessage(message);
				downloadExecutionDto.setSuccess(success);
				downloadExecutionDao.saveDownload(batchJobExecutionDto.getId(), downloadExecutionDto);
			}
			
			success = ++countNextBatchTag < maxCountNextBatchTag;
			
		} while (!StringUtils.isEmpty(nextBatchTag) && success);
		return success;
	}
	
	private List<DiagnosisKey> getValidDiagnosisKeys(DiagnosisKeyBatch diagnosisKeyBatch, List<AuditEntry> auditEntries) {
		List<DiagnosisKey> validKeys = new ArrayList<>();

		Map<String, List<DiagnosisKey>> keys = diagnosisKeyBatch.getKeysList().stream()
				.filter(key -> !efgsProperties.getCountry().equals(key.getOrigin()))
				.collect(Collectors.groupingBy(DiagnosisKey::getOrigin));

		Map<String, List<AuditEntry>> audits = auditEntries.stream()
				.filter(audit -> !efgsProperties.getCountry().equals(audit.getCountry()))
				.collect(Collectors.groupingBy(AuditEntry::getCountry));

		keys.forEach((country, keysByCountry) -> {
			validKeys.addAll(getValidDiagnosisKeysByCountry(keysByCountry, audits.get(country)));
		});
		return validKeys;
	}
	
	private List<DiagnosisKey> getValidDiagnosisKeysByCountry(List<DiagnosisKey> diagnosisKeys, List<AuditEntry> auditEntries) {
		List<DiagnosisKey> validKeys = new ArrayList<>();
		AtomicLong skip = new AtomicLong(0);
		
		auditEntries.stream().forEach(audit -> {
				List<DiagnosisKey> diagnosisKeysByAudit = diagnosisKeys.stream().skip(skip.get()).limit(audit.getAmount())
						.collect(Collectors.toList());
				DiagnosisKeyBatch diagnosisKeyBatchByAudit = DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeysByAudit).build();
				
				if (batchSignatureVerifier.verify(diagnosisKeyBatchByAudit, audit.getBatchSignature())) {
					validKeys.addAll(diagnosisKeysByAudit);
				}
				skip.addAndGet(audit.getAmount());
			});
		return validKeys;
	}
	
	private int saveAllDiagnosisKeys(DiagnosisKeyBatch diagnosisKeyBatch, LocalDate downloadDate, String batchTag) {
		int result = 0;
		Optional<List<AuditEntry>> auditEntries = efgsAuditDownloadClientService.auditDownload(downloadDate, batchTag);
		if (auditEntries.isPresent()) {
			List<DiagnosisKey> diagnosisKeys = getValidDiagnosisKeys(diagnosisKeyBatch, auditEntries.get());
			List<GaenExposedDto> gaenExposedList = diagnosisKeys.stream()
					.map(gaenExposedMapper::diagnosisKeyToDto)
					.map(g -> {
						g.setBatchTag(batchTag);
						return g;
					}).collect(Collectors.toList());
			result = gaenExposedDao.saveAll(gaenExposedList);
		}
		return result;
	}
	
	private String getNextBatchTag(LocalDate downloadDate, String batchTag, String nextBatchTag) {
		try {
			if (StringUtils.isEmpty(batchTag)) {
				return downloadClientService.download(downloadDate, batchTag).map(EfgsDownload::getBatchTag).orElse(null);
			} else if (StringUtils.isEmpty(nextBatchTag)) {
				return downloadClientService.download(downloadDate, batchTag).map(EfgsDownload::getNextBatchTag).orElse(null);
			}
		} catch (Exception e) {
			log.error("Exception getting next batch tag ({}): {}", downloadDate, e.getMessage(), e);
			throw new EfgsServerException(EfgsCodeError.DOWNLOAD_DIAGNOSIS_KEYS, String.format("Exception getting next batch tag (%s): %s", 
					downloadDate, e.getMessage()));
		}
		return nextBatchTag;
	}

}
