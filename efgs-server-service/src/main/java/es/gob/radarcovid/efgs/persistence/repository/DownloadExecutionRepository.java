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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.gob.radarcovid.efgs.persistence.entity.DownloadExecutionEntity;

@Repository
public interface DownloadExecutionRepository extends JpaRepository<DownloadExecutionEntity, Long> {

	Optional<DownloadExecutionEntity> findFirstBySuccessOrderByDownloadDateDescCreatedAtDesc(boolean success);

	@Modifying
	@Query("delete from DownloadExecutionEntity where id in "
			+ "(select d.id from DownloadExecutionEntity d where d.batchJobExecution.startTime<:date)")
	int deleteByBatchJobExecutionStartTime(@Param("date") LocalDateTime date);

}
