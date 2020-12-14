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

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.gob.radarcovid.efgs.persistence.entity.GaenExposedEntity;

@Repository
public interface GaenExposedEntityRepository extends PagingAndSortingRepository<GaenExposedEntity, Integer>, GaenExposedEntityJdbcRepository {

    List<GaenExposedEntity> findAllByCountryOriginAndEfgsSharingIsTrueAndBatchTagIsNull(String countryOrigin, Pageable pageable);
    
    List<GaenExposedEntity> findByCountryOriginAndEfgsSharingAndBatchTag(String countryOrigin, boolean efgsSharing, String batchTag);
    
    @Modifying
    @Query("update GaenExposedEntity set batchTag = :batchTag where key = :key")
    void updateBatchTag(@Param("key") String key, @Param("batchTag") String batchTag);

}
