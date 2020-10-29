/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.persistence.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import es.gob.radarcovid.efgs.persistence.vo.BatchJobStatusEnum;
import lombok.Data;

@Data
public class BatchJobExecutionDto implements Serializable {

    private Long id;

    private String jobName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String message;

    private BatchJobStatusEnum status;

}
