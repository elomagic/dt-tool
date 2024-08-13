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

import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.model.Project;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

public class ProjectCare {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final DTrackClient client = new DTrackClient();

    public void care() {

        List<Project> projects = fetchProject(Configuration.INSTANCE.getVersionMatch(), Configuration.INSTANCE.getOlderThenDays()).toList();

        if (projects.isEmpty() || !Configuration.INSTANCE.isDelete()) {
            return;
        }

        boolean confirm = Configuration.INSTANCE.isBatchMode() || confirmByUser("Delete projects (Y/N)", "Y");

        if (confirm) {
            projects.forEach(p -> client.deleteProject(p.getUuid()));
        }
    }

    private boolean confirmByUser(@NotNull String confirmationText, @NotNull String confirmText) {
        Console console = System.console();
        try (PrintWriter w = console.writer()) {
            w.printf("%n%s: ", confirmationText);
            w.flush();
            return confirmText.equalsIgnoreCase(console.readLine());
        }
    }

    private Stream<Project> fetchProject(@NotNull String regExMatch, int olderThenDays) {

        ZonedDateTime notBefore = ZonedDateTime.now().minusDays(olderThenDays);

        LOGGER.info("Version match: {}", Configuration.INSTANCE.getVersionMatch());
        LOGGER.info("Fetching projects which not older then {} days", olderThenDays);

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .filter(p -> p.getLastBomImport() != null && notBefore.isAfter(p.getLastBomImport()))
                .toList();

        List<Project> oldProjects = projects
                .stream()
                .filter(p -> p.getVersion().matches(regExMatch))
                .toList();

        oldProjects.forEach(p -> LOGGER.info("{}\t {}\t Created {}", p.getName(), p.getVersion(), p.getLastBomImport()));
        LOGGER.info("{} of {} projects matched ", oldProjects.size(), projects.size());

        return oldProjects.stream();

    }

}
