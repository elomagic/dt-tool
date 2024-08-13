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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class Project {

    @JsonProperty
    private UUID uuid;
    @JsonProperty
    private String name;
    @JsonProperty
    private String version;
    @JsonProperty
    private String lastBomImport;
    @JsonProperty
    private String purl;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public UUID getUuid() {
        return uuid;
    }

    @JsonIgnore
    @Nullable
    public ZonedDateTime getLastBomImport() {
        if (lastBomImport == null || lastBomImport.isBlank()) {
            return null;
        }

        // Do of a bug in the REST API of DT, w e have to differ between epoch time and zoned date time
        if (lastBomImport.matches("[\\d]+")) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(lastBomImport));
            return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else {
            return ZonedDateTime.parse(lastBomImport);
        }
    }

    public String getPurl() {
        return purl;
    }

}
