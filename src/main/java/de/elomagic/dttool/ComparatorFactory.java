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

import jakarta.annotation.Nonnull;

import de.elomagic.dttool.model.Project;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.Comparator;

public final class ComparatorFactory {

    private ComparatorFactory() {}

    @Nonnull
    public static Comparator<Project> create() {
        return Comparator.comparing((Project o) -> new ComparableVersion(o.getVersion())).reversed();
    }

}
