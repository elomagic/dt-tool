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
package de.elomagic.dttool.model;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Project {

    private String name;
    private String version;
    private String uuid;
    private String lastBomImport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Nullable
    public ZonedDateTime getLastBomImport() {
        if (lastBomImport == null || lastBomImport.isBlank()) {
            return null;
        }

        // Do of a bug in the REST API of DT, w e have to differ between epoch time and zoned date time
        if (lastBomImport.matches("[0-9]+")) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(lastBomImport));
            return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else {
            return ZonedDateTime.parse(lastBomImport);
        }
    }

}
