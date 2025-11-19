package com.company.hex.api.tools.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line interface for {@link JsonToDtoGenerator}.
 * <p>
 * Designed to be invoked via maven-exec-plugin during the build process.
 * Provides a simple argument parser for configuring DTO generation.
 * </p>
 * 
 * <h3>Supported Arguments:</h3>
 * <ul>
 *   <li>{@code --source=<path>} - Path to JSON file or directory (repeatable)</li>
 *   <li>{@code --targetPackage=<package>} - Target package for generated classes (required)</li>
 *   <li>{@code --outputDir=<path>} - Output directory (default: target/generated-sources/hex-dtos)</li>
 *   <li>{@code --noLombok} - Disable Lombok annotations</li>
 *   <li>{@code --includeAdditionalProperties} - Include additional properties field</li>
 *   <li>{@code --propertyWordDelimiters=<chars>} - Word delimiters (default: "-_ ")</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * java com.company.hex.api.tools.dto.JsonToDtoGeneratorCli \
 *   --source=src/test/resources/api/json \
 *   --targetPackage=com.company.hex.project.api.dto \
 *   --outputDir=target/generated-sources/hex-dtos
 * }</pre>
 */
public final class JsonToDtoGeneratorCli {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonToDtoGeneratorCli.class);
    
    private JsonToDtoGeneratorCli() {
        // Utility class - prevent instantiation
    }
    
    public static void main(String[] args) {
        try {
            CliConfig config = parseArguments(args);
            
            logger.info("Starting DTO generation with configuration:");
            logger.info("  Sources: {}", config.sources);
            logger.info("  Target Package: {}", config.targetPackage);
            logger.info("  Output Directory: {}", config.outputDir);
            logger.info("  Use Lombok: {}", config.useLombok);
            
            JsonToDtoGenerator.Builder builder = JsonToDtoGenerator.builder()
                .targetPackage(config.targetPackage)
                .outputDir(config.outputDir)
                .useLombok(config.useLombok)
                .includeHashcodeAndEquals(config.includeHashcodeAndEquals)
                .includeToString(config.includeToString)
                .includeAdditionalProperties(config.includeAdditionalProperties);
            
            // Add all sources
            for (Path source : config.sources) {
                builder.addSource(source);
            }
            
            if (config.propertyWordDelimiters != null) {
                builder.propertyWordDelimiters(config.propertyWordDelimiters);
            }
            
            // Build and generate
            builder.build().generate();
            
            logger.info("DTO generation completed successfully");
            System.exit(0);
            
        } catch (Exception e) {
            logger.error("DTO generation failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static CliConfig parseArguments(String[] args) {
        if (args.length == 0) {
            printUsageAndExit();
        }
        
        CliConfig config = new CliConfig();
        
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                printUsageAndExit();
            } else if (arg.startsWith("--source=")) {
                String pathStr = arg.substring("--source=".length());
                config.sources.add(Paths.get(pathStr));
            } else if (arg.startsWith("--targetPackage=")) {
                config.targetPackage = arg.substring("--targetPackage=".length());
            } else if (arg.startsWith("--outputDir=")) {
                config.outputDir = Paths.get(arg.substring("--outputDir=".length()));
            } else if (arg.equals("--noLombok")) {
                config.useLombok = false;
            } else if (arg.equals("--includeAdditionalProperties")) {
                config.includeAdditionalProperties = true;
            } else if (arg.equals("--noHashcodeAndEquals")) {
                config.includeHashcodeAndEquals = false;
            } else if (arg.equals("--noToString")) {
                config.includeToString = false;
            } else if (arg.startsWith("--propertyWordDelimiters=")) {
                config.propertyWordDelimiters = arg.substring("--propertyWordDelimiters=".length());
            } else {
                System.err.println("Unknown argument: " + arg);
                printUsageAndExit();
            }
        }
        
        // Validate required arguments
        if (config.sources.isEmpty()) {
            System.err.println("Error: At least one --source must be specified");
            printUsageAndExit();
        }
        if (config.targetPackage == null || config.targetPackage.trim().isEmpty()) {
            System.err.println("Error: --targetPackage must be specified");
            printUsageAndExit();
        }
        
        return config;
    }
    
    private static void printUsageAndExit() {
        System.out.println("Hex JSON-to-DTO Generator");
        System.out.println();
        System.out.println("Usage: java " + JsonToDtoGeneratorCli.class.getName() + " [options]");
        System.out.println();
        System.out.println("Required Options:");
        System.out.println("  --source=<path>              Path to JSON file or directory (repeatable)");
        System.out.println("  --targetPackage=<package>    Target package for generated classes");
        System.out.println();
        System.out.println("Optional Options:");
        System.out.println("  --outputDir=<path>           Output directory");
        System.out.println("                               (default: target/generated-sources/hex-dtos)");
        System.out.println("  --noLombok                   Disable Lombok annotations");
        System.out.println("  --includeAdditionalProperties Include additional properties field");
        System.out.println("  --noHashcodeAndEquals        Disable hashCode and equals generation");
        System.out.println("  --noToString                 Disable toString generation");
        System.out.println("  --propertyWordDelimiters=<c> Word delimiters (default: \"-_ \")");
        System.out.println("  --help, -h                   Show this help message");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java " + JsonToDtoGeneratorCli.class.getName() + " \\");
        System.out.println("    --source=src/test/resources/api/json \\");
        System.out.println("    --targetPackage=com.company.hex.project.api.dto \\");
        System.out.println("    --outputDir=target/generated-sources/hex-dtos");
        System.out.println();
        System.exit(0);
    }
    
    /**
     * Internal configuration holder for CLI arguments.
     */
    private static class CliConfig {
        List<Path> sources = new ArrayList<>();
        String targetPackage;
        Path outputDir = Path.of("target/generated-sources/hex-dtos");
        boolean useLombok = true;
        boolean includeHashcodeAndEquals = true;
        boolean includeToString = true;
        boolean includeAdditionalProperties = false;
        String propertyWordDelimiters = null; // null = use default
    }
}