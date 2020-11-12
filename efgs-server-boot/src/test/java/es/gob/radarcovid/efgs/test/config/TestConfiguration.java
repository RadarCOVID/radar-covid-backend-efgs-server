/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.test.config;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.gob.radarcovid.common.exception.EfgsCodeError;
import es.gob.radarcovid.common.exception.EfgsServerException;
import es.gob.radarcovid.common.security.KeyVault;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.efgs.test.etc.TestProperties;
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(TestProperties.class)
@Slf4j
public class TestConfiguration {

	@Autowired
	TestProperties testProperties;

	@Bean
	Map<String, SignatureGenerator> foreignSignatureGenerators() {
		Map<String, SignatureGenerator> generators = new HashMap<>();

		testProperties.getForeignSigningCerts().forEach((country, signingCert) -> {
			Security.addProvider(new BouncyCastleProvider());
			Security.setProperty("crypto.policy", "unlimited");

			try {
				var privateKey = KeyVault.loadKey(signingCert.getPrivateKey());
				var publicKey = KeyVault.loadKey(signingCert.getPublicKey());

				var radar = new KeyVault.KeyVaultEntry(Constants.PAIR_KEY_RADARCOVID, privateKey, publicKey, signingCert.getAlgorithm());
				log.debug("Loaded radar keys");
				KeyVault keyVault = new KeyVault(radar);
				generators.put(country, new SignatureGenerator(keyVault, signingCert.getCertificate()));
			} catch (KeyVault.PrivateKeyNoSuitableEncodingFoundException | KeyVault.PublicKeyNoSuitableEncodingFoundException | IOException 
					| CertificateEncodingException | OperatorCreationException | CMSException e) {
				log.warn("Error loading signature generators: {}", e.getMessage(), e);
				throw new EfgsServerException(EfgsCodeError.UNKNOWN, e.getMessage());
			}
		});
		return generators;
	}

}
