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

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import eu.interop.federationgateway.model.EfgsProto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides help methods used by {@link BatchSignatureVerifier} to verify a batch signature.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchSignatureUtils {

    /**
     * Extracts the information (e.g., keyData, rollingPeriod, origin, etc.) from a {@link EfgsProto.DiagnosisKeyBatch} object,
     * and generates with it a byte stream used to verify the batch signature. The created byte stream has an order
     * defined in the Federation Gateway specification.
     *
     * @param batch the diagnosis key batch, from which the information to generate the bytes to verify are obtained.
     * @return the bytes that will be used to verify the batch signature.
     */
    public static byte[] generateBytesToVerify(final EfgsProto.DiagnosisKeyBatch batch) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        sortBatchByKeyData(batch)
                .forEach(diagnosisKey -> byteArrayOutputStream.writeBytes(generateBytesToVerify(diagnosisKey)));

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Extracts the information (e.g., keyData, rollingPeriod, origin, etc.) from a {@link EfgsProto.DiagnosisKey} object,
     * and generates with it a byte stream used to verify the batch signature for one entity.
     * The created byte stream has an order defined in the Federation Gateway specification.
     *
     * @param diagnosisKey the diagnosis key, from which the information to generate the bytes to verify are obtained.
     * @return the bytes that will be used to verify the key signature.
     */
    public static byte[] generateBytesToVerify(final EfgsProto.DiagnosisKey diagnosisKey) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        writeBytesInByteArray(diagnosisKey.getKeyData(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeIntInByteArray(diagnosisKey.getRollingStartIntervalNumber(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeIntInByteArray(diagnosisKey.getRollingPeriod(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeIntInByteArray(diagnosisKey.getTransmissionRiskLevel(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeVisitedCountriesInByteArray(diagnosisKey.getVisitedCountriesList(),
                                         byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeB64StringInByteArray(diagnosisKey.getOrigin(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeIntInByteArray(diagnosisKey.getReportTypeValue(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        writeIntInByteArray(diagnosisKey.getDaysSinceOnsetOfSymptoms(), byteArrayOutputStream);
        writeSeperatorInArray(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Converts a Base64 string into a byte array.
     *
     * @param batchSignatureBase64 the base64 string of the batch signature.
     * @return the batch signature decoded as byte array. Returns an empty array if conversion failed.
     */
    static byte[] b64ToBytes(final String batchSignatureBase64) {
        return b64ToBytes(batchSignatureBase64.getBytes());
    }

    static byte[] b64ToBytes(final byte[] bytes) {
        try {
            return Base64.getDecoder().decode(bytes);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert base64 to byte array");
            return new byte[0];
        }
    }

    static String bytesToBase64(byte[] bytes) {
        try {
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert byte array to string");
            return null;
        }
    }

    private static List<EfgsProto.DiagnosisKey> sortBatchByKeyData(EfgsProto.DiagnosisKeyBatch batch) {
        return batch.getKeysList()
                .stream()
                .sorted(Comparator.comparing(diagnosisKey -> bytesToBase64(generateBytesToVerify(diagnosisKey))))
                .collect(Collectors.toList());
    }

    private static void writeSeperatorInArray(final ByteArrayOutputStream byteArray) {
        byteArray.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
    }

    private static void writeStringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
        byteArray.writeBytes(batchString.getBytes(StandardCharsets.US_ASCII));
    }

    private static void writeB64StringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
        writeStringInByteArray(bytesToBase64(batchString.getBytes(StandardCharsets.US_ASCII)), byteArray);
    }

    private static void writeIntInByteArray(final int batchInt, final ByteArrayOutputStream byteArray) {
        writeStringInByteArray(bytesToBase64(ByteBuffer.allocate(4).putInt(batchInt).array()), byteArray);
    }

    private static void writeBytesInByteArray(final ByteString bytes, ByteArrayOutputStream byteArray) {
        writeStringInByteArray(bytesToBase64(bytes.toByteArray()), byteArray);
    }

    private static void writeVisitedCountriesInByteArray(final ProtocolStringList countries,
                                                         final ByteArrayOutputStream byteArray) {
        writeB64StringInByteArray(String.join(",", countries), byteArray);
    }

}