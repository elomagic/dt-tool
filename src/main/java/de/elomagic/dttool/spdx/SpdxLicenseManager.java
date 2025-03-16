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
package de.elomagic.dttool.spdx;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.spdx.model.SpdxLicense;
import de.elomagic.dttool.spdx.model.SpdxLicenses;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpdxLicenseManager {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    private final Map<String, SpdxLicense> idMap = new HashMap<>();
    private final Map<String, SpdxLicense> nameMap = new HashMap<>();

    @Nonnull
    public static SpdxLicenseManager create() {
        return new SpdxLicenseManager();
    }

    private void load(@Nonnull SpdxLicenses licenses) {
        LOGGER.info("Using {} SPDX licenses from version {}", licenses.getLicenses().size(), licenses.getLicenseListVersion());

        idMap.putAll(licenses
                .getLicenses()
                .stream()
                .collect(Collectors.toMap(SpdxLicense::getLicenseId, l -> l)));
        nameMap.putAll(licenses
                .getLicenses()
                .stream()
                .collect(Collectors.toMap(SpdxLicense::getName, Function.identity(), (existing, replacement) -> existing)));
    }

    @Nonnull
    public SpdxLicenseManager loadDefaults() {
        try {
            LOGGER.info("Loading local copy of SPDX licenses");
            String resource = IOUtils.resourceToString(
                    "de/elomagic/dttool/SpdxLicenseList.json5",
                    StandardCharsets.UTF_8,
                    ClassLoader.getSystemClassLoader()
            );

            SpdxLicenses licenses = JsonMapperFactory
                    .create()
                    .readValue(resource, SpdxLicenses.class);

            load(licenses);
        } catch (IOException ex) {
            throw new DtToolException(ex);
        }

        return this;
    }

    public boolean containsIdOrName(@Nullable String id, @Nullable String name) {
        return (id != null && idMap.containsKey(id))
                || (id != null && nameMap.containsKey(id))
                || (name != null && idMap.containsKey(id))
                || (name != null && nameMap.containsKey(id));
    }

    public boolean containsId(@Nullable String id) {
        return id != null && idMap.containsKey(id);
    }

}
