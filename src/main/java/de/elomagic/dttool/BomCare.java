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

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.License;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BomCare {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final Pattern expressionOrPattern = Pattern.compile("^\\((?<id1>.*) OR (?<id2>.*)\\)$");
    private final DTrackClient client = new DTrackClient();

    private final Map<Project, Set<String>> results = new HashMap<>();

    public void care() {

        results.clear();

        fetchProjectsWithInvalidLicenseId(Configuration.INSTANCE.getOlderThenDays());

        LOGGER.always("Found " + results.size() + " projects with invalid license IDs");

    }

    private void addToResults(@NotNull Project p, @NotNull Set<String> ids) {
        if (results.containsKey(p)) {
            results.get(p).addAll(ids);
        } else {
            results.put(p, new HashSet<>(ids));
        }
    }

    @NotNull
    private Component resolveLicenseExpression(@NotNull Component c) {
        if (c.getLicenses() == null || c.getLicenses().getExpression() == null){
            return c;
        }

        String expression = c.getLicenses().getExpression().getValue();
        Matcher matcher = expressionOrPattern.matcher(expression);

        if (matcher.find()) {
            // Create 1
            License l1 = new License();
            l1.setId(matcher.group("id1"));

            // Create 2
            License l2 = new License();
            l2.setId(matcher.group("id2"));

            c.getLicenses().setExpression(null);
            c.getLicenses().setLicenses(List.of(l1, l2));
        } else {
            LOGGER.warn("Expressions like '{}' currently not supported.", expression);
        }

        return c;
    }

    private boolean checkPresentLicenses(@NotNull Project project, @NotNull Component c) {
        if (c.getLicenses() == null
                || (c.getLicenses().getExpression() == null && c.getLicenses().getLicenses().isEmpty())) {
            LOGGER.always(
                    "Component '{}' of project '{} {}' has no license information set",
                    c.getPurl(),
                    project.getName(),
                    project.getVersion()
            );

            addToResults(project, Set.of());

            return false;
        }

        if (c.getLicenses().getExpression() != null) {
            LOGGER.always(
                    "License expression '{}' of component '{}' of project '{} {}' currently not supported.",
                    c.getLicenses().getExpression().getValue(),
                    c.getPurl(),
                    project.getName(),
                    project.getVersion()

            );

            addToResults(project, Set.of(c.getLicenses().getExpression().getValue()));

            return false;
        }

        return true;
    }

    private boolean filterOnIgnoreConf(Component c) {
        String purl = c.getPurl();

        return Configuration
                .INSTANCE
                .getConf()
                .getIgnorePurl()
                .stream()
                .noneMatch(purl::matches);
    }

    private void fetchProjectsWithInvalidLicenseId(int olderThenDays) {

        LOGGER.info("Init SPDX database");
        SpdxLicenseManager spdx = SpdxLicenseManager
                .create()
                .loadDefaults();

        ZonedDateTime notBefore = ZonedDateTime.now().minusDays(olderThenDays);

        LOGGER.info("Fetching projects which not older then {} days", olderThenDays);

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .filter(p -> p.getLastBomImport() != null && notBefore.isAfter(p.getLastBomImport()))
                .toList();

        LOGGER.info("Checking {} project version since {}", projects.size(), notBefore);

        for (Project project : projects) {
            Optional.of(project)
                .map(p -> client.fetchProjectBom(p.getUuid()))
                .map(Bom::getComponents)
                    .orElse(List.of())
                    .stream()
                    .filter(this::filterOnIgnoreConf)
                    .map(this::resolveLicenseExpression)
                    .filter(c -> checkPresentLicenses(project, c))
                    .flatMap(lc -> lc.getLicenses().getLicenses().stream())
                    .filter(Objects::nonNull)
                    .filter(l -> l.getId() != null)
                    .filter(l -> !spdx.matchIdOrName(l.getId(), l.getName()))
                    .forEach(l -> {
                        addToResults(project, Set.of(l.getId()));
                        LOGGER.always(
                                "Component {} version '{}' of project {} version {} has invalid license ID '{}' set",
                                "?",
                                "?",
                                project.getName(),
                                project.getVersion(),
                                l.getId()
                        );
                    });
        }
    }

}
