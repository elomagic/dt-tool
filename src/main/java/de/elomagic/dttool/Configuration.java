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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class Configuration {

    private static final Path CONFIG_FILE = Path.of(
            System.getProperty("user.home"),
            ".dt-tool",
            "configuration.properties");

    public static final String DEFAULT_PROJECT_VERSION_MATCH = "^\\d+(\\.\\d+)*(-.*)?-SNAPSHOT$";
    public static final String DEFAULT_PROJECT_LATEST_VERSION_MATCH = "^\\d+(\\.\\d+)*(\\-Final)?$";
    public static final int DEFAULT_OLDER_THEN_DAYS = 30;

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final Properties properties = new Properties();

    public static final Configuration INSTANCE = new Configuration();

    private Configuration() {
        if (Files.notExists(CONFIG_FILE)) {
            LOGGER.info("Configuration file '{}' not found.", CONFIG_FILE);
            LOGGER.info("Tip. Create configuration template by using option '--createConfig'.");
            return;
        }

        LOGGER.debug("Loading configuration from '{}'.", CONFIG_FILE);
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            properties.load(reader);

            LOGGER.setVerbose(isVerbose());

            properties.forEach((key, value) -> LOGGER.debug("Configuration: {}={}", key, key.equals("apiKey") ? "???" : value + ""));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void createTemplate() throws IOException {
        if (Files.exists(CONFIG_FILE)) {
            LOGGER.error("Configuration file '{}' already exists.", CONFIG_FILE);
            return;
        }

        Files.createDirectories(CONFIG_FILE.getParent());

        try (InputStream in = Configuration.class.getResourceAsStream("/configuration-template.properties")) {
            Files.copy(in, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void loadAlternative(@NotNull Path file) {
        LOGGER.info("Loading alternative configuration from '{}'.", file);
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public String getBaseUrl() {
        return properties.getProperty("baseUrl");
    }

    public void setBaseUrl(String baseUrl) {
        properties.setProperty("baseUrl", baseUrl);
    }

    public String getApiKey() {
        return properties.getProperty("apiKey");
    }

    public void setApiKey(String apiKey) {
        properties.setProperty("apiKey", apiKey);
    }

    public boolean isDelete() {
        return Boolean.parseBoolean(properties.getProperty("delete", "false"));
    }

    public void setDelete(boolean value) {
        properties.setProperty("delete", Boolean.toString(value));
    }

    public String getVersionMatch() {
        return properties.getProperty("versionMatch", DEFAULT_PROJECT_VERSION_MATCH);
    }

    public void setVersionMatch(String value) {
        properties.setProperty("versionMatch", value);
    }

    public int getOlderThenDays() {
        return Integer.parseInt(properties.getProperty("olderThenDays", Integer.toString(DEFAULT_OLDER_THEN_DAYS)));
    }

    public void setOlderThenDays(int value) {
        properties.setProperty("olderThenDays", Integer.toString(value));
    }

    public boolean isBatchMode() {
        return Boolean.parseBoolean(properties.getProperty("batchMode", "false"));
    }

    public void setBatchMode(boolean value) {
        properties.setProperty("batchMode", Boolean.toString(value));
    }

    public ProjectResult getReturnProperty() {
        return ProjectResult.valueOf(properties.getProperty("projectResult", "JSON"));
    }

    public void setReturnProperty(@NotNull ProjectResult value) {
        properties.setProperty("projectResult", value.name());
    }

    public String getLatestVersionMatch() {
        return properties.getProperty("versionLatestMatch", DEFAULT_PROJECT_LATEST_VERSION_MATCH);
    }

    public void setLatestVersionMatch(String value) {
        properties.setProperty("versionLatestMatch", value);
    }

    public boolean isVerbose() {
        return Boolean.parseBoolean(properties.getProperty("verbose", "false"));
    }

    public void setVerbose(boolean value) {
        properties.setProperty("verbose", Boolean.toString(value));
    }

}
