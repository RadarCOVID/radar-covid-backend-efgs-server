/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.repository.impl;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import es.gob.radarcovid.efgs.persistence.repository.GaenExposedEntityJdbcRepository;

@Repository
public class GaenExposedEntityJdbcRepositoryImpl implements GaenExposedEntityJdbcRepository {
	
	private static final String INSERT_GAEN_EXPOSED_ON_CONFLICT_UPDATE_SQL = "INSERT INTO {h-schema}t_gaen_exposed "
	        + "       (key, rolling_start_number, rolling_period, transmission_risk_level, received_at, "
	        + "        country_origin, report_type, days_since_onset, efgs_sharing, batch_tag) "
	        + "VALUES (:key, :rollingStartNumber, :rollingPeriod, :transmissionRiskLevel, :receivedAt, "
	        + "        :countryOrigin, :reportType, :daysSinceOnset, :efgsSharing, :batchTag) "
	        + "ON CONFLICT ON CONSTRAINT gaen_exposed_key DO "
	        + "   UPDATE SET rolling_start_number = :rollingStartNumber, "
	        + "              rolling_period = :rollingPeriod, "
	        + "              transmission_risk_level = :transmissionRiskLevel, "
	        + "              report_type = :reportType, "
	        + "              days_since_onset = :daysSinceOnset, "
	        + "              batch_tag = :batchTag "
	        + "RETURNING pk_exposed_id";
	
	private static final String INSERT_VISITED_ON_CONFLICT_DO_NOTHING_SQL = "INSERT INTO {h-schema}t_visited "
	        + "       (pfk_exposed_id, country) "
	        + "VALUES (:exposedId, :country) "
	        + "ON CONFLICT ON CONSTRAINT pk_t_visited DO NOTHING";
	
    @PersistenceContext
    private EntityManager em;

	@Override
	public void saveOnConflictUpdate(String key, int rollingStartNumber, int rollingPeriod, int transmissionRiskLevel,
			LocalDateTime receivedAt, String countryOrigin, int reportType, int daysSinceOnset, boolean efgsSharing,
			String batchTag, Set<String> visitedCountries) {
    	int exposedId = (int) em.createNativeQuery(INSERT_GAEN_EXPOSED_ON_CONFLICT_UPDATE_SQL)
    			.setParameter("key", key)
    			.setParameter("rollingStartNumber", rollingStartNumber)
    			.setParameter("rollingPeriod", rollingPeriod)
    			.setParameter("transmissionRiskLevel", transmissionRiskLevel)
    			.setParameter("receivedAt", receivedAt)
    			.setParameter("countryOrigin", countryOrigin)
    			.setParameter("reportType", reportType)
    			.setParameter("daysSinceOnset", daysSinceOnset)
    			.setParameter("efgsSharing", efgsSharing)
    			.setParameter("batchTag", batchTag)
    			.getSingleResult();
    	
		visitedCountries.forEach(country -> {
			em.createNativeQuery(INSERT_VISITED_ON_CONFLICT_DO_NOTHING_SQL)
				.setParameter("exposedId", exposedId)
				.setParameter("country", country)
				.executeUpdate();
		});
	}
}
