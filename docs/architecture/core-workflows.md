# Core Workflows

This section illustrates the primary interaction sequences between the framework components during test execution.

### API Test Execution Workflow
This diagram shows the sequence of events when a typical API test is executed using the Hex framework.

```mermaid
sequenceDiagram
    participant J as JUnit 5
    participant BT as BaseTest
    participant CF as ConfigFactory
    participant SP as ServiceProvider
    participant AS as ApiService
    participant RA as RestAssured
    participant AL as Allure

    J->>BT: 1. BeforeEach Test
    BT->>CF: 2. Get Config Snapshot
    CF-->>BT: 3. Returns Immutable Config
    BT->>SP: 4. Get ApiService Instance
    SP->>AS: 5. new ApiService(config)
    SP-->>BT: 6. Returns Service Instance
    J->>Tests: 7. Execute Test Method
    Tests->>AS: 8. call someMethod(dto)
    AS->>AL: 9. step("Execute POST /users")
    AS->>RA: 10. buildRequest(config, dto)
    RA-->>SUT: 11. Make HTTP Call
    SUT-->>RA: 12. HTTP Response
    RA-->>AS: 13. Return Response
    AS->>Tests: 14. Return ResponseDTO
    Tests->>AL: 15. step("Verify user created")
    Tests->>AssertJ: 16. assertThat(response.id()).isNotNull()
    J->>BT: 17. AfterEach Test
```

### UI Test Execution Workflow
This diagram shows the sequence for a UI test, highlighting the creation of a thread-safe driver session and interaction with PageObjects.

```mermaid
sequenceDiagram
    participant J as JUnit 5
    participant BT as BaseTest
    participant CF as ConfigFactory
    participant DP as DriverProvider
    participant S as Selenide
    participant PO as PageObject
    participant C as CompositeComponent
    participant AL as Allure

    J->>BT: 1. BeforeEach Test
    BT->>CF: 2. Get Config Snapshot
    CF-->>BT: 3. Returns Immutable Config
    BT->>DP: 4. Get WebDriver/Selenide Session
    DP->>S: 5. new WebDriver() / Selenide.setWebDriver()
    DP-->>BT: 6. Returns Thread-Local Session
    J->>Tests: 7. Execute Test Method
    Tests->>PO: 8. loginPage.loginAs(user)
    PO->>AL: 9. step("Login as standard_user")
    PO->>C: 10. loginForm.fillUsername(user.name)
    C->>S: 11. find(inputLocator).setValue(...)
    PO->>C: 12. loginForm.clickLogin()
    C->>S: 13. find(buttonLocator).click()
    J->>BT: 14. AfterEach Test
    BT->>DP: 15. Close WebDriver Session
```
