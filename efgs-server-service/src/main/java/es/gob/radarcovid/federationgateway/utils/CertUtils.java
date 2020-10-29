/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.federationgateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class CertUtils {

    /**
     * Calculates the SHA-256 thumbprint of X509Certificate.
     *
     * @param x509Certificate the certificate the thumbprint should be calculated for.
     * @return 32-byte SHA-256 hash as hex encoded string
     */
    public static String getCertThumbprint(X509Certificate x509Certificate) {
        try {
            return calculateHash(x509Certificate.getEncoded());
        } catch (Exception e) {
            log.error("Could not calculate thumbprint of certificate.");
            return null;
        }
    }

    private static String calculateHash(byte[] data) throws NoSuchAlgorithmException {
        byte[] certHashBytes = MessageDigest.getInstance("SHA-256").digest(data);
        String hexString = new BigInteger(1, certHashBytes).toString(16);

        if (hexString.length() == 63) {
            hexString = "0" + hexString;
        }

        return hexString;
    }

    /**
     * Loads a certificate from resource directory.
     *
     * @param certificateFileName filename of cert
     * @return X509Certificate object holding the certificate
     */
    public static X509Certificate loadCertificateFromFile(String certificateFileName) {
        try {
            PEMParser parser = new PEMParser(new InputStreamReader(readKey(certificateFileName)));
            while (parser.ready()) {
                Object pemContent = parser.readObject();

                if (pemContent instanceof X509CertificateHolder) {
                    JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
                    try {
                        return converter.getCertificate((X509CertificateHolder) pemContent);
                    } catch (CertificateException e) {
                        log.error("Cannot convert Certificate Holder to Certificate: {}", e.getMessage());
                    }
                }
            }
            log.error("Failed to load certificate: Certificate does not contain a certificate");
            return null;
        } catch (IOException e) {
            log.error("Failed to load certificate: {}", e.getMessage());
            return null;
        }
    }

    public static InputStream readKey(String key) throws IOException {
        String keyLoaded = key;
        InputStream in = null;
        if (key.startsWith("classpath:/")) {
            in = new ClassPathResource(key.substring(11)).getInputStream();
        } else if (key.startsWith("file:/")) {
            in = new FileInputStream(key);
        } else {
            in = new ByteArrayInputStream(key.getBytes(UTF_8));
        }
        return in;
    }

}
