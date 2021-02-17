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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "t_visited")
@IdClass(VisitedEntityId.class)
@Data
@EqualsAndHashCode(of = {"exposedId", "country"})
public class VisitedEntity implements Serializable {

    @Id
    @Column(name = "pfk_exposed_id")
    private Integer exposedId;

    @Id
    @Column(name = "country")
    private String country;
    
    @ManyToOne
    @JoinColumn(name = "pfk_exposed_id", insertable = false, updatable = false)
    private GaenExposedEntity gaenExposed;

}
