/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.test.etc;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("test")
@Getter
@Setter
public class TestProperties {

	private final HashMap<String, ForeignSigningCert> foreignSigningCerts = new HashMap<>();

	@Getter
	@Setter
	public static class ForeignSigningCert {
		private String certificate;
		private String privateKey;
		private String publicKey;
		private String algorithm;
	}

}
