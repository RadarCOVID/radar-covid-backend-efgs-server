/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.client.model;

import java.util.Map;

import org.springframework.http.MediaType;

import lombok.Getter;

public enum EfgsMediaTypeEnum {

	PROTOBUF_MEDIA_TYPE("application", "protobuf"),
	JSON_MEDIA_TYPE("application", "json");

	@Getter
	private final String type;
	
	@Getter
	private final String subtype;

	EfgsMediaTypeEnum(String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
	}
	
	public MediaType toMediaType(String version) {
		return new MediaType(type, subtype, Map.of("version", version));
	}

}
