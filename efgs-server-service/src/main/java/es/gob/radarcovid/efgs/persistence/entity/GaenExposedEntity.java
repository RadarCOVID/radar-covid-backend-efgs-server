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

import eu.interop.federationgateway.model.EfgsProto;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_gaen_exposed")
@Data
public class GaenExposedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_exposed_id")
    private Integer id;

    @NotNull
    @Column(name = "key", length = 24, nullable = false)
    private String key;

    @Column(name = "rolling_start_number")
    private Integer rollingStartNumber;

    @Column(name = "rollingPeriod")
    private Integer rollingPeriod;

    @Column(name = "transmission_risk_level")
    private Integer transmissionRiskLevel;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "country_origin")
    private String countryOrigin;

    @Column(name = "report_type")
    private Integer reportType;

    @Column(name = "days_since_onset")
    private Integer daysSinceOnset;

    @Column(name = "efgs_sharing")
    private Boolean efgsSharing;

    @Column(name = "batch_tag")
    private String batchTag;
    
    @OneToMany(mappedBy = "gaenExposed")
    private Set<VisitedEntity> visitedCountries = new HashSet<>();

    public EfgsProto.ReportType getReportType() {
        return EfgsProto.ReportType.valueOf(reportType);
    }

    public void setReportType(EfgsProto.ReportType reportType) {
        this.reportType = reportType.getNumber();
    }

}
