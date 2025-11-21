package com.company.hex.project.tests.api;

import com.company.hex.api.tools.dto.JsonToDtoGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Tests for {@link JsonToDtoGenerator}.
 */
class JsonToDtoGeneratorTest {

    @Test
    void shouldGenerateDtoFromJsonFile() throws IOException {
        // Given

        String targetPackage = "com.company.hex.project.api.dto";

        // When
        JsonToDtoGenerator.builder()
                .addSource(Path.of("D:\\projects\\hex_mad\\hex-core-api\\src\\test\\resources\\test-json\\activity.json"))
                .targetPackage(targetPackage)
                .outputDir(Path.of("D:\\projects\\hex_mad\\hex-project-samples\\src\\main\\java\\com\\company\\hex\\project\\api\\dto"))
                .useLombok(true)
                .build()
                .generate();

        System.out.println();

    }

}