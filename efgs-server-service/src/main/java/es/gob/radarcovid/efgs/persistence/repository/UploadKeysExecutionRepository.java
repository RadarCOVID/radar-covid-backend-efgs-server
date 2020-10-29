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

import es.gob.radarcovid.efgs.persistence.entity.UploadKeysExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UploadKeysExecutionRepository extends JpaRepository<UploadKeysExecutionEntity, Long> {

	@Modifying
	@Query("delete from UploadKeysExecutionEntity where id in "
			+ "(select u.id from UploadKeysExecutionEntity u where u.batchJobExecution.startTime<:date)")
	int deleteByBatchJobExecutionStartTime(@Param("date") LocalDateTime date);

}
