/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.client.impl;

import es.gob.radarcovid.efgs.client.EfgsUploadDiagnosisKeysClientService;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.retry.annotation.CircuitBreaker;

@RequiredArgsConstructor
@Slf4j
public class EfgsUploadDiagnosisKeysCircuitBreakerClientServiceImpl implements EfgsUploadDiagnosisKeysClientService {

    private final EfgsUploadDiagnosisKeysClientService client;

    @Override
	@CircuitBreaker(maxAttemptsExpression = "#{${application.efgs.upload-diagnosis-keys.circuit-breaker.max-attempts:3}}", 
		openTimeoutExpression = "#{${application.efgs.upload-diagnosis-keys.circuit-breaker.open-timeout:5000}}")
    public Optional<String> uploadDiagnosisKeys(UploadKeysPayloadDto uploadKeysPayload) {
        log.debug("Entering EfgsUploadDiagnosisKeysCircuitBreakerClientImpl.uploadDiagnosisKeys()");
        Optional<String> result = client.uploadDiagnosisKeys(uploadKeysPayload);
        log.debug("Leaving EfgsUploadDiagnosisKeysCircuitBreakerClientImpl.uploadDiagnosisKeys() with: {}", result);
        return result;
    }
}
