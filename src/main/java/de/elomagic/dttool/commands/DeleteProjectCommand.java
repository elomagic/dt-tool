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

import jakarta.annotation.Nonnull;
import picocli.CommandLine;

import de.elomagic.dttool.ConsoleOptions;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.ConsoleUtils;
import de.elomagic.dttool.DTrackClient;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.ProjectFilterOptions;
import de.elomagic.dttool.model.Project;

import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(name = "delete-projects", description = "Delete projects")
public class DeleteProjectCommand implements Callable<Void> {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    @CommandLine.Mixin
    private DTrackClient client;
    @CommandLine.Mixin
    private ProjectFilterOptions projectFilterOptions;
    @CommandLine.Mixin
    private ConsoleOptions consoleOptions;
    @CommandLine.Option(
            names = { OptionsParams.BATCH_MODE, OptionsParams.BATCH_MODE_SHORT },
            negatable = true,
            description = "In non-interactive (batch)")
    private boolean batchMode;
    @CommandLine.Option(
            names = {OptionsParams.VERSION_MATCH, OptionsParams.VERSION_MATCH_SHORT },
            description = "Regular expression to match version",
            defaultValue = "^\\d+(\\.\\d+)*(-.*)?-(SNAPSHOT|(b\\d{4}))$"
    )
    private String versionMatch;

    public Void call() {

        List<Project> projects = fetchProject().toList();

        if (projects.isEmpty()) {
            return null;
        }

        boolean confirm = batchMode || ConsoleUtils.confirmByUser("Delete projects (Y/N)", "Y");

        if (confirm) {
            projects.forEach(p -> client.deleteProject(p.getUuid()));
        }

        return null;
    }

    @Nonnull
    private Stream<Project> fetchProject() {

        ZonedDateTime notBefore = ZonedDateTime.now().minusDays(projectFilterOptions.getOlderThenDays());

        LOGGER.info("Version match: {}", versionMatch);
        LOGGER.info("Fetching projects which not older then {} days", projectFilterOptions.getOlderThenDays());

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .filter(p -> projectFilterOptions.getProjectFilter().isEmpty() || projectFilterOptions.getProjectFilter().contains(p.getName()))
                .filter(p -> p.getLastBomImport() == null || notBefore.isAfter(p.getLastBomImport()))
                .filter(p -> StringUtils.isBlank(versionMatch) || p.getVersion().matches(versionMatch))
                .toList();

        projects.forEach(p -> LOGGER.info("{}\t {}\t Created {}", p.getName(), p.getVersion(), p.getLastBomImport()));
        LOGGER.info("{} projects matched ", projects.size());

        return projects.stream();

    }

}
