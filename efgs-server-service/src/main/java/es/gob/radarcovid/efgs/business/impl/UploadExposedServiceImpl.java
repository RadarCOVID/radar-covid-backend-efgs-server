/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.business.impl;

import es.gob.radarcovid.common.exception.EfgsCodeError;
import es.gob.radarcovid.common.exception.EfgsServerException;
import es.gob.radarcovid.efgs.business.UploadExposedService;
import es.gob.radarcovid.efgs.client.EfgsUploadDiagnosisKeysClientService;
import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.persistence.GaenExposedDao;
import es.gob.radarcovid.efgs.persistence.UploadKeysExecutionDao;
import es.gob.radarcovid.efgs.persistence.mapper.GaenExposedMapper;
import es.gob.radarcovid.efgs.persistence.model.BatchJobExecutionDto;
import es.gob.radarcovid.efgs.persistence.model.GaenExposedDto;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysExecutionDto;
import es.gob.radarcovid.efgs.persistence.model.UploadKeysPayloadDto;
import es.gob.radarcovid.federationgateway.batchsigning.BatchSignatureUtils;
import es.gob.radarcovid.federationgateway.batchsigning.SignatureGenerator;
import eu.interop.federationgateway.model.EfgsProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class UploadExposedServiceImpl implements UploadExposedService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final EfgsProperties efgsProperties;
    private final SignatureGenerator signatureGenerator;
    private final GaenExposedDao gaenExposedDao;
    private final UploadKeysExecutionDao uploadKeysExecutionDao;
    private final EfgsUploadDiagnosisKeysClientService uploadKeysClientService;
    private final GaenExposedMapper gaenExposedMapper;

    @Override
    public void uploadDiagnosisKeys(BatchJobExecutionDto batchJobExecutionDto) {

        log.debug("Executing scheduler task uploadExposedServiceImpl.uploadDiagnosisKeysTask");

        boolean success = uploadPendingDiagnosisKeys(batchJobExecutionDto);

        if (!success)
            throw new EfgsServerException(EfgsCodeError.UPLOAD_DIAGNOSIS_KEYS, "Error uploading keys");

        log.debug("Leaving uploadExposedServiceImpl.uploadDiagnosisKeysTask");
    }

    private boolean uploadPendingDiagnosisKeys(BatchJobExecutionDto batchJobExecutionDto) {

        LocalDateTime date = LocalDateTime.now();
        String batchTagPrefix = "ES-" + date.format(DATE_TIME_FORMATTER) + "-"+ RandomStringUtils.randomAlphanumeric(4) + "-";
        String batchTag = "";
        String batchSignature = "";

        boolean success = true;

        try {
            int counter = 0;
            int page = 0;
            List<GaenExposedDto> gaenExposedDtoList = null;

            do {
                gaenExposedDtoList = gaenExposedDao.findPendingByCountry(efgsProperties.getCountry(), page,
                                                                         efgsProperties.getUploadDiagnosisKeys().getMaximumUploadBatchSize());

                if (!gaenExposedDtoList.isEmpty()) {

                    UploadKeysPayloadDto uploadKeysPayload = new UploadKeysPayloadDto();
                    uploadKeysPayload.setOriginalKeys(gaenExposedDtoList);
                    List<EfgsProto.DiagnosisKey> diagnosisKeyList = new ArrayList<>();
                    gaenExposedDtoList.stream().map(gaenExposedMapper::dtoToDiagnosisKey).forEach(diagnosisKeyList::add);
                    uploadKeysPayload.setDiagnosisKeyBatch(EfgsProto.DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeyList).build());

                    batchTag = batchTagPrefix + page;
                    uploadKeysPayload.setBatchTag(batchTag);

                    uploadKeysPayload.setBatchSignature(signatureGenerator.getSignatureForBytes(
                            BatchSignatureUtils.generateBytesToVerify(uploadKeysPayload.getDiagnosisKeyBatch())));
                    uploadKeysClientService.uploadDiagnosisKeys(uploadKeysPayload);
                    markSuccessfullyUploadedKeys(uploadKeysPayload);

                    int size = uploadKeysPayload.getOriginalKeys().size();

                    UploadKeysExecutionDto uploadKeysExecutionDto = new UploadKeysExecutionDto();
                    uploadKeysExecutionDto.setBatchTag(batchTag);
                    uploadKeysExecutionDto.setBatchSignature(batchSignature);
                    uploadKeysExecutionDto.setBatchId(batchJobExecutionDto.getId());
                    uploadKeysExecutionDto.setUploadDate(LocalDateTime.now());
                    uploadKeysExecutionDto.setMessage("Saved " + size + " keys");
                    uploadKeysExecutionDto.setSuccess(true);
                    uploadKeysExecutionDao.saveUploadKeys(batchJobExecutionDto.getId(), uploadKeysExecutionDto);

                    counter += size;
                    log.debug("Date {} - Page {} - sent {} records", date, page, size);
                    page++;

                } else if (page == 0) {
                    log.info("No records to send: {}", date);
                }
            } while (gaenExposedDtoList != null && !gaenExposedDtoList.isEmpty());
            if (counter > 0 && log.isInfoEnabled())
                log.info("Sent {} total records on {}", counter, date);
        } catch (Exception e) {
            UploadKeysExecutionDto uploadKeysExecutionDto = new UploadKeysExecutionDto();
            uploadKeysExecutionDto.setBatchId(batchJobExecutionDto.getId());
            uploadKeysExecutionDto.setUploadDate(LocalDateTime.now());
            uploadKeysExecutionDto.setMessage(e.getMessage());
            uploadKeysExecutionDto.setSuccess(false);
            uploadKeysExecutionDao.saveUploadKeys(batchJobExecutionDto.getId(), uploadKeysExecutionDto);

            log.error("Exception uploading keys on {}: {}", date, e.getMessage(), e);
            success = false;

        }
        return success;
    }

    private void markSuccessfullyUploadedKeys(UploadKeysPayloadDto uploadKeysPayload) {
        if (!uploadKeysPayload.getOriginalKeys().isEmpty()) {
            gaenExposedDao.updateBatchTagForKeys(uploadKeysPayload.getOriginalKeys(), uploadKeysPayload.getBatchTag());
        }
    }

}