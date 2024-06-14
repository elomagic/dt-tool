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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class Configuration {

    private static final Path CONFIG_FILE = Path.of(
            System.getProperty("user.home"),
            ".license-patcher",
            "configuration.properties");
    private static final Logger LOGGER = LogManager.getLogger(Configuration.class);
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

            properties.forEach((key, value) -> LOGGER.debug("Configuration: {}={}", key, key.equals("apiKey") ? "???" : value + ""));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void createTemplate() throws IOException {
        if (Files.exists(CONFIG_FILE)) {
            LOGGER.warn("Configuration file '{}' already exists.", CONFIG_FILE);
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
        return properties.getProperty("versionMatch", "\\d+\\.\\d+\\.\\d+\\.\\d+-SNAPSHOT");
    }

    public void setVersionMatch(String value) {
        properties.setProperty("versionMatch", value);
    }

    public int getOlderThenDays() {
        return Integer.parseInt(properties.getProperty("olderThenDays", "30"));
    }

    public void setOlderThenDays(int value) {
        properties.setProperty("olderThenDays", Integer.toString(value));
    }

}
