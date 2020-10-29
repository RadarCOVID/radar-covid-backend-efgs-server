/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.entity;

import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "BATCH_JOB_EXECUTION")
@Data
public class BatchJobExecutionEntity implements Serializable {

    private static final String SEQUENCE_NAME = "SQ_NM_ID_BATCH_JOB_EXECUTION";

    public static final int JOB_NAME_MAX_LENGTH = 64;
    public static final int MESSAGE_MAX_LENGTH = 256;

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "NM_JOB_EXECUTION_ID")
    private Long id;

    @Column(name = "DE_JOB_NAME", length = JOB_NAME_MAX_LENGTH)
    private String jobName;

    @Column(name = "FC_START_TIME")
    private LocalDateTime startTime;

    @Column(name = "FC_END_TIME")
    private LocalDateTime endTime;

    @Column(name = "DE_MESSAGE", length = MESSAGE_MAX_LENGTH)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "DE_STATUS")
    private BatchJobStatusEnum status;

}
