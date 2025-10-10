# Coding Standards

These standards are mandatory for all contributions to the `hex-core-*` modules to ensure code quality, consistency, and maintainability. Consuming projects (`hex-project-*`) are strongly encouraged to adopt them.

### Guiding Principles
Our development philosophy is guided by a pragmatic balance of several core principles:

-   **You apply DRY (Don't Repeat Yourself)** to avoid code duplication.
-   **You use YAGNI (You Ain't Gonna Need It)** to stop yourself from adding unneeded complexity while trying to be DRY.
-   **You follow KISS (Keep It Simple, Stupid)** to ensure that the solutions you do build are clear and understandable.
-   **The SOLID principles** provide more formal, object-oriented guidelines to achieve all of the above in a maintainable architecture.
-   **SoC (Separation of Concerns)** is the high-level strategy for organizing your code to make following the other principles possible.

### Core Standards
- **Languages & Runtimes:** Java 17. Code must be compatible with this version.
- **Style & Linting:** Google Java Style Guide will be the standard. A Checkstyle configuration file will be provided and integrated into the Maven build, failing the build on violations.
- **Test Organization:** Tests must follow the standard Maven directory structure (`src/test/java`). Test classes must be named with a `Test` or `Tests` suffix (e.g., `UserServiceTest.java`).

### Naming Conventions

| Element | Convention | Example |
| :--- | :--- | :--- |
| Packages | lowercase, dot-separated | `io.hex.framework.ui.wrappers` |
| Classes, Interfaces, Enums | PascalCase | `BaseApiService`, `UiConfig` |
| Methods | camelCase | `getUserById()` |
| Constants | UPPER_SNAKE_CASE | `DEFAULT_TIMEOUT` |
| Test Methods | camelCase or snake_case | `shouldCreateUserSuccessfully()` or `should_create_user_successfully` |
| PageObject Classes | PascalCase with `Page` suffix | `LoginPage`, `DashboardPage` |
| API Service Classes | PascalCase with `Service` suffix | `UserService`, `AuthService` |

### Critical Rules
- **Immutability:** Prefer immutable objects where possible, especially for DTOs and configuration objects. Use `final` fields and Lombok's `@Value` annotation.
- **No `System.out.println()`:** All logging must go through the SLF4J facade. Any use of `System.out` or `System.err` is a build failure.
- **Instantiation:** Use designated factories (e.g., `ApiServiceFactory`) or direct instantiation (`new PageObject()`) where appropriate. Avoid complex object creation logic within test methods.
- **Direct Library Usage:** Avoid using RestAssured or Selenide APIs directly within test methods. All interactions must go through the provided `ApiService` or `PageObject`/`Component` abstractions.
- **Optional Usage:** Use `java.util.Optional` correctly. Do not call `.get()` without an `.isPresent()` check; prefer methods like `orElse()`, `orElseThrow()`, or `ifPresent()`.
- **Javadoc:** All public classes and methods in the `hex-core-*` modules must have clear Javadoc documentation in Russian, as per NFR11.

### Language-Specific Guidelines
- **Java 17 Specifics:**
    - **Records:** Use Java Records for simple, immutable DTOs where appropriate, as an alternative to Lombok's `@Value`.
    - **Text Blocks:** Use text blocks for multi-line strings, such as JSON payloads in tests.
    - **Switch Expressions:** Prefer switch expressions over switch statements for more concise and less error-prone code.

