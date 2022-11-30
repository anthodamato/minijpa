package org.minijpa.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class ModelValueArrayTest {
    @Test
    public void add() {
        ModelValueArray<String> modelValueArray = new ModelValueArray<>();
        assertTrue(modelValueArray.isEmpty());
        modelValueArray.add("m1", "v1");
        modelValueArray.add("m2", "v2");
        int index = modelValueArray.indexOfModel("m2");
        assertEquals(1, index);

        String m1 = modelValueArray.getModel(0);
        assertEquals("m1", m1);

        List<String> models = modelValueArray.getModels();
        assertEquals("m1", models.get(0));
        assertEquals("m2", models.get(1));

        List<Object> values = modelValueArray.getValues();
        assertEquals("v1", values.get(0));
        assertEquals("v2", values.get(1));

        ModelValueArray<String> modelValueArray2 = new ModelValueArray<>();
        modelValueArray2.add(modelValueArray);
        assertEquals(2, modelValueArray2.size());

        ModelValueArray<String> modelValueArray3 = new ModelValueArray<>(models, values);
        assertEquals(2, modelValueArray3.size());

        Function<String, String> f = s -> s.toLowerCase();
        index = modelValueArray3.indexOfModel(f, "m2");
        assertEquals(1, index);
    }
}
