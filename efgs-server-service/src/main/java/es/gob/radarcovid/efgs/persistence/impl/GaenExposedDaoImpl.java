/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import es.gob.radarcovid.efgs.persistence.GaenExposedDao;
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper;
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto;
import es.gob.radarcovid.efgs.persistence.repository.GaenExposedEntityRepository;
import es.gob.radarcovid.federationgateway.validator.GaenExposedEntityValidator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GaenExposedDaoImpl implements GaenExposedDao {

    private final GaenExposedEntityRepository repository;
    private final GaenExposedMapper mapper;

    @Override
    public List<GaenExposedDto> findPendingByCountry(String country, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByCountryOriginAndEfgsSharingIsTrueAndBatchTagIsNull(country, pageable)
                .stream().filter(GaenExposedEntityValidator::isValid)
                .map(mapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBatchTagForKeys(Collection<GaenExposedDto> originalKeys, String batchTag) {
        originalKeys.forEach(key -> repository.updateBatchTag(key.getKey(), batchTag));
    }

    @Override
    public int saveAll(List<GaenExposedDto> gaenExposedDtos) {
    	AtomicInteger total = new AtomicInteger(0);
    	gaenExposedDtos.stream().forEach(dto -> {
			repository.saveOnConflictUpdate(
					dto.getKey(), 
					dto.getRollingStartNumber(),
					dto.getRollingPeriod(), 
					dto.getTransmissionRiskLevel(), 
					dto.getReceivedAt(), 
					dto.getCountryOrigin(),
					dto.getReportType().getNumber(), 
					dto.getDaysSinceOnset(), 
					dto.getEfgsSharing(), 
					dto.getBatchTag(),
					dto.getVisitedCountries());
			total.addAndGet(1);
    	});
        return total.get();
    }

}
