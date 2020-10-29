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

import eu.interop.federationgateway.model.EfgsProto;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class GaenExposedDto implements Serializable {

    private String key;

    private Integer rollingStartNumber;

    private Integer rollingPeriod;

    private Integer transmissionRiskLevel;

    private LocalDateTime receivedAt;

    private String countryOrigin;

    private Integer reportType;

    private Integer daysSinceOnset;

    private Boolean efgsSharing;

    private String batchTag;

    public EfgsProto.ReportType getReportType() {
        return EfgsProto.ReportType.valueOf(reportType);
    }

    public void setReportType(EfgsProto.ReportType reportType) {
        this.reportType = reportType.getNumber();
    }

}
