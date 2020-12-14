/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.repository;

import java.time.LocalDateTime;
import java.util.Set;

public interface GaenExposedEntityJdbcRepository {
	
	void saveOnConflictUpdate(String key, int rollingStartNumber, int rollingPeriod, int transmissionRiskLevel,
			LocalDateTime receivedAt, String countryOrigin, int reportType, int daysSinceOnset, boolean efgsSharing,
			String batchTag, Set<String> visitedCountries);

}
