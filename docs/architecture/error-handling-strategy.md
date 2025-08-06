# Error Handling Strategy

This section defines a unified approach to error handling, logging, and reporting within the Hex framework to ensure that test failures are informative, debuggable, and consistent.

### General Approach
- **Error Model:** The framework will primarily use Java's standard exception handling mechanism. It will favor unchecked exceptions (`RuntimeException`) for internal framework errors to avoid cluttering method signatures, and will provide clear guidance on catching and handling expected application errors.
- **Exception Hierarchy:** A custom exception hierarchy will be created to provide more context than standard exceptions.
    - `HexFrameworkException (extends RuntimeException)`
        - `HexApiException (extends HexFrameworkException)`
        - `HexUiException (extends HexFrameworkException)`
        - `HexConfigException (extends HexFrameworkException)`
- **Error Propagation:** Errors originating from underlying libraries (RestAssured, Selenide) will be caught and wrapped in a corresponding `Hex...Exception` to add context (e.g., which element was being interacted with, which API endpoint was called) before being re-thrown.

### Logging Standards
- **Library:** SLF4J API (`2.0.13`) with a Logback Classic (`1.5.6`) implementation.
- **Format:** Logs will be structured and consistent. The default pattern will be: `[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] [%-5level] %logger{36} - %msg%n`.
- **Levels:**
    - `ERROR`: For critical framework failures or assertion errors.
    - `WARN`: For potential issues, such as deprecated usage or non-critical API failures that are being retried.
    - `INFO`: For high-level test lifecycle events (e.g., "Starting test", "Closing driver").
    - `DEBUG`: For detailed framework actions (e.g., "Clicking button 'Login'", "Sending POST to /users").
    - `TRACE`: For extremely verbose output, including request/response bodies (with redaction).
- **Required Context:**
    - **Correlation ID:** A unique ID will be generated for each test run and included in the log context (via MDC - Mapped Diagnostic Context) to correlate all logs for a single test execution.
    - **Redaction:** A custom Logback `TurboFilter` will be implemented to redact sensitive information (passwords, tokens, PII) from log messages based on configurable regex patterns. This directly addresses NFR5.

### Error Handling Patterns
- **External API Errors (within `hex-core-api`):**
    - **Retry Policy:** No built-in retry mechanism will be provided in `hex-core` to avoid encouraging tests that mask real backend instability. The framework will document how a project could implement its own retry logic using libraries like Failsafe if necessary.
    - **Error Translation:** HTTP 4xx and 5xx responses will be automatically translated into a `HexApiException` containing the status code, response body, and request details to facilitate clear assertions in tests.
- **UI Interaction Errors (within `hex-core-ui`):**
    - **Implicit Waits:** The framework will rely on Selenide's robust built-in waiting mechanism. Default timeouts will be configurable via `UiConfig`.
    - **Explicit Waits:** Custom wait methods will be provided in `BaseElement` for complex conditions.
    - **Failure Reporting:** On failure, Selenide's default behavior of taking a screenshot and capturing page source will be enabled. This information will be automatically attached to the Allure report. The wrapping `HexUiException` will include details about the element and the failed condition.
- **Data Consistency:**
    - **Idempotency:** The framework will provide guidance on how to design API tests to be idempotent where possible, especially for `POST` or `PUT` operations that might be retried manually or in CI.
