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
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity;

@Repository
public interface GaenExposedEntityRepository extends PagingAndSortingRepository<GaenExposedEntity, Integer> {

    List<GaenExposedEntity> findAllByCountryOriginAndEfgsSharingIsTrueAndBatchTagIsNull(String countryOrigin, Pageable pageable);
    
    List<GaenExposedEntity> findByCountryOriginAndEfgsSharingAndBatchTag(String countryOrigin, boolean efgsSharing, String batchTag);

    @Modifying
    @Query("update GaenExposedEntity set batchTag = :batchTag where key = :key")
    void updateBatchTag(@Param("key") String key, @Param("batchTag") String batchTag);
    
    @Modifying
    @Query(value = "INSERT INTO {h-schema}t_gaen_exposed "
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
    	         + "              batch_tag = :batchTag",
    	   nativeQuery = true)
    int saveUpdateOnConflict(
    	      @Param("key") String key,
    	      @Param("rollingStartNumber") int rollingStartNumber,
    	      @Param("rollingPeriod") int rollingPeriod,
    	      @Param("transmissionRiskLevel") int transmissionRiskLevel,
    	      @Param("receivedAt") LocalDateTime receivedAt,
    	      @Param("countryOrigin") String countryOrigin,
    	      @Param("reportType") int reportType,
    	      @Param("daysSinceOnset") int daysSinceOnset,
    	      @Param("efgsSharing") boolean efgsSharing,
    	      @Param("batchTag") String batchTag);

}
