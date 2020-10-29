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

import eu.interop.federationgateway.model.EfgsProto;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;

/**
 * This class contains the methods to verify a batch signature.
 */
@Slf4j
@Service
public class BatchSignatureVerifier {


    public BatchSignatureVerifier() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Verifies the signature of a batch. The signature is an PKCS#7 object encoded with base64.
     *
     * @param batch                the {@link EfgsProto.DiagnosisKeyBatch} object that corresponds to the batch signature.
     * @param base64BatchSignature the base64-encoded batch signature to be verified.
     * @return true if the batch signature is correct. False otherwise.
     */
    public boolean verify(final EfgsProto.DiagnosisKeyBatch batch, final String base64BatchSignature) {
        final byte[] batchSignatureBytes = BatchSignatureUtils.b64ToBytes(base64BatchSignature);
        if (batchSignatureBytes != null) {
            try {
                final CMSSignedData signedData = new CMSSignedData(getBatchBytes(batch), batchSignatureBytes);
                final SignerInformation signerInfo = getSignerInformation(signedData);

                final X509CertificateHolder signerCert = getSignerCert(signedData.getCertificates(),
                                                                       signerInfo.getSID());

                if (signerCert == null) {
                    log.error("no signer certificate");
                    return false;
                }

                if (!isCertNotExpired(signerCert)) {
                    log.error("signing certificate expired\", certNotBefore=\"{}\", certNotAfter=\"{}",
                              signerCert.getNotBefore(), signerCert.getNotAfter());
                    return false;
                }

                if (!allOriginsMatchingCertCountry(batch, signerCert)) {
                    log.error("different origins\", certNotBefore=\"{}\", certNotAfter=\"{}",
                              signerCert.getNotBefore(), signerCert.getNotAfter());
                    return false;
                }

                if (signerInfo == null) {
                    log.error("no signer information");
                    return false;
                }

                return verifySignerInfo(signerInfo, signerCert);
            } catch (CertificateException | OperatorCreationException | CMSException e) {
                log.error("error verifying batch signature", e);
            }
        }
        return false;
    }

    private boolean allOriginsMatchingCertCountry(EfgsProto.DiagnosisKeyBatch batch, X509CertificateHolder certificate) {
        String country = getCountryOfCertificate(certificate);

        if (country == null) {
            return false;
        } else {
            return batch.getKeysList().stream()
                    .allMatch(key -> key.getOrigin().equals(country));
        }
    }

    private boolean isCertNotExpired(X509CertificateHolder certificate) {
        Date now = new Date();

        return certificate.getNotBefore().before(now)
                && certificate.getNotAfter().after(now);
    }


    private boolean isCertificateSignatureValid(final X509CertificateHolder certificate) {
        try {
            final X509Certificate x509Certificate = toX509Certificate(certificate);
            final Signature signature = createSignatureInstance(x509Certificate);
            signature.initVerify(x509Certificate.getPublicKey());
            signature.update(x509Certificate.getTBSCertificate());
            return signature.verify(x509Certificate.getSignature());
        } catch (NoSuchAlgorithmException | SignatureException | CertificateException | InvalidKeyException
                | NoSuchProviderException e) {
            log.error("Could not verify signature of signing certificate: " + e.getMessage());
            return false;
        }
    }

    private Signature createSignatureInstance(final X509Certificate x509Certificate)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance(x509Certificate.getSigAlgName(), BouncyCastleProvider.PROVIDER_NAME);
    }

    private X509Certificate toX509Certificate(final X509CertificateHolder certificate) throws CertificateException {
        final JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        return converter.getCertificate(certificate);
    }

    private String getCountryOfCertificate(X509CertificateHolder certificate) {
        RDN[] rdns = certificate.getSubject().getRDNs(BCStyle.C);
        if (rdns.length != 1) {
            log.info("Certificate has no valid country attribute");
            return null;
        } else {
            return rdns[0].getFirst().getValue().toString();
        }
    }

    private CMSProcessableByteArray getBatchBytes(EfgsProto.DiagnosisKeyBatch batch) {
        return new CMSProcessableByteArray(BatchSignatureUtils.generateBytesToVerify(batch));
    }

    private SignerInformation getSignerInformation(final CMSSignedData signedData) {
        final SignerInformationStore signerInfoStore = signedData.getSignerInfos();

        if (signerInfoStore.size() > 0) {
            return signerInfoStore.getSigners().iterator().next();
        }
        return null;
    }

    private X509CertificateHolder getSignerCert(final Store<X509CertificateHolder> certificatesStore,
                                                final SignerId signerId) {
        final Collection certCollection = certificatesStore.getMatches(signerId);

        if (!certCollection.isEmpty()) {
            return (X509CertificateHolder) certCollection.iterator().next();
        }
        return null;
    }

    private boolean verifySignerInfo(final SignerInformation signerInfo, final X509CertificateHolder signerCert)
            throws CertificateException, OperatorCreationException, CMSException {
        return signerInfo.verify(createSignerInfoVerifier(signerCert));
    }

    private SignerInformationVerifier createSignerInfoVerifier(final X509CertificateHolder signerCert)
            throws OperatorCreationException, CertificateException {
        return new JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(
                signerCert);
    }

}
