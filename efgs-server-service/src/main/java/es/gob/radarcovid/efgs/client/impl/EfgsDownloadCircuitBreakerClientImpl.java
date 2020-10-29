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
import java.util.Optional;

import es.gob.radarcovid.efgs.client.EfgsDownloadClientService;
import es.gob.radarcovid.efgs.client.model.EfgsDownload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EfgsDownloadCircuitBreakerClientImpl implements EfgsDownloadClientService {

    private final EfgsDownloadClientService efgsDownloadClientService;

    @Override
    public Optional<EfgsDownload> download(LocalDate date, String batchTag) {
        log.debug("Entering EfgsDownloadCircuitBreakerClientImpl.download()");
        Optional<EfgsDownload> result = efgsDownloadClientService.download(date, batchTag);
        log.debug("Leaving EfgsDownloadCircuitBreakerClientImpl.download() with: {} results", 
        		result.map(d -> d.getDiagnosisKeyBatch().getKeysList().size()).orElse(0));
        return result;
    }

}
