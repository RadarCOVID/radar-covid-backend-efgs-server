/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.gob.radarcovid.efgs.client.model.AuditEntry;

public interface EfgsAuditDownloadClientService {
	
	Optional<List<AuditEntry>> auditDownload(LocalDate date, String batchTag);

}
