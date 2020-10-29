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

import com.google.protobuf.ExtensionRegistry;

public class ProtobufEfgsHttpMessageConverter extends ProtobufHttpMessageConverter {

	public ProtobufEfgsHttpMessageConverter() {
		this(null, null);
	}

	public ProtobufEfgsHttpMessageConverter(ExtensionRegistry extensionRegistry) {
		this(null, extensionRegistry);
	}

	public ProtobufEfgsHttpMessageConverter(ProtobufFormatSupport formatSupport, ExtensionRegistry extensionRegistry) {
		super(new ProtobufEfgsFormatSupport(), extensionRegistry);
	}

}
