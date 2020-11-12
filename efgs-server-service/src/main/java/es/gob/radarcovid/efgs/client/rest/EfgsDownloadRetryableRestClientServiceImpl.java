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
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.gob.radarcovid.efgs.client.EfgsDownloadClientService;
import es.gob.radarcovid.efgs.client.model.EfgsDownload;
import es.gob.radarcovid.efgs.client.model.EfgsMediaTypeEnum;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.federationgateway.utils.CertUtils;
import eu.interop.federationgateway.model.EfgsProto;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsDownloadRetryableRestClientServiceImpl implements EfgsDownloadClientService {

	private final EfgsProperties efgsProperties;
	private final RestTemplate restTemplate;

	@Override
	@Retryable(maxAttemptsExpression = "#{${application.efgs.download-diagnosis-keys.download.retry.max-attempts:1}}", 
		backoff = @Backoff(delayExpression = "#{${application.efgs.download-diagnosis-keys.download.retry.delay:100}}"))
	public Optional<EfgsDownload> download(LocalDate date, String batchTag) {
		log.debug("Entering EfgsDownloadRetryableRestClientServiceImpl.download('{}', '{}')", date, batchTag);
		
        X509Certificate certificate = CertUtils.loadCertificateFromFile(efgsProperties.getCredentials().getAuthentication().getCertificate());

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(EfgsMediaTypeEnum.PROTOBUF_MEDIA_TYPE.toMediaType(efgsProperties.getContentNegotiation().getProtobufVersion())));
        headers.set(efgsProperties.getCertAuth().getHeaderFields().getThumbprint(), CertUtils.getCertThumbprint(certificate));
        headers.set(efgsProperties.getCertAuth().getHeaderFields().getDistinguishedName(), "C=" + efgsProperties.getCountry());
		if (batchTag != null) {
			headers.set(Constants.HEADER_BATCH_TAG, batchTag);
		}

		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(efgsProperties.getDownloadDiagnosisKeys().getDownload().getUrl()).path("/")
				.path(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

		Optional<EfgsDownload> result = Optional.empty();
		try {
			ResponseEntity<EfgsProto.DiagnosisKeyBatch> response = restTemplate.exchange(urlBuilder.toUriString(),
					HttpMethod.GET, httpEntity, EfgsProto.DiagnosisKeyBatch.class);

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				DiagnosisKeyBatch diagnosisKeyBatch = response.getBody() != null ? response.getBody() : DiagnosisKeyBatch.newBuilder().build();
				EfgsDownload.EfgsDownloadBuilder builder = EfgsDownload.builder().diagnosisKeyBatch(diagnosisKeyBatch)
						.batchTag(response.getHeaders().getFirst(Constants.HEADER_BATCH_TAG));
				String nextBatchTag = response.getHeaders().getFirst(Constants.HEADER_NEXTBATCHTAG);
				if (StringUtils.hasLength(nextBatchTag) && !"null".equalsIgnoreCase(nextBatchTag)) {
					builder.nextBatchTag(nextBatchTag);
				}
				result = Optional.of(builder.build());
			}

		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
				log.warn("Exception invoking download ({}): {}", ex.getStatusCode(), ex.getMessage(), ex);
				throw ex;
			}
		}
		log.debug("Leaving EfgsDownloadRetryableRestClientServiceImpl.download() with: {} results", result.map(d -> d.getDiagnosisKeyBatch().getKeysList().size()).orElse(0));
		return result;
	}

}
