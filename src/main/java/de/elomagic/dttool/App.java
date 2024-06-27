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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class App {

    private static final Option COMMAND_PROJECT_CARE = new Option("pc", "projectCare", false, "List projects which older then x days");
    private static final Option COMMAND_CREATE_CONFIG = new Option("cc", "createConfig", false, "Create configuration template");
    private static final Option COMMAND_HELP = new Option("h", "help", false, "Print this message");

    private static final Option OPTION_VERSION_MATCH = new Option("vm", "versionMatch", true, "RegEx to match. Default " + Configuration.DEFAULT_PROJECT_VERSION_MATCH);
    private static final Option OPTION_DELETE = new Option("d", "delete", false, "Delete findings");
    private static final Option OPTION_OLDER_THEN = new Option("otd", "OlderThenDays", true, "Older then days. Default " + Configuration.DEFAULT_OLDER_THEN_DAYS + " days");
    private static final Option OPTION_BASE_URL = new Option("b", "baseUrl", true, "Dependency Track base URL");
    private static final Option OPTION_API_KEY = new Option("k", "apiKey", true, "Dependency Track REST API key");

    private static final Option OPTION_CONFIG_FILE = new Option("cf", "configFile", true, "Loads alternative configuration file");

    public static void main( String[] args ) {

        Options options = new Options();
        options.addOption(COMMAND_PROJECT_CARE);
        options.addOption(COMMAND_CREATE_CONFIG);
        options.addOption(COMMAND_HELP);

        options.addOption(OPTION_VERSION_MATCH);
        options.addOption(OPTION_OLDER_THEN);
        options.addOption(OPTION_DELETE);
        options.addOption(OPTION_CONFIG_FILE);
        options.addOption(OPTION_BASE_URL);
        options.addOption(OPTION_API_KEY);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(OPTION_CONFIG_FILE)) {
                Configuration.INSTANCE.loadAlternative(Path.of(cmd.getOptionValue(OPTION_CONFIG_FILE)));
            }
            if (cmd.hasOption(OPTION_DELETE)) {
                Configuration.INSTANCE.setDelete(true);
            }
            if (cmd.hasOption(OPTION_BASE_URL)) {
                Configuration.INSTANCE.setBaseUrl(cmd.getOptionValue(OPTION_BASE_URL));
            }
            if (cmd.hasOption(OPTION_API_KEY)) {
                Configuration.INSTANCE.setApiKey(cmd.getOptionValue(OPTION_API_KEY));
            }
            if (cmd.hasOption(OPTION_VERSION_MATCH)) {
                Configuration.INSTANCE.setVersionMatch(cmd.getOptionValue(OPTION_VERSION_MATCH));
            }

            if (cmd.hasOption(COMMAND_PROJECT_CARE)) {
                ProjectCare projectCare = new ProjectCare();
                projectCare.care();
            } else if (cmd.hasOption(COMMAND_HELP)) {
                printHelp(options);
            } else if (cmd.hasOption(COMMAND_CREATE_CONFIG)) {
                Configuration.INSTANCE.createTemplate();
            } else {
                printHelp(options);
            }
        } catch (Exception e) {
            LogManager.getLogger(App.class).error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printHelp(@NotNull Options options) {
        String version = "unknown";
        try (InputStream in = App.class.getResourceAsStream("/META-INF/maven/de.elomagic/dt-tool/pom.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            version = properties.getProperty("version");
        } catch (Exception ex) {
            LogManager.getLogger(App.class).error(ex.getMessage(), ex);
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BOM License Patcher v%s".formatted(version), options);
    }

}
