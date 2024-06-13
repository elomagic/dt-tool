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

import de.elomagic.dttool.model.Project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProjectCare {

    private static final Logger LOGGER = LogManager.getLogger(ProjectCare.class);
    private final DTrackClient client = new DTrackClient();

    public void care(@NotNull String regExMatch, int olderThenDays) {

        fetchProject(regExMatch, olderThenDays)
                .filter(p -> !Configuration.INSTANCE.isOnlyValidate())
                .forEach(p -> client.deleteProject(p.getUuid()));

    }

    private Stream<Project> fetchProject(@NotNull String regExMatch, int olderThenDays) {

        ZonedDateTime notBefore = ZonedDateTime.now().minusDays(olderThenDays);

        LOGGER.info("Fetching projects which not older then {} days", notBefore);

        List<Project> projects = new ArrayList<>();
        int size;
        int page = 0;
        int limit = 1000;

        do {
            page++;

            List<Project> pageResult = client.fetchProjects(limit, page);

            projects.addAll(pageResult
                    .stream()
                    .filter(p -> p.getLastBomImport() == null || notBefore.isBefore(p.getLastBomImport()))
                    .toList()
            );

            size = pageResult.size();
        } while (size > 0);

        LOGGER.info("Found {} projects to patch", projects.size());

        List<Project> oldProjects = projects
                .stream()
                .filter(p -> p.getVersion().matches(regExMatch))
                .toList();

        LOGGER.info("{} of {} projects matched ", oldProjects.size(), projects.size());

        return oldProjects.stream();
    }

}
