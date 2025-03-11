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
package de.elomagic.dttool.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;

import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.configuration.model.PatchRule;
import de.elomagic.dttool.configuration.model.Root;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@SuppressWarnings("squid:S6548")
public final class Configuration {

    private static final Path CONFIG_FILE = Path.of(
            System.getProperty("user.home"),
            ".dt-tool",
            "configuration.json5");

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    public static final Configuration INSTANCE = new Configuration();
    private Root conf = new Root();

    private Configuration() {
        load();
    }

    /**
     * Reset and load try to load the configuration if exists.
     */
    public void load() {
        conf = new Root();

        if (Files.notExists(CONFIG_FILE)) {
            LOGGER.info("Configuration file '{}' not found.", CONFIG_FILE);
            LOGGER.info("Tip. Create configuration template by using option '--createConfig'.");
            return;
        }

        LOGGER.debug("Loading configuration from '{}'.", CONFIG_FILE);
        try {
            ObjectMapper objectMapper = JsonMapperFactory.create();
            conf = objectMapper.readValue(CONFIG_FILE.toFile(), Root.class);
            // TODO properties.forEach((key, value) -> LOGGER.debug("Configuration: {}={}", key, key.equals("apiKey") ? "???" : value + ""));
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

        try (InputStream in = Configuration.class.getResourceAsStream("/configuration-template.json5")) {
            Files.copy(in, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.always("Configuration file '{}' created. Please edit and save it.", CONFIG_FILE);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void loadAlternative(@Nonnull Path file) {
        LOGGER.info("Loading alternative configuration from '{}'.", file);
        ObjectMapper objectMapper = JsonMapperFactory.create();
        try {
            conf = objectMapper.readValue(CONFIG_FILE.toFile(), Root.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public String getBaseUrl() {
        return conf.getBaseUrl();
    }

    public void setBaseUrl(String baseUrl) {
        conf.setBaseUrl(baseUrl);
    }

    public String getApiKey() {
        return conf.getApiKey();
    }

    public void setApiKey(String apiKey) {
        conf.setApiKey(apiKey);
    }

    public Set<String> getIgnorePurl() { return conf.getIgnorePurl(); }

    public static Set<PatchRule> getPatchRules() {
            return INSTANCE.conf.getPatchRules() == null ? Set.of() : INSTANCE.conf.getPatchRules();
    }

}
