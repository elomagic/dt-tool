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
import jakarta.annotation.Nullable;
import picocli.CommandLine;

import de.elomagic.dttool.ComparatorFactory;
import de.elomagic.dttool.ConsoleOptions;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.DTrackClient;
import de.elomagic.dttool.ProjectFilterOptions;
import de.elomagic.dttool.model.Project;

import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.List;

public class AbstractProjectFilterCommand {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    @CommandLine.Mixin
    DTrackClient client;
    @CommandLine.Mixin
    ProjectFilterOptions projectFilterOptions;
    @CommandLine.Mixin
    ConsoleOptions consoleOptions;

    @Nonnull
    protected ZonedDateTime getNotAfterInZonedTime(int defaultDays) {
        return ZonedDateTime.now().minusDays(projectFilterOptions.getNotAfterDays() == null ? defaultDays : projectFilterOptions.getNotAfterDays());
    }

    @Nonnull
    protected ZonedDateTime getNotBeforeInZonedTime(int defaultDays) {
        return ZonedDateTime.now().minusDays(projectFilterOptions.getNotBeforeDays() == null ? defaultDays : projectFilterOptions.getNotBeforeDays());
    }

    @Nonnull
    protected List<Project> fetchProjects(@Nonnull ZonedDateTime notBefore, @Nonnull ZonedDateTime notAfter, @Nullable String versionMatchRegEx) {

        LOGGER.info("Matching version with pattern: {}", versionMatchRegEx == null ? "unset" : versionMatchRegEx);
        LOGGER.info("Matching projects with name/uid: {}", projectFilterOptions.getProjectFilter().isEmpty() ? "unset" : projectFilterOptions.getProjectFilter());
        LOGGER.info("Matching projects which not before : {}", notBefore);
        LOGGER.info("Matching projects which not after : {}", notAfter);

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .sorted(ComparatorFactory.defaultComparator())
                .filter(p -> projectFilterOptions.getProjectFilter().isEmpty() || projectFilterOptions.getProjectFilter().contains(p.getName()) || projectFilterOptions.getProjectFilter().contains(p.getUuid().toString()))
                .filter(p -> p.getLastBomImport() == null || notBefore.isBefore(p.getLastBomImport()))
                .filter(p -> p.getLastBomImport() == null || notAfter.isAfter(p.getLastBomImport()))
                .filter(p -> StringUtils.isBlank(versionMatchRegEx) || p.getVersion().matches(versionMatchRegEx))
                .toList();

        projects.forEach(p -> LOGGER.info("{}\t {}\t Created {}", p.getName(), p.getVersion(), p.getLastBomImport()));
        LOGGER.info("{} projects matched ", projects.size());

        return projects;

    }

}
