/*
 * DT-Tool
 * Copyright (c) 2024-present Carsten Rambow
 * mailto:developer AT elomagic DOT de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.elomagic.dttool.commands;

import picocli.CommandLine;

import de.elomagic.dttool.ConsoleOptions;
import de.elomagic.dttool.configuration.Configuration;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create-configuration", description = "Create configuration file")
public class CreateConfigurationFileCommand implements Callable<Void> {

    @CommandLine.Mixin
    private ConsoleOptions consoleOptions;

    @Override
    public Void call() throws Exception {
        Configuration.INSTANCE.createTemplate();

        return null;
    }

}
