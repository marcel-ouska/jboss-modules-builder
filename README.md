# JBoss Modules Builder

## About
Jboss modules builder creates a structure for jboss/wildfly module layers.
It helps to automate the modules building by defining the layers using yaml configuration file.
The plugin creates a folder structure in a way so that it can be placed straight into the jboss_home directory using replace and merge.
The plugin also downloads all necessary artifacts using maven.

## Usage

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.codenuity.jboss</groupId>
                <artifactId>jboss-modules-builder</artifactId>
                <version>[current-version]</version>
                <executions>
                    <execution>
                        <id>build-modules</id>
                        <goals>
                            <goal>build</goal>
                        </goals>
                        <configuration>
                            <modulesYamlFile>${project.basedir}/src/main/resources/modules/modules.yaml</modulesYamlFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```


### Example

`pom file`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <properties>
        <resources.dir>${project.basedir}/src/main/resources/</resources.dir>
        <postgres.driver.version>9.1-901-1.jdbc4</postgres.driver.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>com.codenuity.jboss</groupId>
                <artifactId>jboss-modules-builder</artifactId>
                <version>[current-version]</version>
                <executions>
                    <execution>
                        <id>build-modules</id>
                        <goals>
                            <goal>build</goal>
                        </goals>
                        <configuration>
                            <modulesYamlFile>${resources.dir}/modules/modules.yaml</modulesYamlFile>
                            <parameters>
                                <groupId>${project.groupId}</groupId>
                                <version>${project.version}</version>
                                <postgres.driver.version>${postgres.driver.version}</postgres.driver.version>
                            </parameters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

`yaml file`

```yaml
layers:
  - name: "shooter"
    modules:
      - name: "com.cowboy.shooter"
        artifacts:
          - groupId: ${groupId}
            artifactId: keycloak-gui
            version: ${version}
            packaging: "jar"

      - name: "org.postgresql"
        artifacts:
          - groupId: "postgresql"
            artifactId: "postgresql"
            version: ${postgres.driver.version}
            packaging: "jar"
        dependencies:
          - name: "javax.transaction.api"
          - name: "javax.api"
```

After build creates structure in your target/modules-build folder:

```
└───jboss
    └───modules
        │   layers.conf
        │
        └───system
            └───layers
                └───shooter
                    ├───com
                    │   └───cowboy
                    │       └───shooter
                    │           └───main
                    │                   keycloak-gui-1.0-SNAPSHOT.jar
                    │                   module.xml
                    │
                    └───org
                        └───postgresql
                            └───main
                                    module.xml
                                    postgresql-9.1-901-1.jdbc4.jar
```

### Configuration

| Property | Default Value | Mandatory | Description                                                                          |
|----------|---------------|-----------|--------------------------------------------------------------------------------------|
| modulesYamlFile | - | :heavy_check_mark: | Path to yaml configuration file that describes how the modules should be built       |
| outputDirectory | ${project.build.directory}/modules-build | :x: | Output directory where the result will be stored                                     |
| workDirectory | ${project.build.directory}/work | :x: | Output directory where the temporary files will be stored                            |
| mvnExecutable | [For windows - `mvn.cmd`, for others - `mvn`] | :x: | Path to maven executable in case there is no bindings to "mvn" or "mvn.cmd" commands |
| generateLayersConf | true | :x: | If `layers.conf` will be generated                                                   |
| moduleDefaultNamespace | urn:jboss:module:1.5 | :x: | Set default namespace for all modules                                                |
| parameters | - | :x: | A map of custom parameters that are later usable in the YAML file                    |

### Yaml structure


```

layers: [array]

  - name: [string/mandatory]
    modules: [array]

      - name: [string/mandatory]
        namespace: [string]
        artifacts: [array]
          - groupId: [string/mandatory]
            artifactId: [string/mandatory]
            version: [string/mandatory]
            packaging: [string]
            classifier: [string]
            attributes: [object]
              - key[string]: value[string]

        dependencies: [array]
          - name: [string/mandatory]
            attributes: [object]
              - key[string]: value[string]

        properties: [object]
          key[string]: value[string]
```