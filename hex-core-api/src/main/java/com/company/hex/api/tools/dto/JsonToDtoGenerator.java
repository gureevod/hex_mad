package com.company.hex.api.tools.dto;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * JSON-to-DTO Generator Helper for Hex framework.
 * <p>
 * Provides a simple, opinionated way to generate Java DTOs from JSON files using jsonschema2pojo.
 * Configured with sensible defaults aligned with Hex's DTO style (Jackson, Lombok, thread-safe).
 * </p>
 * 
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * JsonToDtoGenerator.builder()
 *     .addSource(Path.of("src/test/resources/api/json/activity.json"))
 *     .targetPackage("com.company.hex.project.api.dto")
 *     .outputDir(Path.of("target/generated-sources/hex-dtos"))
 *     .build()
 *     .generate();
 * }</pre>
 */
public final class JsonToDtoGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonToDtoGenerator.class);
    
    private final List<Path> sources;
    private final String targetPackage;
    private final Path outputDir;
    private final boolean useLombok;
    private final boolean includeHashcodeAndEquals;
    private final boolean includeToString;
    private final boolean includeAdditionalProperties;
    private final String propertyWordDelimiters;
    
    private JsonToDtoGenerator(Builder builder) {
        this.sources = new ArrayList<>(builder.sources);
        this.targetPackage = builder.targetPackage;
        this.outputDir = builder.outputDir;
        this.useLombok = builder.useLombok;
        this.includeHashcodeAndEquals = builder.includeHashcodeAndEquals;
        this.includeToString = builder.includeToString;
        this.includeAdditionalProperties = builder.includeAdditionalProperties;
        this.propertyWordDelimiters = builder.propertyWordDelimiters;
    }
    
    /**
     * Creates a new builder for configuring the generator.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Generates Java DTO classes from the configured JSON sources.
     * <p>
     * This method is thread-safe; each invocation creates fresh instances of SchemaMapper
     * and GenerationConfig, ensuring no shared mutable state.
     * </p>
     *
     * @throws JsonToDtoGenerationException if generation fails
     */
    public void generate() {
        validateConfiguration();
        
        logger.info("Starting DTO generation for package: {}", targetPackage);
        logger.info("Output directory: {}", outputDir.toAbsolutePath());
        
        try {
            // Ensure output directory exists
            Files.createDirectories(outputDir);
            
            // Collect all JSON files from sources
            List<Path> jsonFiles = collectJsonFiles();
            
            if (jsonFiles.isEmpty()) {
                throw new JsonToDtoGenerationException("No JSON files found in specified sources");
            }
            
            logger.info("Found {} JSON file(s) to process", jsonFiles.size());
            
            // Create fresh instances for thread-safety
            JCodeModel codeModel = new JCodeModel();
            GenerationConfig config = createGenerationConfig();
            LombokAnnotator annotator = new LombokAnnotator(config, useLombok, includeHashcodeAndEquals, includeToString);
            RuleFactory ruleFactory = new RuleFactory(config, annotator, new SchemaStore());
            SchemaMapper mapper = new SchemaMapper(ruleFactory, new SchemaGenerator());
            
            // Process each JSON file
            for (Path jsonFile : jsonFiles) {
                processJsonFile(jsonFile, mapper, codeModel, config, annotator);
            }
            
            // Add Lombok annotations to all generated classes
            if (useLombok) {
                for (java.util.Iterator<JPackage> packages = codeModel.packages(); packages.hasNext();) {
                    JPackage pkg = packages.next();
                    for (java.util.Iterator<JDefinedClass> classes = pkg.classes(); classes.hasNext();) {
                        JDefinedClass clazz = classes.next();
                        annotator.addLombokAnnotations(clazz);
                    }
                }
            }
            
            // Write generated classes to output directory
            codeModel.build(outputDir.toFile());
            
            logger.info("Successfully generated DTOs in: {}", outputDir.toAbsolutePath());
            
        } catch (IOException e) {
            throw new JsonToDtoGenerationException("Failed to generate DTOs: " + e.getMessage(), e);
        }
    }
    
    private void validateConfiguration() {
        if (sources == null || sources.isEmpty()) {
            throw new JsonToDtoGenerationException("At least one source must be specified");
        }
        if (targetPackage == null || targetPackage.trim().isEmpty()) {
            throw new JsonToDtoGenerationException("Target package must be specified");
        }
        if (outputDir == null) {
            throw new JsonToDtoGenerationException("Output directory must be specified");
        }
        
        // Validate that all sources exist
        for (Path source : sources) {
            if (!Files.exists(source)) {
                throw new JsonToDtoGenerationException("Source path does not exist: " + source);
            }
        }
    }
    
    private List<Path> collectJsonFiles() throws IOException {
        List<Path> jsonFiles = new ArrayList<>();
        
        for (Path source : sources) {
            if (Files.isRegularFile(source)) {
                if (isJsonFile(source)) {
                    jsonFiles.add(source);
                } else {
                    logger.warn("Skipping non-JSON file: {}", source);
                }
            } else if (Files.isDirectory(source)) {
                try (Stream<Path> paths = Files.walk(source)) {
                    paths.filter(Files::isRegularFile)
                         .filter(this::isJsonFile)
                         .forEach(jsonFiles::add);
                }
            }
        }
        
        return jsonFiles;
    }
    
    private boolean isJsonFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".json");
    }
    
    private void processJsonFile(Path jsonFile, SchemaMapper mapper, JCodeModel codeModel, GenerationConfig config, LombokAnnotator annotator) throws IOException {
        logger.info("Processing: {}", jsonFile.getFileName());
        
        try {
            // Generate class name from file name
            String className = generateClassName(jsonFile);
            
            // Convert Path to URL for jsonschema2pojo
            URL schemaUrl = jsonFile.toUri().toURL();
            
            // Generate the DTO class
            mapper.generate(codeModel, className, targetPackage, schemaUrl);
            
            logger.debug("Generated class: {}.{}", targetPackage, className);
            
        } catch (IOException e) {
            throw new JsonToDtoGenerationException(
                "Failed to process JSON file: " + jsonFile + " - " + e.getMessage(), e
            );
        }
    }
    
    private String generateClassName(Path jsonFile) {
        String fileName = jsonFile.getFileName().toString();
        // Remove .json extension
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        // Convert to PascalCase
        StringBuilder className = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : baseName.toCharArray()) {
            if (propertyWordDelimiters.indexOf(c) >= 0) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                className.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                className.append(c);
            }
        }
        
        return className.toString();
    }
    
    private GenerationConfig createGenerationConfig() {
        return new DefaultGenerationConfig() {
            @Override
            public AnnotationStyle getAnnotationStyle() {
                return AnnotationStyle.JACKSON2;
            }
            
            @Override
            public SourceType getSourceType() {
                return SourceType.JSON;
            }
            
            @Override
            public boolean isUseLongIntegers() {
                return false;
            }
            
            @Override
            public boolean isIncludeHashcodeAndEquals() {
                return !useLombok && includeHashcodeAndEquals;
            }
            
            @Override
            public boolean isIncludeToString() {
                return !useLombok && includeToString;
            }
            
            @Override
            public boolean isIncludeAdditionalProperties() {
                return includeAdditionalProperties;
            }
            
            @Override
            public boolean isGenerateBuilders() {
                return !useLombok; // Don't generate builders if using Lombok
            }
            
            @Override
            public boolean isIncludeConstructors() {
                return !useLombok; // Lombok will generate constructors
            }
            
            @Override
            public boolean isConstructorsRequiredPropertiesOnly() {
                return false;
            }
            
            @Override
            public boolean isIncludeGetters() {
                return !useLombok; // Lombok will generate these
            }
            
            @Override
            public boolean isIncludeSetters() {
                return !useLombok; // Lombok will generate these
            }
            
            @Override
            public char[] getPropertyWordDelimiters() {
                return propertyWordDelimiters.toCharArray();
            }
            
            @Override
            public boolean isIncludeJsr303Annotations() {
                return false;
            }
            
            @Override
            public boolean isIncludeGeneratedAnnotation() {
                return false; // Don't add @Generated annotation
            }
            
            @Override
            public boolean isUseInnerClassBuilders() {
                return false;
            }
            
            @Override
            public boolean isIncludeDynamicAccessors() {
                return false;
            }
            
            @Override
            public boolean isIncludeDynamicGetters() {
                return false;
            }
            
            @Override
            public boolean isIncludeDynamicSetters() {
                return false;
            }
            
            @Override
            public boolean isIncludeDynamicBuilders() {
                return false;
            }
        };
    }
    
    /**
     * Builder for {@link JsonToDtoGenerator}.
     * <p>
     * Provides fluent API for configuring the generator with sensible defaults.
     * </p>
     */
    public static final class Builder {
        private final List<Path> sources = new ArrayList<>();
        private String targetPackage;
        private Path outputDir = Path.of("target/generated-sources/hex-dtos");
        private boolean useLombok = true;
        private boolean includeHashcodeAndEquals = true;
        private boolean includeToString = true;
        private boolean includeAdditionalProperties = false;
        private String propertyWordDelimiters = "-_ ";
        
        private Builder() {
        }
        
        /**
         * Adds a source JSON file or directory to process.
         * <p>
         * If a directory is provided, all JSON files within it (recursively) will be processed.
         * </p>
         *
         * @param jsonFileOrDir path to a JSON file or directory containing JSON files
         * @return this builder
         */
        public Builder addSource(Path jsonFileOrDir) {
            if (jsonFileOrDir == null) {
                throw new IllegalArgumentException("Source path cannot be null");
            }
            this.sources.add(jsonFileOrDir);
            return this;
        }
        
        /**
         * Sets multiple source JSON files or directories at once.
         *
         * @param filesOrDirs collection of paths to JSON files or directories
         * @return this builder
         */
        public Builder sources(Collection<Path> filesOrDirs) {
            if (filesOrDirs == null || filesOrDirs.isEmpty()) {
                throw new IllegalArgumentException("Sources collection cannot be null or empty");
            }
            this.sources.addAll(filesOrDirs);
            return this;
        }
        
        /**
         * Sets the target package for generated DTO classes.
         *
         * @param pkg package name (e.g., "com.company.hex.project.api.dto")
         * @return this builder
         */
        public Builder targetPackage(String pkg) {
            if (pkg == null || pkg.trim().isEmpty()) {
                throw new IllegalArgumentException("Target package cannot be null or empty");
            }
            this.targetPackage = pkg;
            return this;
        }
        
        /**
         * Sets the output directory for generated sources.
         * <p>
         * Default: {@code target/generated-sources/hex-dtos}
         * </p>
         *
         * @param dir output directory path
         * @return this builder
         */
        public Builder outputDir(Path dir) {
            if (dir == null) {
                throw new IllegalArgumentException("Output directory cannot be null");
            }
            this.outputDir = dir;
            return this;
        }
        
        /**
         * Configures whether to use Lombok annotations for getters, setters, builders, etc.
         * <p>
         * Default: {@code true}
         * </p>
         *
         * @param enabled true to use Lombok annotations
         * @return this builder
         */
        public Builder useLombok(boolean enabled) {
            this.useLombok = enabled;
            return this;
        }
        
        /**
         * Configures whether to include hashCode() and equals() methods.
         * <p>
         * Default: {@code true}
         * </p>
         *
         * @param include true to include hashCode and equals
         * @return this builder
         */
        public Builder includeHashcodeAndEquals(boolean include) {
            this.includeHashcodeAndEquals = include;
            return this;
        }
        
        /**
         * Configures whether to include toString() method.
         * <p>
         * Default: {@code true}
         * </p>
         *
         * @param include true to include toString
         * @return this builder
         */
        public Builder includeToString(boolean include) {
            this.includeToString = include;
            return this;
        }
        
        /**
         * Configures whether to include additional properties field.
         * <p>
         * Default: {@code false}
         * </p>
         *
         * @param include true to include additional properties
         * @return this builder
         */
        public Builder includeAdditionalProperties(boolean include) {
            this.includeAdditionalProperties = include;
            return this;
        }
        
        /**
         * Sets the delimiters used to detect word boundaries in property names.
         * <p>
         * Default: {@code "-_ "}
         * </p>
         *
         * @param delimiters string containing delimiter characters
         * @return this builder
         */
        public Builder propertyWordDelimiters(String delimiters) {
            if (delimiters == null) {
                throw new IllegalArgumentException("Property word delimiters cannot be null");
            }
            this.propertyWordDelimiters = delimiters;
            return this;
        }
        
        /**
         * Sets the source type to JSON (fixed for MVP).
         * <p>
         * This is a no-op method included for API completeness and future extensibility.
         * </p>
         *
         * @return this builder
         */
        public Builder sourceTypeJson() {
            // Fixed to JSON for MVP - no-op but included for API clarity
            return this;
        }
        
        /**
         * Builds the configured {@link JsonToDtoGenerator}.
         *
         * @return a new JsonToDtoGenerator instance
         * @throws IllegalStateException if required configuration is missing
         */
        public JsonToDtoGenerator build() {
            if (sources.isEmpty()) {
                throw new IllegalStateException("At least one source must be specified");
            }
            if (targetPackage == null || targetPackage.trim().isEmpty()) {
                throw new IllegalStateException("Target package must be specified");
            }
            return new JsonToDtoGenerator(this);
        }
    }
}