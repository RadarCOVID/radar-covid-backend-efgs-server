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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.retry.annotation.CircuitBreaker;

import es.gob.radarcovid.efgs.client.EfgsAuditDownloadClientService;
import es.gob.radarcovid.efgs.client.model.AuditEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsAuditDownloadCircuitBreakerClientImpl implements EfgsAuditDownloadClientService {

    private final EfgsAuditDownloadClientService efgsAuditDownloadClientService;

    @Override
	@CircuitBreaker(maxAttemptsExpression = "#{${application.efgs.download-diagnosis-keys.audit.circuit-breaker.max-attempts:3}}", 
		openTimeoutExpression = "#{${application.efgs.download-diagnosis-keys.audit.circuit-breaker.open-timeout:5000}}")
    public Optional<List<AuditEntry>> auditDownload(LocalDate date, String batchTag) {
        log.debug("Entering EfgsAuditDownloadCircuitBreakerClientImpl.auditDownload('{}', '{}')", date, batchTag);
        Optional<List<AuditEntry>> result = efgsAuditDownloadClientService.auditDownload(date, batchTag);
        log.debug("Leaving EfgsAuditDownloadCircuitBreakerClientImpl.auditDownload with: {} results", result.map(List::size).orElse(0));
        return result;
    }

}
