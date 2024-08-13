package de.elomagic.dttool;

import de.elomagic.dttool.configuration.Configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public abstract class AbstractMockedServer {

    private ClientAndServer mockServer;

    protected Integer getPort() {
        return mockServer.getPort();
    }

    @BeforeEach
    public void beforeEach() {
        Configuration.INSTANCE.load();
        mockServer = startClientAndServer();
    }

    @AfterEach
    public void afterEach() {
        mockServer.stop();
        Configuration.INSTANCE.load();
    }

}
