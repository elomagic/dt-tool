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

import de.elomagic.dttool.ConsoleUtils;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.model.Project;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete-projects", description = "Delete projects")
public class DeleteProjectCommand extends AbstractProjectFilterCommand implements Callable<Void> {

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

        List<Project> projects = fetchProjects(
                getNotBeforeInZonedTime(40 * 365),
                getNotAfterInZonedTime(30),
                versionMatch);

        if (projects.isEmpty()) {
            return null;
        }

        boolean confirm = batchMode || ConsoleUtils.confirmByUser("Delete projects, enter YES", "YES");

        if (confirm) {
            projects.forEach(p -> client.deleteProject(p));
        }

        return null;
    }

}
