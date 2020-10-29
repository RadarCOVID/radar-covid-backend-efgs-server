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

import es.gob.radarcovid.common.exception.EfgsCodeError;
import es.gob.radarcovid.common.exception.EfgsServerException;
import es.gob.radarcovid.common.security.KeyVault;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;

@Configuration
@Slf4j
public class KeysConfiguration {

    @Autowired
    EfgsProperties efgsProperties;

    @Bean
    KeyVault keyVault() {
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        try {
            var privateKey = KeyVault.loadKey(efgsProperties.getCredentials().getSigning().getPrivateKey());
            var publicKey = KeyVault.loadKey(efgsProperties.getCredentials().getSigning().getPublicKey());

            var radar = new KeyVault.KeyVaultEntry(Constants.PAIR_KEY_RADARCOVID, privateKey, publicKey,
                                                   efgsProperties.getCredentials().getSigning().getAlgorithm());
            log.debug("Loaded radar keys");
            return new KeyVault(radar);
        } catch (KeyVault.PrivateKeyNoSuitableEncodingFoundException | KeyVault.PublicKeyNoSuitableEncodingFoundException | IOException e) {
            log.warn("Error loading keys: {}", e.getMessage(), e);
            throw new EfgsServerException(EfgsCodeError.UNKNOWN, e.getMessage());
        }
    }

    @Bean
    SignatureGenerator signatureGenerator(KeyVault keyVault)
            throws OperatorCreationException, CertificateEncodingException, CMSException, IOException {
        return new SignatureGenerator(keyVault, efgsProperties.getCredentials().getSigning().getCertificate());
    }

}
