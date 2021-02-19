package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassInspectorTest {

    @Test
    public void mappedSuperclass() throws Exception {
	ClassInspector ci = new ClassInspector();
	List<ManagedData> managedDatas = new ArrayList<>();
	ManagedData managedData = ci.inspect("org.minijpa.jpa.model.TriangleAttrs", new ArrayList<>());
	managedDatas.add(managedData);

	managedData = ci.inspect("org.minijpa.jpa.model.Square", new ArrayList<>());
	managedDatas.add(managedData);

	Assertions.assertNotNull(managedDatas);
	Assertions.assertEquals(2, managedDatas.size());

	ManagedData managedData0 = managedDatas.get(0);
	Assertions.assertNotNull(managedData0.mappedSuperclass);

	ManagedData managedData1 = managedDatas.get(1);
	Assertions.assertNotNull(managedData1.mappedSuperclass);

	ManagedData triangleManagedData = managedData0.getClassName().equals("org.minijpa.jpa.model.TriangleAttrs")
		? managedData0
		: managedData1;
	Assertions.assertNotNull(triangleManagedData);

	Assertions.assertEquals(4, triangleManagedData.getDataAttributes().size());
	List<String> names = triangleManagedData.getDataAttributes().stream().map(d -> d.property.ctField.getName())
		.collect(Collectors.toList());
	CollectionUtils.containsAll(Arrays.asList("primitiveLong", "extraProperties", "extraValues", "longValue"),
		names);

	Assertions.assertEquals(0, triangleManagedData.getEmbeddables().size());

	Assertions.assertEquals(1, triangleManagedData.getMethodInfos().size());
	BMTMethodInfo bmtMethodInfo = triangleManagedData.getMethodInfos().get(0);
	Assertions.assertNotNull(bmtMethodInfo.ctConstructor);
	Assertions.assertEquals(5, bmtMethodInfo.getBmtFieldInfos().size());
    }
}
