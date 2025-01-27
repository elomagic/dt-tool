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

import jakarta.annotation.Nonnull;

import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.configuration.model.ProjectResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Consumer;

public class App {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    private static final Option COMMAND_PROJECT_CARE = new Option("pc", "projectCare", false, "List projects which older then x days");
    private static final Option COMMAND_LATEST_VERSION = new Option("l", "latest", false, "Find latest active of projects");
    private static final Option COMMAND_LICENSE_CHECK = new Option("lic", "license", false, "Check SBOM on valid SPDX ID");
    private static final Option COMMAND_CREATE_CONFIG = new Option("cc", "createConfig", false, "Create configuration template");
    private static final Option COMMAND_HELP = new Option("h", "help", false, "Print this message");

    private static final Option OPTION_API_KEY = new Option("k", "apiKey", true, "Dependency Track REST API key");
    private static final Option OPTION_BASE_URL = new Option("u", "baseUrl", true, "Dependency Track base URL");
    private static final Option OPTION_BATCH_MODE = new Option("b", "batchMode", false, "in non-interactive (batch)");
    private static final Option OPTION_DEBUG = new Option("de", "debug", false, "Debug mode");
    private static final Option OPTION_DELETE = new Option("d", "delete", false, "Delete findings");
    private static final Option OPTION_LATEST_VERSION_MATCH = new Option("lvm", "latestVersionMatch", true, "RegEx to match when using --latest. Default " + Configuration.DEFAULT_PROJECT_LATEST_VERSION_MATCH);
    private static final Option OPTION_OLDER_THEN = new Option("otd", "OlderThenDays", true, "Older then days. Default " + Configuration.DEFAULT_OLDER_THEN_DAYS + " days");
    private static final Option OPTION_PATCH = new Option("p", "patch", false, "Patch unset licenses");
    private static final Option OPTION_PROJECT_FILTER = new Option("pf", "projectFilter", true, "Project name or UUID filter");
    private static final Option OPTION_PROJECT_NAME = new Option("pn", "projectName", true, "Project name");
    private static final Option OPTION_RETURN_PROPERTY = new Option("rp", "returnProperty", true, "Which property will be returned when using --latest. Supported values are: JSON, VERSION, UUID");
    private static final Option OPTION_VERBOSE = new Option("v", "verbose", false, "Verbose mode");
    private static final Option OPTION_VERSION_MATCH = new Option("vm", "versionMatch", true, "RegEx to match when using --projectCare. Default " + Configuration.DEFAULT_PROJECT_VERSION_MATCH);

    private static final Option OPTION_CONFIG_FILE = new Option("cf", "configFile", true, "Loads alternative configuration file");

    public static void main( String[] args ) {

        Options options = new Options()
                .addOption(COMMAND_PROJECT_CARE)
                .addOption(COMMAND_LATEST_VERSION)
                .addOption(COMMAND_LICENSE_CHECK)
                .addOption(COMMAND_CREATE_CONFIG)
                .addOption(COMMAND_HELP);

        options.addOption(OPTION_API_KEY)
                .addOption(OPTION_BASE_URL)
                .addOption(OPTION_BATCH_MODE)
                .addOption(OPTION_CONFIG_FILE)
                .addOption(OPTION_DEBUG)
                .addOption(OPTION_DELETE)
                .addOption(OPTION_LATEST_VERSION_MATCH)
                .addOption(OPTION_OLDER_THEN)
                .addOption(OPTION_PATCH)
                .addOption(OPTION_PROJECT_FILTER)
                .addOption(OPTION_PROJECT_NAME)
                .addOption(OPTION_RETURN_PROPERTY)
                .addOption(OPTION_VERBOSE)
                .addOption(OPTION_VERSION_MATCH);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(OPTION_VERBOSE)) {
                Configuration.INSTANCE.setVerbose(true);
                ConsolePrinter.INSTANCE.setVerbose(true);
            }

            if (cmd.hasOption(OPTION_DEBUG)) {
                Configuration.INSTANCE.setDebug(true);
                ConsolePrinter.INSTANCE.setDebug(true);
            }

            applyOptionIfExist(cmd, OPTION_CONFIG_FILE, v -> Configuration.INSTANCE.loadAlternative(Path.of(v)));
            applyOptionIfExist(cmd, OPTION_DELETE, v -> Configuration.INSTANCE.setDelete(true));
            applyOptionIfExist(cmd, OPTION_BASE_URL, Configuration.INSTANCE::setBaseUrl);
            applyOptionIfExist(cmd, OPTION_API_KEY, Configuration.INSTANCE::setApiKey);
            applyOptionIfExist(cmd, OPTION_LATEST_VERSION_MATCH, Configuration.INSTANCE::setLatestVersionMatch);
            applyOptionIfExist(cmd, OPTION_BATCH_MODE, v -> Configuration.INSTANCE.setBatchMode(true));
            applyOptionIfExist(cmd, OPTION_OLDER_THEN, v -> Configuration.INSTANCE.setOlderThenDays(Integer.parseInt(v)));
            applyOptionIfExist(cmd, OPTION_PATCH, v -> Configuration.setPatchMode(true));
            applyOptionIfExist(cmd, OPTION_PROJECT_FILTER, Configuration::setProjectFilter);
            applyOptionIfExist(cmd, OPTION_RETURN_PROPERTY, v -> Configuration.INSTANCE.setReturnProperty(ProjectResult.valueOf(cmd.getOptionValue(OPTION_RETURN_PROPERTY))));
            applyOptionIfExist(cmd, OPTION_VERSION_MATCH, Configuration.INSTANCE::setVersionMatch);

            if (cmd.hasOption(COMMAND_PROJECT_CARE)) {
                ProjectCare projectCare = new ProjectCare();
                projectCare.care();
            } else if (cmd.hasOption(COMMAND_LICENSE_CHECK)) {
                ComponentCare componentCare = new ComponentCare();
                componentCare.care();
            } else if (cmd.hasOption(COMMAND_LATEST_VERSION)) {
                GetLatest lv = new GetLatest();
                lv.getLatest(cmd.getOptionValue(OPTION_PROJECT_NAME))
                        .ifPresent(LOGGER::always);
            } else if (cmd.hasOption(COMMAND_HELP)) {
                printHelp(options);
            } else if (cmd.hasOption(COMMAND_CREATE_CONFIG)) {
                Configuration.INSTANCE.createTemplate();
            } else {
                printHelp(options);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void applyOptionIfExist(@Nonnull CommandLine cmd, @Nonnull Option option, @Nonnull Consumer<String> consumer) {
        if (cmd.hasOption(option.getOpt())) {
            consumer.accept(cmd.getOptionValue(option));
        }
    }

    private static void printHelp(@Nonnull Options options) {
        String version = "unknown";
        try (InputStream in = App.class.getResourceAsStream("/de/elomagic/dttool/meta.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            version = properties.getProperty("version");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        HelpFormatter formatter = new HelpFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // formatter.printHelp("BOM License Patcher v%s".formatted(version), options);
        formatter.printHelp(
                new PrintWriter(out, true),
                formatter.getWidth(),
                "Dependency Track Tool v%s".formatted(version),
                null,
                options,
                formatter.getLeftPadding(),
                formatter.getDescPadding(),
                null
        );

        LOGGER.always(out.toString(StandardCharsets.UTF_8));
    }

}
