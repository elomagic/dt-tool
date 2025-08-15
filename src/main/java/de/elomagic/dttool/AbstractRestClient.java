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
import picocli.CommandLine;

import de.elomagic.dttool.configuration.Configuration;

import org.jspecify.annotations.NonNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;

public abstract class AbstractRestClient {

    private static final String APPLICATION_JSON = "application/json";
    private static final ConsolePrinter LOGGER = ConsolePrinter.INSTANCE;

    private final ObjectMapper objectMapper = JsonMapperFactory.create();

    @CommandLine.Option(names = { "--apiKey", "-k" }, description = "DTrack API Key")
    String apiKey = Configuration.INSTANCE.getApiKey();
    @CommandLine.Option(names = { "--disableTlsCheck" }, description = "Disable TLS certificate check", negatable = true)
    boolean ignoreCertificate;

    private HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            HttpClient.Builder builder = HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS);

            if (ignoreCertificate) {
                try {
                    var sslContext = SSLContext.getInstance("TLS");
                    var trustManager = new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }

                        @SuppressWarnings("squid:S4830")
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // noop
                        }

                        @SuppressWarnings("squid:S4830")
                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // noop
                        }
                    };

                    sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

                    builder.sslContext(sslContext);
                } catch (KeyManagementException | NoSuchAlgorithmException ex) {
                    throw new DtToolException(ex.getMessage());
                }
            }

            httpClient = builder.build();
        }

        return httpClient;
    }

    private HttpRequest.@NonNull Builder createDefaultRequest(@NonNull URI uri) {
        return HttpRequest
                .newBuilder(uri)
                .setHeader("X-Api-Key", apiKey)
                .timeout(Duration.ofSeconds(60));
    }

    @NonNull
    protected HttpRequest createDefaultGET(@NonNull URI uri, @NonNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri);

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .GET()
                .build();
    }

    @NonNull
    protected HttpRequest createDefaultDELETE(@NonNull URI uri, @NonNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri);

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .DELETE()
                .build();
    }

    @NonNull
    protected HttpRequest createDefaultPUT(@NonNull URI uri, HttpRequest.@NonNull BodyPublisher publisher, @NonNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri).header("Content-Type", APPLICATION_JSON);

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .PUT(publisher)
                .build();
    }

    @NonNull
    protected HttpRequest createDefaultPOST(@NonNull URI uri, HttpRequest.@NonNull BodyPublisher publisher, @NonNull String... headers) {
        HttpRequest.Builder builder = createDefaultRequest(uri)
                .header("Content-Type", APPLICATION_JSON)
                .header("Accept",APPLICATION_JSON);

        if (headers.length != 0) {
            builder = builder.headers(headers);
        }

        return builder
                .POST(publisher)
                .build();
    }

    @NonNull
    protected String executeRequest(@NonNull HttpRequest request) throws IOException, InterruptedException {
        HttpClient client = getHttpClient();
        try {
            LOGGER.debug("Executing HTTP {} to {}", request.method(), request.uri());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            LOGGER.debug("Responses HTTP status {}", response.statusCode());
            if (!List.of(200, 204, 302).contains(response.statusCode())) {
                throw new DtToolException("Unexpected HTTP status code %s: Body=%s".formatted(response.statusCode(), response.body()));
            }
            return response.body();
        } catch (Exception ex) {
            LOGGER.error("Error on url request '{}' occurred.", request.uri());
            throw ex;
        }
    }

    /**
     * Execute a HTTP request and the result JSON body will be mapped into a given class type.
     *
     * @param request HTTP to be requested
     * @param classType Class type to be mapped
     * @return Returns instance of class type
     * @param <T> Class type to be mapped
     * @throws IOException Thrown if an I/ O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException Thrown if the operation is interrupted
     */
    @NonNull
    protected <T> T executeRequest(@NonNull HttpRequest request, @NonNull Class<? extends T> classType) throws IOException, InterruptedException {
        try {
            String content = executeRequest(request);

            LOGGER.trace("HTTP response body={}", content);

            return objectMapper.readValue(content, classType);
        } catch (Exception ex) {
            LOGGER.error("Error on url request '{}' occurred.", request.uri());
            throw ex;
        }
    }

}
