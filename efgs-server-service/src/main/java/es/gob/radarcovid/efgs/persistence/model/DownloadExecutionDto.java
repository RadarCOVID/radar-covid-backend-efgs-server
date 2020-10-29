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

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DownloadExecutionDto implements Serializable {
	
	private Long id;
	
	private Long batchId;
	
	private LocalDateTime createdAt;
	
	private LocalDate downloadDate;
	
	private String batchTag;
	
    private String nextBatchTag;

    private String message;
	
	private boolean success;
	
}
