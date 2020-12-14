/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import es.gob.radarcovid.efgs.etc.EfgsProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = {"es.gob.radarcovid.efgs.persistence.repository"})
@ComponentScan(basePackages = "es.gob.radarcovid")
@EnableConfigurationProperties(EfgsProperties.class)
public class EfgsApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication
                .exit(SpringApplication.run(EfgsApplication.class, args)));
    }

}
