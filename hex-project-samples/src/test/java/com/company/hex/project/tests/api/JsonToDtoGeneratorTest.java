package com.company.hex.project.tests.api;

import com.acme.qa.orders.ApiClient;
import com.company.hex.api.tools.dto.JsonToDtoGenerator;
import com.company.hex.project.api.dto.Address;
import com.company.hex.project.api.dto.Contact;
import com.company.hex.project.api.dto.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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



        User user = User.builder()
                .id(123)
                .addresses(List.of(Address.builder().city("123").build()))
                .name("123")
                .contact(Contact.builder().email("123").build())
                .build();

        System.out.println();

    }

}