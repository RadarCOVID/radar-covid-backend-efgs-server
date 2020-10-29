/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.etc;

import eu.interop.federationgateway.model.EfgsProto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("application.efgs")
@Getter
@Setter
public class EfgsProperties {

    private int retentionDays = 14;

    private String country;

    private final Credentials credentials = new Credentials();
    private final CertAuth certAuth = new CertAuth();
    private final ContentNegotiation contentNegotiation = new ContentNegotiation();
    private final UploadDiagnosisKeys uploadDiagnosisKeys = new UploadDiagnosisKeys();
    private final DownloadDiagnosisKeys downloadDiagnosisKeys = new DownloadDiagnosisKeys();
    private final CleanBatchJob cleanBatchJob = new CleanBatchJob();

    @Getter
    @Setter
    public static class Credentials {
        private Signing signing = new Signing();
        private Authentication authentication = new Authentication();

        @Getter
        @Setter
        public static class Signing {
            private String certificate;
            private String privateKey;
            private String publicKey;
            private String algorithm;
        }

        @Getter
        @Setter
        public static class Authentication {
            private String certificate;
        }
    }

    @Getter
    @Setter
    public static class CertAuth {
        private final HeaderFields headerFields = new HeaderFields();

        @Getter
        @Setter
        public static class HeaderFields {
            private String thumbprint;
            private String distinguishedName;
        }
    }

    @Getter
    @Setter
    public static class ContentNegotiation {
        private String protobufVersion;
        private String jsonVersion;
    }

    @Getter
    @Setter
    public static class UploadDiagnosisKeys {
        private boolean enabled;
        private List<String> countryList;
        private final Batching batching = new Batching();
        private int maximumUploadBatchSize = 5000;
        private String url;
        private final Retry retry = new Retry();

        private final Default defaultValues = new Default();

        @Getter
        @Setter
        public static class Batching {
            private int timeInterval = 300000;
            private int lockLimit = 1800000;
        }

        @Getter
        @Setter
        public static class Default {
            private int transmissionRiskLevel = 2;
            private EfgsProto.ReportType reportType = EfgsProto.ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
        }

    }
    
    @Getter
    @Setter
    public static class DownloadDiagnosisKeys {
        private boolean enabled;
        private final Batching batching = new Batching();
        private final Download download = new Download();
        private final Audit audit = new Audit();
        private int maximumDownloadNextBatchTag = 500;
        
        @Getter
        @Setter
        public static class Download {
        	private String url;
        	private final Retry retry = new Retry();
        }
        
        @Getter
        @Setter
        public static class Audit {
        	private String url;
        	private final Retry retry = new Retry();
        }
    }
    
    @Getter
    @Setter
    public static class CleanBatchJob {
        private boolean enabled;
        private final Batching batching = new Batching();
        private int retentionMonths = 6;
    }
    
    @Getter
    @Setter
    public static class Batching {
        private String cron;
        private int lockLimit = 1800000;
    }

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts;
        private int delay;
    }

}
