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

import picocli.CommandLine;

import de.elomagic.dttool.ComparatorFactory;
import de.elomagic.dttool.ConsoleOptions;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.dt.DTrackClient;
import de.elomagic.dttool.ProjectFilterOptions;
import de.elomagic.dttool.StringFormatter;
import de.elomagic.dttool.dt.model.Project;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public class AbstractProjectFilterCommand implements StringFormatter {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    @CommandLine.Mixin
    DTrackClient client;
    @CommandLine.Mixin
    ProjectFilterOptions projectFilterOptions;
    @CommandLine.Mixin
    ConsoleOptions consoleOptions;

    @NonNull
    protected ZonedDateTime getNotAfterInZonedTime(int defaultDays) {
        return ZonedDateTime.now().minusDays(projectFilterOptions.getNotAfterDays() == null ? defaultDays : projectFilterOptions.getNotAfterDays());
    }

    @NonNull
    protected ZonedDateTime getNotBeforeInZonedTime(int defaultDays) {
        return ZonedDateTime.now().minusDays(projectFilterOptions.getNotBeforeDays() == null ? defaultDays : projectFilterOptions.getNotBeforeDays());
    }

    @NonNull
    protected List<Project> fetchProjects(@NonNull ZonedDateTime notBefore, @NonNull ZonedDateTime notAfter, @Nullable String versionMatchRegEx) {

        LOGGER.info("Matching version with pattern: {}", versionMatchRegEx == null ? "<unset>" : versionMatchRegEx);
        LOGGER.info("Matching projects with name/uid: {}", projectFilterOptions.getProjectFilter().isEmpty() ? "<unset>" : projectFilterOptions.getProjectFilter());
        LOGGER.info("Matching projects which not before: {}", t2s(notBefore));
        LOGGER.info("Matching projects which not after: {}", t2s(notAfter));

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .sorted(ComparatorFactory.defaultComparator())
                .filter(p -> projectFilterOptions.getProjectFilter().isEmpty() || projectFilterOptions.getProjectFilter().contains(p.getName()) || projectFilterOptions.getProjectFilter().contains(p.getUuid().toString()))
                .filter(p -> p.getLastBomImport() == null || notBefore.isBefore(p.getLastBomImport()))
                .filter(p -> p.getLastBomImport() == null || notAfter.isAfter(p.getLastBomImport()))
                .filter(p -> matchVersion(p, versionMatchRegEx))
                .toList();

        int minimumNameWidth = projects
                .stream()
                .filter(p -> p.getName() != null)
                .mapToInt(p -> p.getName().length())
                .max()
                .orElse(0);

        int minimumVersionWidth = projects
                .stream()
                .filter(p -> p.getVersion() != null)
                .mapToInt(p -> p.getVersion().length())
                .max()
                .orElse(0);

        projects.forEach(p ->
                LOGGER.info(
                        "{}\t {}\t Created {}",
                        mnw(p.getName(), minimumNameWidth),
                        mnw(p.getVersion(), minimumVersionWidth),
                        t2s(p.getLastBomImport())));

        LOGGER.info("{} projects matched ", projects.size());

        return projects;

    }

    private boolean matchVersion(@NonNull Project project, @Nullable String versionMatchRegEx) {
        try {
            return StringUtils.isBlank(versionMatchRegEx) || project.getVersion().matches(versionMatchRegEx);
        } catch (Exception e) {
            LOGGER.error("Error matching version '{}' for project {}: {}", versionMatchRegEx, project, e.getMessage(), e);
            return false;
        }
    }

}
