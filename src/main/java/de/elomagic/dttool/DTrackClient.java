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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.elomagic.dttool.model.Component;
import de.elomagic.dttool.model.License;
import de.elomagic.dttool.model.Project;
import de.elomagic.dttool.model.Violation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class DTrackClient extends AbstractRestClient {

    private static final Logger LOGGER = LogManager.getLogger(DTrackClient.class);

    private final String baseURL = Configuration.INSTANCE.getBaseUrl();

    // https://[HOSTNAME]/api/swagger.json

    public List<Violation> fetchViolations() {
        try {
            URI uri = URI.create("%s/api/v1/violation?suppressed=false".formatted(baseURL));
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Violation[].class));
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    /**
     * Fetch all active projects.
     *
     * @param limit Limit count of projects in the response
     * @param page Starts from 1
     * @return List of projects
     */
    public List<Project> fetchProjects(int limit, int page) {
        try {
            URI uri = URI.create("%s/api/v1/project?excludeInactive=true&limit=%s&page=%s".formatted(baseURL, limit, page));
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Project[].class));
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    public Component fetchComponent(@NotNull String uuid) {
        try {
            LOGGER.info("Fetching component #{}", uuid);
            URI uri = URI.create("%s/api/v1/component/%s?includeRepositoryMetaData=false".formatted(baseURL, uuid));
            HttpRequest request = createDefaultGET(uri);

            return executeRequest(request, Component.class);
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    public License fetchLicense(@NotNull String licenseId) {
        try {
            LOGGER.info("Fetching license {}", licenseId);
            URI uri = URI.create("%s/api/v1/license/%s".formatted(baseURL, URLEncoder.encode(licenseId, StandardCharsets.UTF_8)));
            HttpRequest request = createDefaultGET(uri);

            return executeRequest(request, License.class);
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    public String fetchProjectBom(@NotNull String projectUid) {
        try {
            LOGGER.info("Fetching BOM of project '{}'", projectUid);
            URI uri = URI.create("%s/api/v1/bom/cyclonedx/project/%s?download=false".formatted(baseURL, URLEncoder.encode(projectUid, StandardCharsets.UTF_8)));
            HttpRequest request = createDefaultGET(uri);

            return executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    public void updateBom(@NotNull String bom, @NotNull String projectName, @NotNull String projectVersion) {
        try {
            LOGGER.info("Upload BOM for project '{}' and version '{}'", projectName, projectVersion);
            URI uri = URI.create("%s/api/v1/bom".formatted(baseURL));

            String base64 = Base64.getEncoder().encodeToString(bom.getBytes(StandardCharsets.UTF_8));

            ObjectMapper mapper = JsonMapperFactory.create();
            ObjectNode root = mapper.createObjectNode();
            root.put("projectName", projectName)
                    .put("projectVersion", projectVersion)
                    .put("autoCreate", true)
                    .put("bom", base64);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            String payload = writer.writeValueAsString(root);

            HttpRequest request = createDefaultPUT(uri, HttpRequest.BodyPublishers.ofString(payload));

            executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }
    }

    public void deleteProject(@NotNull String uuid) {
        try {
            LOGGER.info("Delete project {}", uuid);
            URI uri = URI.create("%s/api/v1/project/%s".formatted(baseURL, uuid));
            HttpRequest request = createDefaultDELETE(uri);

            executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            throw new DtToolException(ex.getMessage(), ex);
        }

    }


}
