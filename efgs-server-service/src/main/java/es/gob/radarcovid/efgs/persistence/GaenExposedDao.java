/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto;

public interface GaenExposedDao {

	@Transactional(readOnly=true)
    List<GaenExposedDto> findPendingByCountry(String country, Integer page, Integer size);

    @Transactional
    void updateBatchTagForKeys(Collection<GaenExposedDto> originalKeys, String batchTag);
    
    @Transactional
    int saveAll(List<GaenExposedDto> gaenExposedDtos);
    
}
