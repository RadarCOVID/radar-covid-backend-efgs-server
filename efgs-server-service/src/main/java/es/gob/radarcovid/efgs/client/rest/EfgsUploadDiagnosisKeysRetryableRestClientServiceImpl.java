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

import es.gob.radarcovid.efgs.client.EfgsUploadDiagnosisKeysClientService;
import es.gob.radarcovid.efgs.client.model.EfgsMediaTypeEnum;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysPayloadDto;
import es.gob.radarcovid.federationgateway.utils.CertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class EfgsUploadDiagnosisKeysRetryableRestClientServiceImpl implements EfgsUploadDiagnosisKeysClientService {

    private final EfgsProperties efgsProperties;
    private final RestTemplate restTemplate;

    @Override
    @Retryable(maxAttemptsExpression = "#{${application.efgs.upload-diagnosis-keys.retry.max-attempts:1}}",
            backoff = @Backoff(delayExpression = "#{${application.efgs.upload-diagnosis-keys.retry.delay:100}}"))
    public Optional<String> uploadDiagnosisKeys(UploadKeysPayloadDto uploadKeysPayload) {
        log.debug("Entering EfgsUploadDiagnosisKeysRetryableRestClientServiceImpl.uploadDiagnosisKeys()");
        X509Certificate certificate = CertUtils.loadCertificateFromFile(efgsProperties.getCredentials().getAuthentication().getCertificate());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(EfgsMediaTypeEnum.JSON_MEDIA_TYPE.toMediaType(efgsProperties.getContentNegotiation().getJsonVersion())));
        headers.setContentType(EfgsMediaTypeEnum.PROTOBUF_MEDIA_TYPE.toMediaType(efgsProperties.getContentNegotiation().getProtobufVersion()));
        headers.set(Constants.HEADER_BATCH_TAG, uploadKeysPayload.getBatchTag());
        headers.set(Constants.HEADER_BATCH_SIGNATURE, uploadKeysPayload.getBatchSignature());
        headers.set(efgsProperties.getSsl().getHeaderFields().getThumbprint(), CertUtils.getCertThumbprint(certificate));
        headers.set(efgsProperties.getSsl().getHeaderFields().getDistinguishedName(), "C=" + efgsProperties.getCountry());

        HttpEntity httpEntity = new HttpEntity(uploadKeysPayload.getDiagnosisKeyBatch(), headers);

        Optional<String> result = Optional.empty();
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(efgsProperties.getUploadDiagnosisKeys().getUrl(), httpEntity, String.class);
            log.debug("responseEntity = {}", responseEntity);
            if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBatchTag = responseEntity.getHeaders().getFirst("batchTag");
                result = (uploadKeysPayload.getBatchTag().equals(responseBatchTag)) ? Optional.of("OK") : Optional.of("KO");
                log.debug("Resultado de la invocación: {}", result);
            } else {
                log.debug("Respuesta no OK: {}", responseEntity == null ? "<null>" : responseEntity.getStatusCode());
            }

        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException invoking EFGS ({}): {}", ex.getStatusCode(), ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.warn("Exception invoking EFGS: {}", ex.getMessage(), ex);
            throw ex;
        }
        log.debug("Leaving EfgsUploadDiagnosisKeysRetryableRestClientServiceImpl.uploadDiagnosisKeys() with: {}", result);
        return result;
    }

}
