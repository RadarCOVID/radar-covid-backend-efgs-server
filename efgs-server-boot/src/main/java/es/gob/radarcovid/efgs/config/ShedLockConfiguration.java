/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

import static net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration.builder;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class ShedLockConfiguration {

    private static final String TABLE_NAME = "shedlock";

    @Value("${spring.jpa.properties.hibernate.default_schema:}")
    private String defaultSchema;

    /**
     * Creates a LockProvider for ShedLock.
     *
     * @param dataSource JPA datasource
     * @return LockProvider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        String tablename = (StringUtils.isEmpty(defaultSchema) ? "" : defaultSchema + ".") + TABLE_NAME;
        return new JdbcTemplateLockProvider(builder()
                .withTableName(tablename)
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }

}
