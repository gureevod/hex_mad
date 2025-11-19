# JSON-to-DTO Generator Helper (hex-core-api)

## Purpose
- Provide a tiny, opinionated helper to generate API DTO classes from JSON files using `jsonschema2pojo`.
- Remove manual DTO coding (e.g., `ActivityDto`) while keeping configuration simple and predictable.
- Fit into Hex architecture: immutable configs, thread-safe utilities, minimal surface area, easy adoption in projects.

## Scope (MVP)
- Input: one or more JSON files or a directory with JSON files.
- Output: Java DTOs into a target package and output directory.
- Annotations: Jackson 2 for JSON mapping; Lombok for boilerplate reduction.
- Source type: JSON (examples), not JSON Schema or OpenAPI (can be added later).
- Keep defaults aligned with our DTO style; expose a few switches only.

## Module & Package
- Module: `hex-core-api`
- Package: `com.company.hex.api.tools.dto`

## Dependencies (added to hex-core-api)
- `org.jsonschema2pojo:jsonschema2pojo-core` (core generator)
- `com.fasterxml.jackson.core:jackson-databind` (transitive required by annotator)
- Lombok is expected to be available in consuming modules (already standard in repo)

Example (pom snippet for `hex-core-api`):
```xml
<dependency>
  <groupId>org.jsonschema2pojo</groupId>
  <artifactId>jsonschema2pojo-core</artifactId>
  <version>${jsonschema2pojo.version}</version>
</dependency>
```

## Design

### Core Types
```java
// Builder-style, minimal config surface
public final class JsonToDtoGenerator {
  public static Builder builder() { /* ... */ }
  public void generate();

  public static final class Builder {
    public Builder addSource(Path jsonFileOrDir);         // add a file or folder (glob handled internally)
    public Builder sources(Collection<Path> filesOrDirs); // convenience for multiple inputs
    public Builder targetPackage(String pkg);             // e.g. "com.company.hex.project.api.dto"
    public Builder outputDir(Path dir);                   // default: target/generated-sources/hex-dto

    // Optional toggles (sane defaults)
    public Builder useLombok(boolean enabled);            // default: true
    public Builder includeHashcodeAndEquals(boolean b);   // default: true
    public Builder includeToString(boolean b);            // default: true
    public Builder includeAdditionalProperties(boolean b);// default: false
    public Builder propertyWordDelimiters(String delims); // default: "-_ "
    public Builder sourceTypeJson();                      // fixed for MVP

    public JsonToDtoGenerator build();
  }
}
```

### Defaults (aligned with ActivityDto style)
- `annotationStyle = JACKSON2`
- `useLombok = true` (getters/setters, constructors, builder)
- `includeHashcodeAndEquals = true`
- `includeToString = true`
- `includeAdditionalProperties = false` (no `Map<String,Object> additionalProperties`)
- `sourceType = JSON`
- `propertyWordDelimiters = "-_ "` (smart camelCase from typical keys)
- Field names and types inferred from sample JSON

Note: jsonschema2pojo with Lombok generates Lombok annotations that effectively match our preferred boilerplate (`@Getter/@Setter`, constructors, builder). It may not generate literal `@Data`, but the net effect (equals/hashCode/toString) is the same with `includeHashcodeAndEquals` and `includeToString`.

### Thread-Safety
- The generator is stateless after `build()`; each `generate()` creates fresh `SchemaMapper` + `GenerationConfig` instances.
- No shared mutable state or singletons.

## Usage

### 1) Programmatic (one-off or test-time generation)
```java
import com.company.hex.api.tools.dto.JsonToDtoGenerator;

JsonToDtoGenerator.builder()
    .addSource(Path.of("src/test/resources/api/json/activity.json"))
    .targetPackage("com.company.hex.project.api.dto")
    .outputDir(Path.of("target/generated-sources/hex-dtos"))
    .useLombok(true)
    .includeHashcodeAndEquals(true)
    .includeToString(true)
    .includeAdditionalProperties(false)
    .build()
    .generate();
```

Generated classes can be copied into your sources if you want to persist them, or compiled directly from `target/generated-sources/...` when wired into Maven.

### 2) Build-integrated (preferred)
Add the generator as a small CLI wrapper in `hex-core-api` (class `JsonToDtoGeneratorCli`), then run it via `maven-exec-plugin` in consuming modules. This keeps the config and defaults centralized.

Example (in a consumer module pom):
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>3.1.0</version>
  <executions>
    <execution>
      <id>generate-hex-dtos</id>
      <phase>generate-sources</phase>
      <goals><goal>java</goal></goals>
      <configuration>
        <mainClass>com.company.hex.api.tools.dto.JsonToDtoGeneratorCli</mainClass>
        <arguments>
          <!-- Accepts --source, --targetPackage, --outputDir, and minimal toggles -->
          <argument>--source=src/test/resources/api/json</argument>
          <argument>--targetPackage=com.company.hex.project.api.dto</argument>
          <argument>--outputDir=target/generated-sources/hex-dtos</argument>
        </arguments>
      </configuration>
    </execution>
  </executions>
</plugin>

<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <version>3.4.0</version>
  <executions>
    <execution>
      <id>add-generated-sources</id>
      <phase>generate-sources</phase>
      <goals><goal>add-source</goal></goals>
      <configuration>
        <sources>
          <source>target/generated-sources/hex-dtos</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
```

This setup compiles generated DTOs without committing them. If you prefer committed sources, copy files once and remove the plugins.

## Minimal CLI (optional but recommended)
```java
public final class JsonToDtoGeneratorCli {
  public static void main(String[] args) {
    // parse args: --source (repeatable), --targetPackage, --outputDir,
    // flags: --noLombok, --includeAdditionalProperties, etc.
    // build the generator and call generate()
  }
}
```

## Example: Activity JSON -> DTO
Input `activity.json` (simplified):
```json
{
  "id": 1,
  "title": "Learn Hex",
  "dueDate": "2025-01-01",
  "completed": false
}
```

Generated class (shape):
```java
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Getter @lombok.Setter
public class ActivityDto {
  @JsonProperty("id")
  private int id;
  @JsonProperty("title")
  private String title;
  @JsonProperty("dueDate")
  private String dueDate;
  @JsonProperty("completed")
  private boolean completed;
}
```

## Error Handling & Logging
- Fail fast with clear messages if a source path is missing/empty.
- Log which files were processed and where classes were written.
- Surface jsonschema2pojo validation errors with file context.

## Limitations (MVP)
- Only JSON example files as input (no JSON Schema/OpenAPI yet).
- Single target package per run.
- No per-field overrides; relies on `jsonschema2pojo` inference.

## Future Enhancements
- Support `sourceType = JSONSCHEMA` and OpenAPI integration.
- Per-file package overrides and enum customization.
- Date/time strategy (JSR-310 types) and custom type mappings.
- Seamless integration with declarative API generator phase.

## Why not the Maven plugin directly?
- The official plugin is powerful but verbose to configure per project.
- Our helper centralizes defaults aligned with Hex and exposes just the few toggles teams need.
- Projects can still switch to the plugin later if they need full control.

## Checklist to Adopt
- Add dependency on `hex-core-api` (brings the helper).
- Add either: programmatic call in a small utility class, or the `exec-maven-plugin` invocation.
- Place JSON files under `src/test/resources/api/json` (or similar).
- Set `targetPackage` to your project DTO package.
- Run `mvn generate-sources` and use generated DTOs.

