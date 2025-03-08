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

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Nonnull;
import picocli.CommandLine;

import de.elomagic.dttool.ComparatorFactory;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.DTrackClient;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.ProjectFilterOptions;
import de.elomagic.dttool.configuration.model.ProjectResult;
import de.elomagic.dttool.model.Project;

import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(name = "fetch-projects", description = "Fetch projects")
public class FetchProjectsCommand implements Callable<Void> {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    @CommandLine.Mixin
    private DTrackClient client;
    @CommandLine.Mixin
    private ProjectFilterOptions projectFilterOptions;
    @CommandLine.Option(
            names = { "--format", "-f" },
            description = "Returns format. Supported values are: JSON, VERSION, UUID",
            defaultValue = "JSON")
    private ProjectResult format;
    @CommandLine.Option(
            names = { OptionsParams.VERSION_MATCH, OptionsParams.VERSION_MATCH_SHORT },
            description = "Regular expression to match version",
            defaultValue = "^\\d+(\\.\\d+)*(\\-Final)?$")
    private String versionMatch;
    @CommandLine.Option(names = { "--debug", "-d" }, negatable = true, description = "Debug mode")
    void setDebug(boolean debug) {
        ConsolePrinter.INSTANCE.setDebug(true);
    }
    @CommandLine.Option(names = { "--verbose", "-v" }, negatable = true, description = "Verbose mode")
    void setVerbose(boolean debug) {
        ConsolePrinter.INSTANCE.setVerbose(true);
    }

    @Override
    public Void call() {
        fetchProjectByName()
                .sorted(ComparatorFactory.create())
                .map(p -> mapToString(p, format))
                .limit(projectFilterOptions.getMaxCount())
                .findFirst()
                .ifPresent(LOGGER::always);

        return null;
    }

    @Nonnull
    private String mapToString(@Nonnull Project project, @Nonnull ProjectResult result) {
        try {
            return switch (result) {
                case JSON -> JsonMapperFactory.create().writeValueAsString(project);
                case UUID -> Optional.ofNullable(project.getUuid()).map(UUID::toString).orElse("");
                case VERSION -> project.getVersion();
            };
        } catch (JsonProcessingException ex) {
            throw new DtToolException(ex);
        }
    }

    @Nonnull
    private Stream<Project> fetchProjectByName() {

        LOGGER.info("Fetching projects with name/uid {}", projectFilterOptions.getProjectFilter());

        ZonedDateTime notAfter = ZonedDateTime.now().minusDays(projectFilterOptions.getOlderThenDays());

        return  client
                .fetchAllProjects()
                .stream()
                .filter(p -> projectFilterOptions.getProjectFilter().isEmpty() || projectFilterOptions.getProjectFilter().contains(p.getName()))
                .filter(p -> p.getLastBomImport() == null || notAfter.isAfter(p.getLastBomImport()))
                .filter(p -> StringUtils.isBlank(versionMatch) || p.getVersion().matches(versionMatch));
    }
}
