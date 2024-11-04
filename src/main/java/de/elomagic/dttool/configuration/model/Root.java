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
package de.elomagic.dttool.configuration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

public class Root {

    private String baseUrl;
    private String apiKey;
    private boolean delete;
    private String versionMatch;
    private boolean debug;
    private boolean verbose;
    private String versionLatestMatch;
    private Integer olderThenDays;
    private boolean batchMode;
    private ProjectResult projectResult;
    private Set<String> ignorePurl = new HashSet<>();
    @JsonProperty("patchRules")
    private Set<PatchRule> patchRules = new HashSet<>();
    private boolean patchMode;
    private String projectFilter;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getVersionMatch() {
        return versionMatch;
    }

    public void setVersionMatch(String versionMatch) {
        this.versionMatch = versionMatch;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getVersionLatestMatch() {
        return versionLatestMatch;
    }

    public void setVersionLatestMatch(String versionLatestMatch) {
        this.versionLatestMatch = versionLatestMatch;
    }

    public Integer getOlderThenDays() {
        return olderThenDays;
    }

    public void setOlderThenDays(Integer olderThenDays) {
        this.olderThenDays = olderThenDays;
    }

    public boolean isBatchMode() {
        return batchMode;
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

    public ProjectResult getProjectResult() {
        return projectResult;
    }

    public void setProjectResult(ProjectResult projectResult) {
        this.projectResult = projectResult;
    }

    public Set<String> getIgnorePurl() {
        return ignorePurl;
    }

    public Set<PatchRule> getPatchRules() {
        return patchRules;
    }

    public boolean isPatchMode() {
        return patchMode;
    }

    public void setPatchMode(boolean patchMode) {
        this.patchMode = patchMode;
    }

    @Nonnull
    public Set<String> getProjectFilter() {
        return projectFilter == null ? Set.of() : Set.of(projectFilter.split(","));
    }

    public void setProjectFilter(String projectFilter) {
        this.projectFilter = projectFilter;
    }

}
