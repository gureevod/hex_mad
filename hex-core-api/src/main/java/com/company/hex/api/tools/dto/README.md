# JSON-to-DTO Generator Helper

A simple, opinionated utility for generating Java DTO classes from JSON example files using `jsonschema2pojo`.

## Features

- **Simple Builder API** - Fluent interface for configuration
- **Sensible Defaults** - Aligned with Hex framework DTO patterns
- **Thread-Safe** - No shared mutable state
- **Maven Integration** - CLI wrapper for build-time generation
- **Flexible Sources** - Process individual files or entire directories

## Quick Start

### Programmatic Usage

```java
import com.company.hex.api.tools.dto.JsonToDtoGenerator;
import java.nio.file.Path;

// Generate DTOs from a JSON file
JsonToDtoGenerator.builder()
    .addSource(Path.of("src/test/resources/api/json/activity.json"))
    .targetPackage("com.company.hex.project.api.dto")
    .outputDir(Path.of("target/generated-sources/hex-dtos"))
    .build()
    .generate();
```

### Maven Integration

Add to your module's `pom.xml`:

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
          <argument>--source=src/test/resources/api/json</argument>
          <argument>--targetPackage=com.company.hex.project.api.dto</argument>
          <argument>--outputDir=target/generated-sources/hex-dtos</argument>
        </arguments>
      </configuration>
    </execution>
  </executions>
  <dependencies>
    <dependency>
      <groupId>com.company.hex</groupId>
      <artifactId>hex-core-api</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>
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

Then run: `mvn generate-sources`

## Configuration Options

### Builder Methods

| Method | Default | Description |
|--------|---------|-------------|
| `addSource(Path)` | - | Add a JSON file or directory (required, repeatable) |
| `sources(Collection<Path>)` | - | Add multiple sources at once |
| `targetPackage(String)` | - | Target package for generated classes (required) |
| `outputDir(Path)` | `target/generated-sources/hex-dtos` | Output directory |
| `useLombok(boolean)` | `true` | Use Lombok annotations |
| `includeHashcodeAndEquals(boolean)` | `true` | Include hashCode and equals |
| `includeToString(boolean)` | `true` | Include toString |
| `includeAdditionalProperties(boolean)` | `false` | Include additional properties field |
| `propertyWordDelimiters(String)` | `"-_ "` | Word delimiters for camelCase conversion |

### CLI Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `--source=<path>` | Yes | Path to JSON file or directory (repeatable) |
| `--targetPackage=<pkg>` | Yes | Target package name |
| `--outputDir=<path>` | No | Output directory (default: `target/generated-sources/hex-dtos`) |
| `--noLombok` | No | Disable Lombok annotations |
| `--includeAdditionalProperties` | No | Include additional properties field |
| `--noHashcodeAndEquals` | No | Disable hashCode/equals generation |
| `--noToString` | No | Disable toString generation |
| `--propertyWordDelimiters=<chars>` | No | Custom word delimiters |

## Example

### Input JSON (`activity.json`)

```json
{
  "id": 1,
  "title": "Learn Hex Framework",
  "dueDate": "2025-01-01",
  "completed": false,
  "priority": "high"
}
```

### Generated DTO (`Activity.java`)

```java
package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("dueDate")
    private String dueDate;
    
    @JsonProperty("completed")
    private Boolean completed;
    
    @JsonProperty("priority")
    private String priority;
}
```

**Note:** `@Data` is a convenient shortcut that includes `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, and `@RequiredArgsConstructor`. This keeps the generated code clean and compact.

## File Naming Convention

The generator converts JSON filenames to PascalCase class names:

- `activity.json` → `Activity.java`
- `user-profile.json` → `UserProfile.java`
- `api_response.json` → `ApiResponse.java`

## Error Handling

The generator fails fast with clear error messages:

- **Missing source**: Source path does not exist
- **No JSON files**: No JSON files found in specified sources
- **Missing configuration**: Required parameters not specified
- **Processing errors**: Failed to process specific JSON file (with file context)

## Thread Safety

The generator is thread-safe:
- Each `generate()` call creates fresh instances of internal components
- No shared mutable state or singletons
- Safe for parallel test execution

## Limitations (MVP)

- **Source Type**: Only JSON example files (no JSON Schema or OpenAPI)
- **Single Package**: One target package per generation run
- **Inference-Based**: Relies on jsonschema2pojo type inference from sample data
- **No Per-Field Overrides**: Global configuration only

## Future Enhancements

- Support for JSON Schema and OpenAPI specifications
- Per-file package overrides
- Custom type mappings (e.g., date/time formats)
- Enum generation and customization
- Integration with declarative API generator

## Why Not Use the Maven Plugin Directly?

The official `jsonschema2pojo-maven-plugin` is powerful but:
- Requires verbose configuration per project
- Has many options that most users don't need
- Can be overwhelming for simple use cases

This helper:
- Centralizes Hex-aligned defaults
- Exposes only essential toggles
- Provides simpler programmatic API
- Teams can still migrate to the full plugin later if needed

## Dependencies

Required in `hex-core-api/pom.xml`:

```xml
<dependency>
  <groupId>org.jsonschema2pojo</groupId>
  <artifactId>jsonschema2pojo-core</artifactId>
</dependency>
```

Jackson and Lombok are expected to be available in consuming modules.