package de.elomagic.dttool;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class AppTest {

    @Test
    void testHelp() {
        App.main(new String[] { "-help" });
    }

    @Test
    void testProjectCare() throws IOException {
        App.main(new String[] { "-pc", "-v" });

//        String content = Files.readString(Path.of("./junit.log"), StandardCharsets.UTF_8);
//        assertTrue(content.contains("Patching pkg:maven/org.glassfish.jersey.core/jersey-client@2.41"), "At least one licence are not patched");
//        assertFalse(content.contains("\"expression\""), "Some or all expressions not patched");
    }

}