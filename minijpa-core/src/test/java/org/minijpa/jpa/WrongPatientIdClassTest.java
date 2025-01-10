package org.minijpa.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.persistence.Persistence;
import java.io.IOException;

public class WrongPatientIdClassTest {
    @Test
    public void persist() throws IOException {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            Persistence.createEntityManagerFactory("wrong-idclass", PersistenceUnitProperties.getProperties());
        });
    }
}
