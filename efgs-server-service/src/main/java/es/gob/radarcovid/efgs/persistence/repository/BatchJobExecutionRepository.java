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

import es.gob.radarcovid.efgs.persistence.entity.BatchJobExecutionEntity;
import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BatchJobExecutionRepository extends JpaRepository<BatchJobExecutionEntity, Long> {

    @Modifying
    @Query("UPDATE BatchJobExecutionEntity b SET b.endTime = :endDate, b.status = :status, b.message = :message WHERE b.id = :id")
    int endBatchJobExecutionById(@Param("id") Long id, @Param("endDate") LocalDateTime endDate,
                                 @Param("message") String message, @Param("status") BatchJobStatusEnum status);

    @Modifying
    @Query("delete from BatchJobExecutionEntity where startTime<:date")
    int deleteByStartTimeBefore(@Param("date") LocalDateTime date);

}
