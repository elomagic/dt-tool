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
package de.elomagic.dttool.dt.model;

import java.util.UUID;

/**
 * Example:
 * "resolvedLicense": {
 *     "uuid": "36926739-11fe-4521-b46c-a806f511c1ca",
 *     "name": "Eclipse Public License 1.0",
 *     "licenseId": "EPL-1.0",
 *     "isOsiApproved": true,
 *     "isFsfLibre": true,
 *     "isDeprecatedLicenseId": false,
 *     "isCustomLicense": false
 *   },
 */
public class License {

    private UUID uuid;
    private String name;
    private String licenseId;
    private boolean isOsiApproved;
    private boolean isFsfLibre;
    private boolean isDeprecatedLicenseId;
    private boolean isCustomLicense;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public boolean isOsiApproved() {
        return isOsiApproved;
    }

    public void setOsiApproved(boolean osiApproved) {
        isOsiApproved = osiApproved;
    }

    public boolean isFsfLibre() {
        return isFsfLibre;
    }

    public void setFsfLibre(boolean fsfLibre) {
        isFsfLibre = fsfLibre;
    }

    public boolean isDeprecatedLicenseId() {
        return isDeprecatedLicenseId;
    }

    public void setDeprecatedLicenseId(boolean deprecatedLicenseId) {
        isDeprecatedLicenseId = deprecatedLicenseId;
    }

    public boolean isCustomLicense() {
        return isCustomLicense;
    }

    public void setCustomLicense(boolean customLicense) {
        isCustomLicense = customLicense;
    }

}
