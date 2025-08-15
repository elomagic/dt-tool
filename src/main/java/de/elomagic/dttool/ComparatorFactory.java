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

import de.elomagic.dttool.dt.model.Project;
import de.elomagic.dttool.dto.ReportDTO;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;

public final class ComparatorFactory {

    private ComparatorFactory() {}

    /**
     * Sort a project by name asc and version desc.
     *
     * @return Returns the {@link Comparator<Project>}
     */
    @NonNull
    public static Comparator<Project> defaultComparator() {
        return Comparator.comparing(Project::getName)
                .thenComparing((Project o) -> new ComparableVersion(o.getVersion())).reversed();
    }

    @NonNull
    public static Comparator<ReportDTO> reportComparator() {
        return Comparator.comparing(ReportDTO::flooredBomDate)
                .thenComparing(ReportDTO::projectName);
    }

}
