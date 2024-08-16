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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.configuration.model.PatchRule;
import de.elomagic.dttool.model.Component;
import de.elomagic.dttool.model.Project;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentCare {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final DTrackClient client = new DTrackClient();

    public void care() {

        LOGGER.info("Project name filter: {}", Configuration.getProjectFilter());
        LOGGER.info("Configured patch rules: {}", Configuration.getPatchRules().size());

        Set<Component> unset = fetchProjectsUnsetComponentsLicenseId(Configuration.INSTANCE.getOlderThenDays());

        unset.forEach(c -> LOGGER.info(
                "Component '{}' of project {} version {} has not license ID.",
                c.getPurl(),
                c.getProject() == null ? "unknown" : c.getProject().getName(),
                c.getProject() == null ? "???" : c.getProject().getVersion()
            ));

        LOGGER.always("Found " + unset.size() + " components with unset license IDs");

        if (unset.isEmpty() || !Configuration.isPatchMode()) {
            return;
        }

        boolean confirm = Configuration.INSTANCE.isBatchMode() || confirmByUser("Patch component license ID (Y/N)", "Y");

        if (confirm) {
            patchComponents(unset);
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

    @NotNull
    private Optional<PatchRule> containsRule(@NotNull String purl) {
        return Configuration
                .getPatchRules()
                .stream()
                .filter(r -> purl.matches(r.getMatchPurl()))
                .findFirst();
    }

    private void patchComponent(@NotNull Component component, @NotNull String licenseId) {

        LOGGER.always("Try to patch component '{}' with license ID '{}'", component.getPurl(), licenseId);
        try {
            // Patch original JSON string
            ObjectNode root = (ObjectNode) client.fetchComponentAsJson(component.getUuid());
            JsonNode license =  client.fetchLicenseAsJson(licenseId);
            root.set("resolvedLicense", license);

            // Post patched JSON
            client.updateComponent(root);
        } catch (Exception ex) {
            throw new DtToolException(ex);
        }

    }

    private void patchComponents(@NotNull Set<Component> components) {
        LOGGER.info("Init SPDX database");
        SpdxLicenseManager spdx = SpdxLicenseManager
                .create()
                .loadDefaults();

        LOGGER.info("Validate license ids in {} patch rules", Configuration.getPatchRules());
        Configuration
                .getPatchRules()
                .stream()
                .filter(r -> !spdx.containsId(r.getLicenseId()))
                .findFirst()
                .ifPresent(r -> {
                    throw new DtToolException("License ID '%s' in patch rules '%s' doesn't exist.".formatted(r.getLicenseId(), r.getMatchPurl()));
                });

        for (Component c : components) {
             containsRule(c.getPurl())
                     .ifPresentOrElse(r -> patchComponent(c, r.getLicenseId()), () -> LOGGER.always("No patching rule for component '{}' found.", c.getPurl()));
        }
    }

    private boolean filterOnIgnoreConf(Component c) {
        String purl = c.getPurl();

        return Configuration
                .INSTANCE
                .getIgnorePurl()
                .stream()
                .noneMatch(purl::matches);
    }

    private Set<Component> fetchProjectsUnsetComponentsLicenseId(int notOlderThenDays) {

        ZonedDateTime notBefore = ZonedDateTime.now().minusDays(notOlderThenDays);

        LOGGER.info("Fetching projects which not older then {} days", notOlderThenDays);

        List<Project> projects = client
                .fetchAllProjects()
                .stream()
                .filter(p -> p.getLastBomImport() != null && notBefore.isBefore(p.getLastBomImport()))
                .filter(p -> Configuration.getProjectFilter() == null || p.getName().equals(Configuration.getProjectFilter()) || p.getUuid().toString().equalsIgnoreCase(Configuration.getProjectFilter()))
                .toList();

        if (!projects.isEmpty()) {
            LOGGER.info("Checking components of {} project versions since {}. Please wait, this process can take a while.", projects.size(), notBefore);
        }

        Set<Component> unsetComponents = new HashSet<>();
        for (Project project : projects) {
            unsetComponents.addAll(
                    client.fetchComponents(project.getUuid())
                            .filter(c -> c.getResolvedLicense() == null)
                            .filter(this::filterOnIgnoreConf)
                            .collect(Collectors.toSet())
            );
        }

        return unsetComponents;
    }

}
