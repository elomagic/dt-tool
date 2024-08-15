package de.elomagic.dttool;

import de.elomagic.dttool.model.Project;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class ComparatorFactoryTest {

    @Test
    void testCreate() {

        Comparator<Project> c = ComparatorFactory.create();

        Project pa = new Project();
        Project pb = new Project();

        pa.setVersion("1.0.0.b0001");
        pb.setVersion("1.0.0.b0005");
        assertEquals(1, c.compare(pa, pb));

        pa.setVersion("1.0.0.b0005");
        pb.setVersion("1.0.0.b0001");
        assertEquals(-1, c.compare(pa, pb));

        pa.setVersion("1.0.0");
        pb.setVersion("1.0.0-SNAPSHOT");
        assertEquals(-1, c.compare(pa, pb));

    }
}