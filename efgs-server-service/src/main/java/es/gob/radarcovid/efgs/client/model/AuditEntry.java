/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package es.gob.radarcovid.efgs.client.model;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditEntry {

  private String country;
  private ZonedDateTime uploadedTime;
  private String uploaderCertificate;
  private String uploaderThumbprint;
  private String uploaderOperatorSignature;
  private String signingCertificate;
  private String uploaderSigningThumbprint;
  private String signingCertificateOperatorSignature;
  private long amount;
  private String batchSignature;

}
