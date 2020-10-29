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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import es.gob.radarcovid.efgs.business.DownloadExposedService;
import es.gob.radarcovid.efgs.business.UploadExposedService;
import es.gob.radarcovid.efgs.business.impl.DownloadExposedServiceImpl;
import es.gob.radarcovid.efgs.business.impl.UploadExposedServiceImpl;
import es.gob.radarcovid.efgs.client.EfgsAuditDownloadClientService;
import es.gob.radarcovid.efgs.client.EfgsDownloadClientService;
import es.gob.radarcovid.efgs.client.EfgsUploadDiagnosisKeysClientService;
import es.gob.radarcovid.efgs.client.impl.EfgsAuditDownloadCircuitBreakerClientImpl;
import es.gob.radarcovid.efgs.client.impl.EfgsAuditDownloadClientServiceImpl;
import es.gob.radarcovid.efgs.client.impl.EfgsDownloadCircuitBreakerClientImpl;
import es.gob.radarcovid.efgs.client.impl.EfgsDownloadClientServiceImpl;
import es.gob.radarcovid.efgs.client.impl.EfgsUploadDiagnosisKeysCircuitBreakerClientServiceImpl;
import es.gob.radarcovid.efgs.client.impl.EfgsUploadDiagnosisKeysClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsAuditDownloadRetryableFakeRestClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsAuditDownloadRetryableRestClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsDownloadRetryableFakeRestClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsDownloadRetryableRestClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsUploadDiagnosisKeysRetryableFakeRestClientServiceImpl;
import es.gob.radarcovid.efgs.client.rest.EfgsUploadDiagnosisKeysRetryableRestClientServiceImpl;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.DownloadExecutionDao;
import es.gob.radarcovid.efgs.persistence.GaenExposedDao;
import es.gob.radarcovid.efgs.persistence.UploadKeysExecutionDao;
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper;
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureVerifier;
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator;
import eu.interop.federationgateway.model.EfgsProto.DiagnosisKeyBatch;

@Configuration
public class EfgsConfiguration {

    @Bean("efgsUploadDiagnosisKeysRetryableRestClient")
    @ConditionalOnProperty(name = "application.efgs.upload-diagnosis-keys.simulate", havingValue = "false", matchIfMissing = true)
    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysRetryableRestClient(EfgsProperties efgsProperties,
                                                                                    RestTemplate restTemplate) {
        return new EfgsUploadDiagnosisKeysRetryableRestClientServiceImpl(efgsProperties, restTemplate);
    }

    @ConditionalOnProperty(name = "application.efgs.upload-diagnosis-keys.simulate", havingValue = "true")
    @Bean("efgsUploadDiagnosisKeysRetryableRestClient")
    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysRetryableFakeRestClient() {
        return new EfgsUploadDiagnosisKeysRetryableFakeRestClientServiceImpl();
    }

    @Bean
    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysCircuitBreakerClient(
            @Qualifier("efgsUploadDiagnosisKeysRetryableRestClient")
                    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysClientService) {
        return new EfgsUploadDiagnosisKeysCircuitBreakerClientServiceImpl(efgsUploadDiagnosisKeysClientService);
    }

    @Bean
    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysClient(
            @Qualifier("efgsUploadDiagnosisKeysCircuitBreakerClient")
                    EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysClientService) {
        return new EfgsUploadDiagnosisKeysClientServiceImpl(efgsUploadDiagnosisKeysClientService);
    }

    @Bean
    UploadExposedService uploadExposedService(EfgsProperties efgsProperties, GaenExposedDao gaenExposedDao,
                                              UploadKeysExecutionDao uploadKeysExecutionDao,
                                              SignatureGenerator signatureGenerator,
                                              @Qualifier("efgsUploadDiagnosisKeysClient")
                                              EfgsUploadDiagnosisKeysClientService efgsUploadDiagnosisKeysClientService,
                                              GaenExposedMapper gaenExposedMapper) {
        return new UploadExposedServiceImpl(efgsProperties, signatureGenerator, gaenExposedDao, uploadKeysExecutionDao,
                                            efgsUploadDiagnosisKeysClientService, gaenExposedMapper);
    }

    @ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.simulate", havingValue = "false", matchIfMissing = true)
    @Bean("efgsDownloadRetryableRestClient")
    EfgsDownloadClientService efgsDownloadRetryableRestClient(EfgsProperties efgsProperties, RestTemplate restTemplate) {
        return new EfgsDownloadRetryableRestClientServiceImpl(efgsProperties, restTemplate);
    }

    @Bean
    EfgsDownloadClientService efgsDownloadCircuitBreakerClient(
            @Qualifier("efgsDownloadRetryableRestClient") EfgsDownloadClientService efgsDownloadClientService) {
        return new EfgsDownloadCircuitBreakerClientImpl(efgsDownloadClientService);
    }

    @Bean
    EfgsDownloadClientService efgsDownloadClientService(
            @Qualifier("efgsDownloadCircuitBreakerClient") EfgsDownloadClientService efgsDownloadClientService) {
        return new EfgsDownloadClientServiceImpl(efgsDownloadClientService);
    }

    @Bean
    DownloadExposedService downloadExposedService(EfgsProperties efgsProperties, GaenExposedDao gaenExposedDao,
                                                  DownloadExecutionDao downloadExecutionDao,
                                                  @Qualifier("efgsDownloadClientService")
                                                  EfgsDownloadClientService efgsDownloadClientService,
                                                  @Qualifier("efgsAuditDownloadClientService")
                                                  EfgsAuditDownloadClientService efgsAuditDownloadClientService,
                                                  BatchSignatureVerifier batchSignatureVerifier,
                                                  GaenExposedMapper gaenExposedMapper) {
		return new DownloadExposedServiceImpl(efgsProperties, efgsDownloadClientService, efgsAuditDownloadClientService,
				downloadExecutionDao, gaenExposedDao, batchSignatureVerifier, gaenExposedMapper);
    }
    
    @ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.simulate", havingValue = "false", matchIfMissing = true)
    @Bean("efgsAuditDownloadRetryableRestClient")
    EfgsAuditDownloadClientService efgsAuditDownloadRetryableRestClient(EfgsProperties efgsProperties, RestTemplate restTemplate) {
        return new EfgsAuditDownloadRetryableRestClientServiceImpl(efgsProperties, restTemplate);
    }

    @Bean
    EfgsAuditDownloadClientService efgsAuditDownloadCircuitBreakerClient(
            @Qualifier("efgsAuditDownloadRetryableRestClient") EfgsAuditDownloadClientService efgsAuditDownloadClientService) {
        return new EfgsAuditDownloadCircuitBreakerClientImpl(efgsAuditDownloadClientService);
    }

    @Bean
    EfgsAuditDownloadClientService efgsAuditDownloadClientService(
            @Qualifier("efgsAuditDownloadCircuitBreakerClient") EfgsAuditDownloadClientService efgsAuditDownloadClientService) {
        return new EfgsAuditDownloadClientServiceImpl(efgsAuditDownloadClientService);
    }
    
    @ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.simulate", havingValue = "true")
    @Bean("diaganosisKeyBatchMap")
    Map<String, DiagnosisKeyBatch> diaganosisKeyBatchMap() {
        return new HashMap<>();
    }
    
    @ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.simulate", havingValue = "true")
    @Bean("efgsDownloadRetryableRestClient")
    EfgsDownloadClientService efgsDownloadRetryableFakeRestClient(EfgsProperties efgsProperties,
    		                                                      @Qualifier("diaganosisKeyBatchMap") 
                                                                  Map<String, DiagnosisKeyBatch> diaganosisKeyBatchMap) {
        return new EfgsDownloadRetryableFakeRestClientServiceImpl(efgsProperties, diaganosisKeyBatchMap);
    }
    
    @ConditionalOnProperty(name = "application.efgs.download-diagnosis-keys.simulate", havingValue = "true")
    @Bean("efgsAuditDownloadRetryableRestClient")
    EfgsAuditDownloadClientService efgsAuditDownloadRetryableFakeRestClient(EfgsProperties efgsProperties,
    		                                                                SignatureGenerator signatureGenerator,
    		                                                                @Qualifier("diaganosisKeyBatchMap") 
                                                                            Map<String, DiagnosisKeyBatch> diaganosisKeyBatchMap) {
        return new EfgsAuditDownloadRetryableFakeRestClientServiceImpl(efgsProperties, signatureGenerator, diaganosisKeyBatchMap);
    }

}
