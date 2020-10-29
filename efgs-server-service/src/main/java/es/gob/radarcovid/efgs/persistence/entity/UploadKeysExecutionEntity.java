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

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "UPLOAD_KEYS_EXECUTION")
@Data
public class UploadKeysExecutionEntity implements Serializable {

    private static final String SEQUENCE_NAME = "SQ_NM_UPLOAD_KEYS_EXECUTION";

    public static final int MESSAGE_MAX_LENGTH = 128;

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "NM_UPLOAD_KEYS_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "NM_JOB_EXECUTION_ID")
    private BatchJobExecutionEntity batchJobExecution;

    @Column(name = "FC_UPLOAD_DATE")
    private LocalDateTime uploadDate;

    @Column(name = "DE_BATCH_TAG")
    private String batchTag;

    @Column(name = "DE_BATCH_SIGNATURE")
    private String batchSignature;

    @Column(name = "DE_MESSAGE", length = MESSAGE_MAX_LENGTH)
    private String message;

    @Column(name = "IN_SUCCESS")
    private boolean success;

}
