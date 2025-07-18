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
package de.elomagic.dttool.dt;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nonnull;
import picocli.CommandLine;

import de.elomagic.dttool.AbstractRestClient;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.StringFormatter;
import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.dt.model.Component;
import de.elomagic.dttool.dt.model.Project;
import de.elomagic.dttool.dt.model.Violation;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.BomParserFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@CommandLine.Command
public class DTrackClient extends AbstractRestClient implements StringFormatter {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    @CommandLine.Option(names = {"--baseUrl", "-u"}, description = "DTrack base URL")
    private String baseURL = Configuration.INSTANCE.getBaseUrl();

    // https://[HOSTNAME]/api/swagger.json

    @Nonnull
    public List<Violation> fetchViolations() {
        try {
            URI uri = URI.create("%s/api/v1/violation?suppressed=false".formatted(baseURL));
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Violation[].class));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    /**
     * Fetch all active projects.
     *
     * @param limit Limit count of projects in the response
     * @param page  Starts from 1
     * @return List of projects
     */
    @Nonnull
    public List<Project> fetchProjects(int limit, int page) {
        try {
            URI uri = URI.create("%s/api/v1/project?excludeInactive=true&limit=%s&page=%s".formatted(baseURL, limit, page));
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Project[].class));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    /**
     * Fetch all active project by internal pagination of {@link this#fetchProjects(int, int)}
     *
     * @return Returns a stream
     */
    @Nonnull
    public List<Project> fetchAllProjects() {
        List<Project> projects = new ArrayList<>();
        int size;
        int page = 0;
        int limit = 1000;

        do {
            page++;
            List<Project> pageResult = fetchProjects(limit, page);
            projects.addAll(pageResult);
            size = pageResult.size();
        } while (size > 0);

        return projects;
    }

    /**
     * Fetch all active projects by name.
     *
     * @param name  Name of the project to fetch
     * @param limit Limit count of projects in the response
     * @param page  Starts from 1
     * @return List of projects
     */
    @Nonnull
    public List<Project> fetchProjectsByName(@Nonnull String name, int limit, int page) {
        try {
            URI uri = URI.create(
                    "%s/api/v1/project?name=%s&excludeInactive=false&limit=%s&page=%s".formatted(
                            baseURL,
                            name,
                            limit,
                            page
                    )
            );
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Project[].class));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    /**
     * Fetch all active project by internal pagination of {@link this#fetchProjects(int, int)}
     *
     * @return Returns a stream
     */
    @Nonnull
    public Stream<Component> fetchComponents(@Nonnull Project project) {
        List<Component> components = new ArrayList<>();
        int size;
        int page = 0;
        int limit = 1000;

        do {
            page++;
            List<Component> pageResult = fetchComponents(project, limit, page);
            components.addAll(pageResult);
            size = pageResult.size();
        } while (size > 0);

        return components.stream();
    }

    @Nonnull
    public List<Component> fetchComponents(@Nonnull Project project, int limit, int page) {
        try {
            LOGGER.info("Fetching components of project {} {} {}", project.getUuid(), project.getName(), project.getVersion());
            URI uri = URI.create("%s/api/v1/component/project/%s?limit=%s&page=%s".formatted(baseURL, project.getUuid(), limit, page));
            HttpRequest request = createDefaultGET(uri);

            return List.of(executeRequest(request, Component[].class));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    @Nonnull
    public JsonNode fetchComponentAsJson(@Nonnull Component component) {
        try {
            LOGGER.info("Fetching component {} {} {}", component.getUuid(), component.getName(), component.getVersion());
            URI uri = URI.create("%s/api/v1/component/%s".formatted(baseURL, component.getUuid()));
            HttpRequest request = createDefaultGET(uri);

            return mapper.readTree(executeRequest(request));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    @Nonnull
    public Component updateComponent(@Nonnull ObjectNode root) {
        try {
            URI uri = URI.create("%s/api/v1/component".formatted(baseURL));

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            String payload = writer.writeValueAsString(root);

            LOGGER.trace("Updating component > HTTP POST body={}", payload);

            HttpRequest request = createDefaultPOST(uri, HttpRequest.BodyPublishers.ofString(payload));

            return executeRequest(request, Component.class);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    @Nonnull
    public JsonNode fetchLicenseAsJson(@Nonnull String licenseId) {
        try {
            LOGGER.info("Fetching license '{}'", licenseId);
            URI uri = URI.create("%s/api/v1/license/%s".formatted(baseURL, URLEncoder.encode(licenseId, StandardCharsets.UTF_8)));
            HttpRequest request = createDefaultGET(uri);

            return mapper.readTree(executeRequest(request));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    @Nonnull
    public Bom fetchProjectBom(@Nonnull Project project) {
        try {
            LOGGER.info("Fetching BOM of project {} {} {}", project.getUuid(), project.getName(), project.getVersion());
            URI uri = URI.create("%s/api/v1/bom/cyclonedx/project/%s?download=false".formatted(baseURL, URLEncoder.encode(project.getUuid().toString(), StandardCharsets.UTF_8)));
            HttpRequest request = createDefaultGET(uri);

            String content = executeRequest(request);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            return BomParserFactory.createParser(bytes).parse(bytes);
        } catch (IOException | InterruptedException | ParseException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    public void updateBom(@Nonnull String bom, @Nonnull String projectName, @Nonnull String projectVersion) {
        try {
            LOGGER.info("Upload BOM for project '{}' and version '{}'", projectName, projectVersion);
            URI uri = URI.create("%s/api/v1/bom".formatted(baseURL));

            String base64 = Base64.getEncoder().encodeToString(bom.getBytes(StandardCharsets.UTF_8));

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
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    public void deleteProject(@Nonnull Project project) {
        try {
            LOGGER.info("Delete project {} {} {} {}", project.getUuid(), t2s(project.getLastBomImport()), project.getName(), project.getVersion());
            URI uri = URI.create("%s/api/v1/project/%s".formatted(baseURL, project.getUuid()));
            HttpRequest request = createDefaultDELETE(uri);

            executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    public void tagProject(@Nonnull Project project, @Nonnull String tag) {
        try {
            LOGGER.info("Tag project {} {} {} {}", project.getUuid(), t2s(project.getLastBomImport()), project.getName(), project.getVersion());
            URI uri = URI.create("%s/api/v1/tag/%s/project".formatted(baseURL, tag));
            String payload = "[ \"%s\" ]".formatted(project.getUuid());
            HttpRequest request = createDefaultPOST(
                    uri,
                    HttpRequest.BodyPublishers.ofString(payload)
            );

            executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }

    public void updateProject(@Nonnull Project project) {
        try {
            LOGGER.info("Update project {} {} {} {}", project.getUuid(), t2s(project.getLastBomImport()), project.getName(), project.getVersion());
            URI uri = URI.create("%s/api/v1/project".formatted(baseURL));

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            String payload = writer.writeValueAsString(project);

            HttpRequest request = createDefaultPOST(
                    uri,
                    HttpRequest.BodyPublishers.ofString(payload)
            );

            executeRequest(request);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DtToolException(ex);
        }
    }
}
