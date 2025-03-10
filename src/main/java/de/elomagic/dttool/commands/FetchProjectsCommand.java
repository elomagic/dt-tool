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

import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.configuration.model.ProjectResult;
import de.elomagic.dttool.model.Project;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(name = "fetch-projects", description = "Fetch projects")
public class FetchProjectsCommand extends AbstractProjectFilterCommand implements Callable<Void> {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

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

    @Override
    public Void call() {
        fetchProjectsByName()
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
    private Stream<Project> fetchProjectsByName() {
        return fetchProjects(versionMatch).stream();
    }
}
