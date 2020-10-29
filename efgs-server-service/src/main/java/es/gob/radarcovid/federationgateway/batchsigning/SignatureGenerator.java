/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package es.gob.radarcovid.federationgateway.batchsigning;

import es.gob.radarcovid.common.security.KeyVault;
import es.gob.radarcovid.efgs.etc.Constants;
import es.gob.radarcovid.federationgateway.utils.CertUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Slf4j
public class SignatureGenerator {

    private final SignerInfoGenerator signerInfo;
    private final X509CertificateHolder certificateHolder;
    private final CMSSignedDataGenerator signedDataGenerator;

    public SignatureGenerator(KeyVault keyVault, String certificateFileName)
            throws OperatorCreationException, CertificateEncodingException, CMSException, IOException {

        X509Certificate certificate = CertUtils.loadCertificateFromFile(certificateFileName);
        PrivateKey privateKey = keyVault.get(Constants.PAIR_KEY_RADARCOVID).getPrivate();

        DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder().build();
        ContentSigner contentSigner =
                new JcaContentSignerBuilder(certificate.getSigAlgName()).build(privateKey);
        signerInfo = new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider).build(contentSigner, certificate);
        certificateHolder = new X509CertificateHolder(certificate.getEncoded());
        signedDataGenerator = new CMSSignedDataGenerator();

        signedDataGenerator.addSignerInfoGenerator(signerInfo);
        signedDataGenerator.addCertificate(certificateHolder);
    }

    /**
     * Returns signature of a batch of bytes with certificate from constructor.
     *
     * @param data byte array with data.
     * @return Base64 encoded string with signature
     */
    public String getSignatureForBytes(final byte[] data) throws CMSException, IOException {
        CMSSignedData signedData = signedDataGenerator.generate(new CMSProcessableByteArray(data), false);
        return Base64.getEncoder().encodeToString(signedData.getEncoded());
    }

}
