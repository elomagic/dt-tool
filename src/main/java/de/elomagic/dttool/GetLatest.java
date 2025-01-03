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

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Nonnull;

import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.configuration.model.ProjectResult;
import de.elomagic.dttool.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class GetLatest {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final DTrackClient client = new DTrackClient();

    @Nonnull
    public Optional<String> getLatest(@Nonnull String projectName) {
        return fetchProjectByName(projectName, Configuration.INSTANCE.getLatestVersionMatch())
                .sorted(ComparatorFactory.create())
                .map(p -> mapToString(p, Configuration.INSTANCE.getReturnProperty()))
                .findFirst();
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
    private Stream<Project> fetchProjectByName(@Nonnull String projectName, @Nonnull String regExVersionMatch) {

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
                    .filter(p -> p.getVersion().matches(regExVersionMatch))
                    .toList()
            );

            size = pageResult.size();
        } while (size > 0);

        return projects.stream();
    }
}
