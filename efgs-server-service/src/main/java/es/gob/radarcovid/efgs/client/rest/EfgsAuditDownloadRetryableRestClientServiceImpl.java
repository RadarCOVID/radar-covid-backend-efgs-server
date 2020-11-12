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

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.gob.radarcovid.efgs.client.EfgsAuditDownloadClientService;
import es.gob.radarcovid.efgs.client.model.AuditEntry;
import es.gob.radarcovid.efgs.client.model.EfgsMediaTypeEnum;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.federationgateway.utils.CertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsAuditDownloadRetryableRestClientServiceImpl implements EfgsAuditDownloadClientService {

	private final EfgsProperties efgsProperties;
	private final RestTemplate restTemplate;

	@Override
	@Retryable(maxAttemptsExpression = "#{${application.efgs.download-diagnosis-keys.audit.retry.max-attempts:1}}", 
		backoff = @Backoff(delayExpression = "#{${application.efgs.download-diagnosis-keys.audit.retry.delay:100}}"))
	public Optional<List<AuditEntry>> auditDownload(LocalDate date, String batchTag) {
		
		log.debug("Entering EfgsAuditDownloadRetryableRestClientServiceImpl.auditDownload('{}', '{}')", date, batchTag);
		
        X509Certificate certificate = CertUtils.loadCertificateFromFile(efgsProperties.getCredentials().getAuthentication().getCertificate());

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(EfgsMediaTypeEnum.JSON_MEDIA_TYPE.toMediaType(efgsProperties.getContentNegotiation().getProtobufVersion())));
        headers.set(efgsProperties.getCertAuth().getHeaderFields().getThumbprint(), CertUtils.getCertThumbprint(certificate));
        headers.set(efgsProperties.getCertAuth().getHeaderFields().getDistinguishedName(), "C=" + efgsProperties.getCountry());

		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(efgsProperties.getDownloadDiagnosisKeys().getAudit().getUrl()).path("/")
				.path(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).path("/").path(batchTag);
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

		Optional<List<AuditEntry>> result = Optional.empty();
		try {
			ResponseEntity<List<AuditEntry>> response = restTemplate.exchange(urlBuilder.toUriString(), HttpMethod.GET,
					httpEntity, new ParameterizedTypeReference<List<AuditEntry>>() {});

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				result = Optional.of(response.getBody());
			}

		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
				log.warn("Exception invoking audit download ({}): {}", ex.getStatusCode(), ex.getMessage(), ex);
				throw ex;
			}
		}
		log.debug("Leaving EfgsAuditDownloadRetryableRestClientServiceImpl.auditDownload() with: {} results", result.map(List::size).orElse(0));
		return result;
	}

}
