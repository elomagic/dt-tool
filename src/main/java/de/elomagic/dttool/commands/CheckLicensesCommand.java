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

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nonnull;
import picocli.CommandLine;

import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.ConsoleUtils;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.configuration.model.PatchRule;
import de.elomagic.dttool.dt.model.Component;
import de.elomagic.dttool.dt.model.Project;
import de.elomagic.dttool.spdx.SpdxLicenseManager;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "check-licenses", description = "Check or patch unset licenses")
public class CheckLicensesCommand extends AbstractProjectFilterCommand implements Callable<Void>  {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    private final SpdxLicenseManager spdx = SpdxLicenseManager.create();

    @CommandLine.Option(names = { "-p", "--patch" }, description = "Patch invalid licenses")
    private boolean patch;
    @CommandLine.Option(names = { OptionsParams.BATCH_MODE, OptionsParams.BATCH_MODE_SHORT }, description = "In non-interactive (batch)")
    private boolean batchMode;

    public Void call() {

        LOGGER.info("Project name filter: {}", projectFilterOptions.getProjectFilter());
        LOGGER.info("Configured patch rules: {}", Configuration.getPatchRules().size());

        Set<Component> unset = fetchProjectsUnsetComponentsLicenseId();

        unset.forEach(c -> LOGGER.info(
                "Component '{}' of project {} version {} has not license ID.",
                c.getPurl(),
                c.getProject() == null ? "unknown" : c.getProject().getName(),
                c.getProject() == null ? "???" : c.getProject().getVersion()
            ));

        LOGGER.always("Found {} components with unset license IDs", unset.size());

        if (unset.isEmpty() || !patch) {
            return null;
        }

        boolean confirm = batchMode || ConsoleUtils.confirmByUser("Patch component license ID, enter YES", "YES");

        if (confirm) {
            LOGGER.info("Init SPDX database");
            spdx.loadDefaults();

            patchComponents(unset);
        }

        return null;

    }

    @Nonnull
    private Optional<PatchRule> containsRule(@Nonnull String purl) {
        return Configuration
                .getPatchRules()
                .stream()
                .filter(r -> purl.matches(r.getMatchPurl()))
                .findFirst();
    }

    private void patchComponent(@Nonnull Component component, @Nonnull String licenseId) {

        LOGGER.always("Patching component '{}' with license ID '{}'", component.getPurl(), licenseId);
        try {
            // Patch original JSON string
            ObjectNode root = (ObjectNode) client.fetchComponentAsJson(component);
            root.put("license", licenseId);
            root.remove("licenseExpression");

            // Post patched JSON
            LOGGER.info("Updating component '{}'", component.getPurl());
            Component c = client.updateComponent(root);

            if (c.getResolvedLicense() == null) {
                LOGGER.warn("Failed to update license ID of component '{}'", component.getPurl());
            }
        } catch (Exception ex) {
            throw new DtToolException(ex);
        }

    }

    private void patchComponents(@Nonnull Set<Component> components) {
        LOGGER.info("Validating license IDs in {} patch rules", Configuration.getPatchRules().size());
        Configuration
                .getPatchRules()
                .stream()
                .filter(r -> !spdx.containsId(r.getLicenseId()))
                .findFirst()
                .ifPresent(r -> {
                    throw new DtToolException("License ID '%s' in patch rules '%s' doesn't exist.".formatted(r.getLicenseId(), r.getMatchPurl()));
                });

        components
                .stream()
                .filter(c -> c.getResolvedLicense() == null)
                .forEach(c -> containsRule(c.getPurl())
                        .ifPresentOrElse(
                                r -> patchComponent(c, r.getLicenseId()),
                                () -> LOGGER.always("No patching rule for component '{}' found.", c.getPurl())
                        )
                );
    }

    private boolean filterOnIgnoreConf(Component c) {
        String purl = c.getPurl();

        return Configuration
                .INSTANCE
                .getIgnorePurl()
                .stream()
                .noneMatch(purl::matches);
    }

    private Set<Component> fetchProjectsUnsetComponentsLicenseId() {

        List<Project> projects = fetchProjects(
                getNotBeforeInZonedTime(30),
                getNotAfterInZonedTime(0),
                null);

        if (!projects.isEmpty()) {
            LOGGER.info("Checking license components of {} project versions, this process can take a while.", projects.size());
        }

        Set<Component> unsetComponents = new HashSet<>();
        for (Project project : projects) {
            unsetComponents.addAll(
                    client.fetchComponents(project)
                            .filter(c -> c.getResolvedLicense() == null)
                            .filter(this::filterOnIgnoreConf)
                            .collect(Collectors.toSet())
            );
        }

        return unsetComponents;

    }

}
