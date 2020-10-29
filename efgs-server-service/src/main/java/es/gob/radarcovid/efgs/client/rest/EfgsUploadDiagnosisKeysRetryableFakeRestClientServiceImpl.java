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
import es.gob.radarcovid.efgs.persistence.model.UploadKeysPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class EfgsUploadDiagnosisKeysRetryableFakeRestClientServiceImpl implements EfgsUploadDiagnosisKeysClientService {

    @Override
    public Optional<String> uploadDiagnosisKeys(UploadKeysPayloadDto uploadKeysPayload) {
        log.debug("Entering EfgsUploadDiagnosisKeysFakeRetryableRestClientServiceImpl.uploadDiagnosisKeys()");
        Optional<String> result = Optional.of("OK");
        log.debug("Leaving EfgsUploadDiagnosisKeysFakeRetryableRestClientServiceImpl.uploadDiagnosisKeys() with: {}", result);
        return result;
    }

}
