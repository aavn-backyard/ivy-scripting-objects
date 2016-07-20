# ivy-scripting-objects

Contains utility classes to overcome the cumbersome of Ivy Scritping Objects.


# How to build this project using Maven

This project use a custom Maven plugin

```
<plugin>
    <groupId>ch.ivyteam.ivy</groupId>
    <artifactId>addivyjars-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>add-ivy-jars</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

to add Axon.ivy's JARs into classpath at runtime. Make sure you have property `ivy-server-path` points to the Axon.ivy Engine directory.

# How to build this project on Eclipse

You need to create a User Library which contains **all** the JARs from the directory:

```
${axon.ivy.engine}/lib/ivy
${axon.ivy.engine}/lib/shared
```


>NOTE: Do not commit the `.classpath`, `.project`, `bin`, `.settings` and `.class` which created by Eclipse.