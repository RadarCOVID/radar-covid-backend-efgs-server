/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.springframework.http.converter.protobuf;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter.ProtobufFormatSupport;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

public class ProtobufEfgsFormatSupport implements ProtobufFormatSupport {

	public static final MediaType EFGS_PROTOBUF = new MediaType("application", "protobuf", StandardCharsets.UTF_8);

	@Override
	public MediaType[] supportedMediaTypes() {
		return new MediaType[] { ProtobufHttpMessageConverter.PROTOBUF, TEXT_PLAIN, APPLICATION_JSON, EFGS_PROTOBUF };
	}

	@Override
	public boolean supportsWriteOnly(MediaType mediaType) {
		return false;
	}

	@Override
	public void merge(InputStream input, Charset charset, MediaType contentType, ExtensionRegistry extensionRegistry,
			Builder builder) throws IOException, HttpMessageConversionException {

		if (contentType.isCompatibleWith(EFGS_PROTOBUF)) {
			builder.mergeFrom(input, extensionRegistry);
		} else {
			throw new HttpMessageConversionException("protobuf-efgs does not support parsing " + contentType);
		}
	}

	@Override
	public void print(Message message, OutputStream output, MediaType contentType, Charset charset)
			throws IOException, HttpMessageConversionException {

		if (contentType.isCompatibleWith(EFGS_PROTOBUF)) {
			CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(output);
			message.writeTo(codedOutputStream);
			codedOutputStream.flush();
		} else {
			throw new HttpMessageConversionException("protobuf-efgs does not support printing " + contentType);
		}

	}

}
