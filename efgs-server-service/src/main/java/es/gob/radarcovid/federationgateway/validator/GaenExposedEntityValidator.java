/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.federationgateway.validator;

import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GaenExposedEntityValidator {

    private static final String VALIDATION_FAILED_MESSAGE = "Validation of diagnosis key {} failed: {}";
    private static final int ROLLING_START_INTERVAL_LENGTH = 600;

    public static final int KEY_DATA_LENGTH = 16;
    public static final int MIN_ROLLING_PERIOD = 0;
    public static final int MAX_ROLLING_PERIOD = 144;
    public static final int MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS = -14;
    public static final int MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS = 4000;
    public static final int MIN_TRANSMISSION_RISK_LEVEL = 0;
    public static final int MAX_TRANSMISSION_RISK_LEVEL = 8;
    //public static final int ISO_COUNTRY_CODE_LENGTH = 2;

    // this value will be used if a correct TransmissionRiskLevel cannot be provided
    private static final int TRL_DEFAULT_VALUE = 0x7fffffff;

    public static boolean isValid(GaenExposedEntity entity) {

        long minimumRollingStart = Instant.now()
                .truncatedTo(ChronoUnit.DAYS)
                .minus(15, ChronoUnit.DAYS)
                .getEpochSecond() / ROLLING_START_INTERVAL_LENGTH;

        long maximumRollingStart = Instant.now()
                .getEpochSecond() / ROLLING_START_INTERVAL_LENGTH;
        maximumRollingStart += 1;

        String key = entity.getKey();
        byte[] keyData = Base64.getDecoder().decode(key);

        if (StringUtils.isEmpty(key)) {
            return fail(key, "The key is empty or null");

        } else if (keyData.length != KEY_DATA_LENGTH) {
            return fail(key, "The key is not 16 bytes.");

        } else if (entity.getRollingStartNumber() < minimumRollingStart
                || entity.getRollingStartNumber() > maximumRollingStart) {
            return fail(key, "Invalid rolling start interval number.");

        } else if (entity.getRollingPeriod() < MIN_ROLLING_PERIOD || entity.getRollingPeriod() > MAX_ROLLING_PERIOD) {
            return fail(key, "Invalid rolling period.");

        } else if ((entity.getTransmissionRiskLevel() < MIN_TRANSMISSION_RISK_LEVEL || entity.getTransmissionRiskLevel() > MAX_TRANSMISSION_RISK_LEVEL)
                && entity.getTransmissionRiskLevel() != TRL_DEFAULT_VALUE) {
            return fail(key, "Invalid transmission risk level");
        //} else if (entity.getDaysSinceOnset() < MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS || entity.getDaysSinceOnset() > MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS) {
        //    return fail("Invalid days since onset of symptoms.", key);
        }

        log.debug("Successful validation of diagnosis key: {}", key);
        return true;
    }

    private static boolean fail(String key, String reason) {
        log.warn(VALIDATION_FAILED_MESSAGE, key, reason);
        return false;
    }

}