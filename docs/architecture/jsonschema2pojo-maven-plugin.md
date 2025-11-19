# jsonschema2pojo Maven Plugin (Direct Integration)

## Purpose
- Generate API DTO classes directly via the official `jsonschema2pojo` Maven plugin.
- Zero custom helper code; configuration lives in the consumer module’s `pom.xml`.
- Align defaults with Hex DTO style (Jackson 2 + Lombok, equals/hashCode, toString, no additionalProperties).

## When To Use
- Teams prefer standard Maven plugin mechanics over framework helpers.
- DTOs generate during `generate-sources` and stay out of VCS.
- Minimal knobs, with the option to scale up to the full plugin feature set later.

## Inputs and Outputs
- Input: JSON example files (MVP) or JSON Schema (future-ready).
- Output: Java classes under a configured package into `target/generated-sources/...`.

## Add Version Property
Define the plugin version once (typically in parent POM):
```xml
<properties>
  <jsonschema2pojo.version>1.2.2</jsonschema2pojo.version>
  <!-- Optionally centralize the generated output path -->
  <hex.generated.dtos.dir>${project.build.directory}/generated-sources/hex-dtos</hex.generated.dtos.dir>
  <hex.target.dto.package>com.company.hex.project.api.dto</hex.target.dto.package>
  <hex.json.input.dir>src/test/resources/api/json</hex.json.input.dir>
  <!-- If you use schemas later: <hex.schema.input.dir>src/test/resources/api/schema</hex.schema.input.dir> -->
  </properties>
```

## Plugin Configuration (JSON examples, Hex defaults)
Add to the consuming module `pom.xml` (e.g., `hex-project-samples/pom.xml`).
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jsonschema2pojo</groupId>
      <artifactId>jsonschema2pojo-maven-plugin</artifactId>
      <version>${jsonschema2pojo.version}</version>
      <executions>
        <execution>
          <id>generate-hex-dtos</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>generate</goal>
          </goals>
          <configuration>
            <!-- Inputs: one or more directories with JSON files -->
            <sourceDirectory>${hex.json.input.dir}</sourceDirectory>
            <!-- Source type: JSON examples (not JSON Schema) -->
            <sourceType>json</sourceType>

            <!-- Output location and package -->
            <outputDirectory>${hex.generated.dtos.dir}</outputDirectory>
            <targetPackage>${hex.target.dto.package}</targetPackage>

            <!-- Hex-aligned defaults -->
            <annotationStyle>jackson2</annotationStyle>
            <useLombok>true</useLombok>
            <includeHashcodeAndEquals>true</includeHashcodeAndEquals>
            <includeToString>true</includeToString>
            <includeAdditionalProperties>false</includeAdditionalProperties>
            <propertyWordDelimiters>-_ </propertyWordDelimiters>

            <!-- Optional quality-of-life -->
            <includeConstructors>true</includeConstructors>
            <useJsr305Annotations>false</useJsr305Annotations>
            <removeOldOutput>true</removeOldOutput>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <!-- Make Maven compile generated sources -->
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>build-helper-maven-plugin</artifactId>
      <version>3.4.0</version>
      <executions>
        <execution>
          <id>add-hex-generated-sources</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>${hex.generated.dtos.dir}</source>
            </sources>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
 </build>
```

Notes
- Keep JSON examples under `src/test/resources/api/json` (or similar) in your project.
- The plugin infers types and names from sample data; stable samples yield stable DTOs.
- With `<removeOldOutput>true</removeOldOutput>`, stale classes are cleaned between runs.

## Typical JSON Example
Place an input file such as `src/test/resources/api/json/activity.json`:
```json
{
  "id": 1,
  "title": "Learn Hex",
  "dueDate": "2025-01-01",
  "completed": false
}
```
The generated class will resemble `hex-project-samples/src/main/java/com/company/hex/project/api/dto/ActivityDto.java` in shape and annotations.

## One-Time Generation (Optional)
Run generation without binding to a phase:
- `mvn jsonschema2pojo:generate -pl :your-module`

## Multiple Inputs or Patterns
To include multiple folders, duplicate the execution or switch to `<sourcePaths>`:
```xml
<configuration>
  <sourcePaths>
    <sourcePath>src/test/resources/api/json</sourcePath>
    <sourcePath>src/test/resources/api/more-examples</sourcePath>
  </sourcePaths>
  <sourceType>json</sourceType>
  <!-- other options -->
</configuration>
```

## Optional: JSON Schema (Future)
If you later provide schemas instead of examples:
```xml
<sourceDirectory>src/test/resources/api/schema</sourceDirectory>
<sourceType>jsonschema</sourceType>
```
You may also configure date/time types explicitly, e.g.:
```xml
<dateTimeType>java.time.OffsetDateTime</dateTimeType>
<dateType>java.time.LocalDate</dateType>
<timeType>java.time.LocalTime</timeType>
```

## Lombok and Jackson
- Ensure Lombok is available on the classpath; most modules in the repo already use it.
- Jackson annotations are emitted with `annotationStyle=jackson2` and work with our `RestAssured`/Jackson setup.

## Cleaning or Committing
- Preferred: Do not commit generated sources; rely on build generation via `generate-sources`.
- If you must commit: copy the generated files once into `src/main/java/...` and remove the plugin execution.

## CI Integration
- Nothing special: generation runs as part of the standard Maven lifecycle.
- Ensure CI caches the local repo to avoid repeated downloads of the plugin.

## Troubleshooting
- Wrong package? Check `<targetPackage>`.
- No classes generated? Verify `<sourceDirectory>` path and that files exist.
- Conflicting names or types? Stabilize input JSON; consider switching to JSON Schema for stronger control.
- Need different naming? Tweak `<propertyWordDelimiters>` or add `<propertyNamingStrategy>`.

## Choosing Between Plugin vs Helper
- Plugin: standard, declarative, no extra code; great for conventional use.
- Helper (`docs/architecture/jsonschema2pojo-helper.md`): programmatic control, Hex-centralized defaults, simple CLI via `exec-maven-plugin`.

Start with the plugin for simplicity. If you need more orchestrated behavior or to centralize defaults across many repos, adopt the helper.

