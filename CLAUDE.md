# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven-based multi-module Java test automation framework called "Hex Automation Framework" built with Java 17. The framework provides modular components for API testing (RestAssured), UI testing (Selenide), and comprehensive test reporting (Allure).

## Key Commands

### Build & Test
- **Build project**: `mvn clean install`
- **Run unit tests**: `mvn test`
- **Run integration tests**: `mvn failsafe:integration-test`
- **Generate test reports**: Allure results are automatically generated in `target/allure-results`
- **Code quality check**: `mvn checkstyle:check` (uses Google Java Style Guide)
- **Code coverage**: `mvn jacoco:report` (minimum 85% coverage required)

### Development
- **Compile only**: `mvn compile`
- **Skip tests**: `mvn clean install -DskipTests`
- **Run specific test**: `mvn test -Dtest=ClassName#methodName`

## Architecture

### Module Structure
The project follows a hexagonal architecture pattern with clear separation of concerns:

- **hex-core**: Base module containing common abstractions, configuration utilities, and logging infrastructure
- **hex-core-api**: API testing capabilities using RestAssured with BaseApiService abstractions
- **hex-core-ui**: UI testing framework using Selenide with BaseElement/BaseComponent wrappers
- **hex-core-testing**: Testing utilities including JUnit 5 extensions, Allure integration, and BaseTest classes
- **hex-project-samples**: Example implementations and usage patterns

### Key Design Patterns
- **Factory Pattern**: Used for configuration and service creation (HexConfigFactory, ApiServiceFactory)
- **Page Object Model**: UI testing follows strict page object patterns with component abstractions
- **Service Layer**: API interactions are encapsulated in service classes
- **Builder Pattern**: Used for complex object creation and test data setup

### Configuration Management
- Uses Owner library for type-safe configuration management
- Configuration interfaces extend BaseConfig
- Environment-specific configs loaded via HexConfigFactory

## Code Standards

### Mandatory Rules
- **Java 17 compatibility**: All code must be compatible with Java 17
- **Google Java Style**: Enforced via Checkstyle, build fails on violations
- **No System.out**: Use SLF4J logging only, System.out/err causes build failure
- **Immutability**: Prefer immutable objects, use `final` fields and Lombok's `@Value`
- **Optional Usage**: Never call `.get()` without `.isPresent()` check
- **Abstraction Layers**: Never use RestAssured/Selenide APIs directly in tests

### Naming Conventions
- **Classes/Interfaces**: PascalCase (e.g., `BaseApiService`)
- **Methods**: camelCase (e.g., `getUserById()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Page Objects**: PascalCase + "Page" suffix (e.g., `LoginPage`)
- **API Services**: PascalCase + "Service" suffix (e.g., `UserService`)
- **Test Methods**: camelCase or snake_case (e.g., `shouldCreateUserSuccessfully()`)

### Documentation
- All public classes and methods in hex-core-* modules require Javadoc documentation in Russian
- Use Java 17 features: Records for DTOs, text blocks for multi-line strings, switch expressions

## Testing Strategy

### Test Organization
- Unit tests: `src/test/java` with `Test` or `Tests` suffix
- Integration tests: `IT` or `IntegrationTest` suffix
- All tests extend BaseTest from hex-core-testing module
- Allure annotations used for test reporting and step documentation

### Test Data
- Use JavaFaker for test data generation
- Immutable DTOs using Records or Lombok @Value
- JSON Schema validation for API responses

## Dependencies & Versions

### Core Framework Versions
- Java: 17 (LTS)
- Maven: 3.9.6+
- JUnit: 5.10.2
- Allure: 2.27.0
- Selenide: 7.3.2
- RestAssured: 5.4.0
- Owner: 1.0.12

### Quality & Utilities
- Checkstyle: Google Java Style Guide
- JaCoCo: Code coverage (85% minimum)
- Lombok: 1.18.32
- AssertJ: 3.25.3
- Mockito: 5.12.0

## Development Workflow

The project uses BMad Method workflow with specialized roles:
- **SM (Scrum Master)**: Story creation using `*draft` command
- **Dev (Developer)**: Story implementation using `*develop-story {story}` command  
- **QA (Quality Assurance)**: Story review using `*review {story}` command

## Important Notes

- Never directly instantiate framework objects - use provided factories
- Follow the strict layering: Tests → Services/PageObjects → Core utilities
- All configuration must go through Owner-based config interfaces
- Correlation IDs are automatically managed for request tracing
- Sensitive information must never be logged (RedactionTurboFilter active)