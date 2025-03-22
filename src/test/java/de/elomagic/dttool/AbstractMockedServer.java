package de.elomagic.dttool;

import com.github.tomakehurst.wiremock.WireMockServer;

import de.elomagic.dttool.configuration.Configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class AbstractMockedServer {

    public WireMockServer mockServer;

    protected Integer getPort() {
        return mockServer.port();
    }

    @BeforeEach
    public void beforeEach() {
        Configuration.INSTANCE.load();
        mockServer = new WireMockServer(options().dynamicPort().dynamicHttpsPort());
        mockServer.start();
    }

    @AfterEach
    public void afterEach() {
        mockServer.stop();
        Configuration.INSTANCE.load();
    }

}
