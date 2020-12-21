/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.config;

import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.protobuf.ProtobufEfgsHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import es.gob.radarcovid.efgs.etc.EfgsProperties;
import es.gob.radarcovid.efgs.etc.EfgsProperties.Ssl;

@Configuration
@EnableRetry
public class RestClientConfiguration {

    @Autowired
    EfgsProperties efgsProperties;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder,
			ProtobufEfgsHttpMessageConverter ehmc,
			ProtobufJsonFormatHttpMessageConverter jhmc) throws Exception {
		
		Ssl ssl = efgsProperties.getSsl();
		if (ssl.isEnabled()) {

			SSLContext sslcontext = SSLContexts.custom()
					.loadTrustMaterial(ssl.getTrustStore().getURL(), ssl.getTrustStorePassword().toCharArray())
					.loadKeyMaterial(ssl.getKeyStore().getURL(), ssl.getKeyStorePassword().toCharArray(), ssl.getKeyStorePassword().toCharArray())
					.build();
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext,
					new NoopHostnameVerifier());
			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
			restTemplateBuilder = restTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
		}

		return restTemplateBuilder.additionalMessageConverters(Arrays.asList(ehmc, jhmc)).build();
	}

	@Bean
	ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter() {
		return new ProtobufJsonFormatHttpMessageConverter();
	}

	@Bean
	ProtobufEfgsHttpMessageConverter protobufEfgsHttpMessageConverter() {
		return new ProtobufEfgsHttpMessageConverter();
	}

}
