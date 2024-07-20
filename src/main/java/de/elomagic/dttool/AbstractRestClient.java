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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public abstract class AbstractRestClient {

    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;
    private final String apiKey = Configuration.INSTANCE.getApiKey();

    private HttpRequest.Builder createDefaultRequest(@NotNull URI uri) {
        return HttpRequest
                .newBuilder(uri)
                .setHeader("X-Api-Key", apiKey)
                .timeout(Duration.ofSeconds(60));
    }

    protected HttpRequest createDefaultGET(@NotNull URI uri, @NotNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri);

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .GET()
                .build();
    }

    protected HttpRequest createDefaultDELETE(@NotNull URI uri, @NotNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri);


        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .DELETE()
                .build();
    }

    protected HttpRequest createDefaultPUT(@NotNull URI uri, @NotNull HttpRequest.BodyPublisher publisher, @NotNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri).header("Content-Type", "application/json");

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .PUT(publisher)
                .build();
    }

    protected String executeRequest(@NotNull HttpRequest request) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()) {
            LOGGER.debug("Executing HTTP {} to {}", request.method(), request.uri());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!List.of(200, 204, 302).contains(response.statusCode())) {
                throw new DtToolException("Unexpected HTTP status code %s: Body=%s".formatted(response.statusCode(), response.body()));
            }
            return response.body();
        } catch (Exception ex) {
            LOGGER.error("Error on url request '{}' occurred.", request.uri());
            throw ex;
        }
    }

    protected <T> T executeRequest(@NotNull HttpRequest request, Class<T> classType) throws IOException, InterruptedException {
        try {
            String content = executeRequest(request);

            LOGGER.trace("HTTP response body={}", content);

            ObjectMapper objectMapper = JsonMapperFactory.create();
            return objectMapper.readValue(content, classType);
        } catch (Exception ex) {
            LOGGER.error("Error on url request '{}' occurred.", request.uri());
            throw ex;
        }
    }

}
