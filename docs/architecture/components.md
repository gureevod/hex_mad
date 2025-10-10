# Components

The Hex framework is decomposed into several key logical components, each implemented as a separate Maven module. This modular design enforces separation of concerns and allows teams to adopt only the parts of the framework they need.

### `hex-core-api`
**Responsibility:** Provides all core abstractions and utilities required for robust API testing. It has no dependency on any UI testing libraries.
**Key Interfaces:**
- `BaseApiService`: An abstract class for creating service-specific API clients. It will encapsulate RestAssured `RequestSpecification` setup, logging, and default error handling.
- `DtoBase`: A marker interface or abstract class for DTOs to enforce common patterns.
- `ApiConfig`: An Owner interface defining common API configuration properties (e.g., base URL, timeouts).
**Dependencies:** RestAssured, SLF4J, Owner, Lombok, Jackson, AssertJ.
**Technology Stack:** This component is purely Java-based and contains the foundational logic for making and verifying API calls.

### `hex-core-ui`
**Responsibility:** Provides all core abstractions for UI testing based on Selenide. It focuses on creating stable, reusable, and business-readable UI interaction patterns.
**Key Interfaces:**
- `BaseElement`: An abstract base for all element wrappers (e.g., `Button`, `Input`) providing common functionality like waiting, assertions, and logging.
- `BaseComponent`: An abstract base for creating composite UI components that group multiple elements and business logic.
- `UiConfig`: An Owner interface for UI-specific configuration (e.g., browser type, window size, base URL).
**Dependencies:** Selenide, SLF4J, Owner, Lombok, AssertJ.
**Technology Stack:** This component is built directly on top of Selenide, abstracting its powerful features into a more structured pattern.

### `hex-core-testing`
**Responsibility:** Provides the foundational testing infrastructure that glues the API and UI layers together. It contains cross-cutting concerns like test lifecycle management, reporting, and parallel execution setup.
**Key Interfaces:**
- `BaseTest`: A base test class for JUnit 5 that manages test lifecycle, context, and integration with Allure and logging.
- `ConfigFactory`: A utility for creating and managing thread-safe configuration instances.
**Dependencies:** JUnit 5, Allure, Owner, SLF4J/Logback. It has optional dependencies on `hex-core-api` and `hex-core-ui`.
**Technology Stack:** This component integrates the test runner (JUnit 5) with reporting, configuration, and logging libraries.

### `hex-project-samples` (Implementation Example)
**Responsibility:** Serves as a reference implementation and a "living documentation" of how to use the `hex-core` modules. It is not a core framework component but a consumer of it.
**Key Interfaces:** This module *implements* interfaces and extends base classes from the core modules.
- `UserApiService extends BaseApiService`
- `LoginPage extends BasePage`
- `LoginForm extends BaseComponent`
- `UserApiTests extends BaseTest`
**Dependencies:** `hex-core-api`, `hex-core-ui`, `hex-core-testing`, JavaFaker.
**Technology Stack:** This is a standard Maven project that demonstrates the end-to-end usage of the Hex framework for both API and UI tests.

### Component Diagram
```mermaid
graph TD
    subgraph "Test Project (`hex-project-samples`)"
        Tests("JUnit 5 Tests")
        PageObjects("PageObjects & Composites")
        ApiServices("API Services & DTOs")
    end

    subgraph "Framework (`hex-core-*`)"
        CoreTesting["`hex-core-testing` (Lifecycle, Reporting, Factories)"]
        CoreUI["`hex-core-ui` (Wrappers, Components)"]
        CoreAPI["`hex-core-api` (Client, Services)"]
    end

    subgraph "External Libraries"
        JUnit5("JUnit 5")
        Allure("Allure")
        Selenide("Selenide")
        RestAssured("RestAssured")
    end

    Tests --> PageObjects
    Tests --> ApiServices
    Tests --> CoreTesting

    PageObjects --> CoreUI
    ApiServices --> CoreAPI

    CoreTesting --> JUnit5
    CoreTesting --> Allure
    CoreUI --> Selenide
    CoreAPI --> RestAssured
