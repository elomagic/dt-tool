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
package de.elomagic.dttool;

import picocli.CommandLine;

import de.elomagic.dttool.commands.CreateConfigurationFileCommand;
import de.elomagic.dttool.commands.DeleteProjectCommand;
import de.elomagic.dttool.commands.FetchProjectsCommand;
import de.elomagic.dttool.commands.CheckLicensesCommand;

@CommandLine.Command(versionProvider = VersionProvider.class, name = "dt-tool", description = "Dependency Track Tool")
public class App {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    public static void main( String[] args ) {

        try {
            App app = new App();
            System.exit(app.execute(args));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    public int execute(String[] args) {
        CommandLine commandLine = new CommandLine(this)
                .addSubcommand(new CommandLine.HelpCommand())
                .addSubcommand(new DeleteProjectCommand())
                .addSubcommand(new CheckLicensesCommand())
                .addSubcommand(new FetchProjectsCommand())
                .addSubcommand(new CreateConfigurationFileCommand());

        return commandLine.execute(args);
    }

}
