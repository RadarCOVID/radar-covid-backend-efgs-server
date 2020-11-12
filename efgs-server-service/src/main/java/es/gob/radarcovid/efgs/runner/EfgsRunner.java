/*
 * Copyright (c) 2020 Gobierno de España
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package es.gob.radarcovid.efgs.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class EfgsRunner implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		args.getNonOptionArgs().stream().filter(arg -> arg.equals(jobName())).findFirst().ifPresent(arg -> run());
	}

	public abstract String jobName();

	public abstract void run();

}
