# minijpa-metamodel-generator
Metamodel classes can be generated using the '*org.minijpa.jpa.metamodel.generator.MetamodelGenerator*' procedure with the following parameters:

1. persistence unit name
2. persistence xml file path
3. destination folder


Example:

java -cp minijpa-metamodel-generator/target/minijpa-metamodel-generator-0.0.1-SNAPSHOT.jar:"/Users/myuser/workspace/minijpa/minijpa-core/target/test-classes" org.minijpa.jpa.metamodel.generator.MetamodelGenerator mapped_superclass /Users/myuser/workspace/minijpa/minijpa-core/src/test/resources/META-INF/persistence.xml /Users/myuser/workspace/minijpa/minijpa-core/src/test/java
