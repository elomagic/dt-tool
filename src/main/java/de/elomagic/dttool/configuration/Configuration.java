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

import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.configuration.model.PatchRule;
import de.elomagic.dttool.configuration.model.ProjectResult;
import de.elomagic.dttool.configuration.model.Root;

import org.jetbrains.annotations.NotNull;

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

    public static final String DEFAULT_PROJECT_VERSION_MATCH = "^\\d+(\\.\\d+)*(-.*)?-(SNAPSHOT|(b\\d{4}))$";
    public static final String DEFAULT_PROJECT_LATEST_VERSION_MATCH = "^\\d+(\\.\\d+)*(\\-Final)?$";
    public static final int DEFAULT_OLDER_THEN_DAYS = 30;

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
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void loadAlternative(@NotNull Path file) {
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

    public boolean isDelete() {
        return conf.isDelete();
    }

    public void setDelete(boolean value) {
        conf.setDelete(value);
    }

    public String getVersionMatch() {
        return conf.getVersionMatch() == null ? DEFAULT_PROJECT_VERSION_MATCH : conf.getVersionMatch();
    }

    public void setVersionMatch(String value) {
        conf.setVersionMatch(value);
    }

    public int getOlderThenDays() {
        return conf.getOlderThenDays() == null ? DEFAULT_OLDER_THEN_DAYS : conf.getOlderThenDays();
    }

    public void setOlderThenDays(int value) {
        conf.setOlderThenDays(value);
    }

    public boolean isBatchMode() {
        return conf.isBatchMode();
    }

    public void setBatchMode(boolean value) {
        conf.setBatchMode(value);
    }

    public ProjectResult getReturnProperty() {
        return conf.getProjectResult() == null ? ProjectResult.JSON : conf.getProjectResult();
    }

    public void setReturnProperty(@NotNull ProjectResult value) {
        conf.setProjectResult(value);
    }

    public String getLatestVersionMatch() {
        return conf.getVersionMatch() == null ? DEFAULT_PROJECT_LATEST_VERSION_MATCH : conf.getVersionLatestMatch();
    }

    public void setLatestVersionMatch(String value) {
        conf.setVersionLatestMatch(value);
    }

    public boolean isVerbose() {
        return conf.isVerbose();
    }

    public void setVerbose(boolean value) {
        conf.setVerbose(value);
    }

    public void setDebug(boolean value) {
        conf.setDebug(value);
    }

    public Set<String> getIgnorePurl() { return conf.getIgnorePurl(); }

    public static Set<PatchRule> getPatchRules() {
            return INSTANCE.conf.getPatchRules() == null ? Set.of() : INSTANCE.conf.getPatchRules();
    }

    public static boolean isPatchMode() {
        return INSTANCE.conf.isPatchMode();
    }

    public static void setPatchMode(boolean value) {
        INSTANCE.conf.setPatchMode(value);
    }

    @NotNull
    public static Set<String> getProjectFilter() {
        return INSTANCE.conf.getProjectFilter();
    }

    public static void setProjectFilter(String projectFilter) {
        INSTANCE.conf.setProjectFilter(projectFilter);
    }

}
