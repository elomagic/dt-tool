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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LatestVersion {

    private static final Logger LOGGER = LogManager.getLogger(LatestVersion.class);
    private final DTrackClient client = new DTrackClient();

    @NotNull
    public Optional<String> getLatestVersion(@NotNull String projectName) {
        return fetchProjectByName(projectName)
                .sorted(ComparatorFactory.create())
                .map(Project::getVersion)
                .findFirst();
    }

    private Stream<Project> fetchProjectByName(@NotNull String projectName) {

        LOGGER.info("Fetching projects with name {}", projectName);

        List<Project> projects = new ArrayList<>();
        int size;
        int page = 0;
        int limit = 1000;

        do {
            page++;

            List<Project> pageResult = client.fetchProjectsByName(projectName, limit, page);

            projects.addAll(pageResult
                    .stream()
                    .filter(p -> p.getName().equals(projectName))
                    .toList()
            );

            size = pageResult.size();
        } while (size > 0);

        return projects.stream();
    }
}
