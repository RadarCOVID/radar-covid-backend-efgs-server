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

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import es.gob.radarcovid.efgs.persistence.model.DownloadExecutionDto;

public interface DownloadExecutionDao {
	
	Optional<DownloadExecutionDto> findLastDownloadSuccess();
	
    @Transactional
    boolean saveDownload(Long batchJobExecutionId, DownloadExecutionDto downloadExecutionDto);

}
