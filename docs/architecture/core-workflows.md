# Core Workflows

This section illustrates the primary interaction sequences between the framework components during test execution.

### API Test Execution Workflow
This diagram shows the sequence of events when a typical API test is executed using the Hex framework.

```mermaid
sequenceDiagram
    participant J as JUnit 5
    participant BT as BaseTest
    participant CF as ConfigFactory
    participant AS as ApiService
    participant RA as RestAssured
    participant AL as Allure

    J->>BT: 1. BeforeEach Test
    BT->>CF: 2. Get Config Snapshot
    CF-->>BT: 3. Returns Immutable Config
    BT->>AS: 4. ApiService.create(config)
    J->>Tests: 5. Execute Test Method
    Tests->>AS: 6. call someMethod(dto)
    AS->>AL: 7. step("Execute POST /users")
    AS->>RA: 8. buildRequest(config, dto)
    RA-->>SUT: 11. Make HTTP Call
    SUT-->>RA: 12. HTTP Response
    RA-->>AS: 9. Return Response
    AS->>Tests: 10. Return ResponseDTO
    Tests->>AL: 11. step("Verify user created")
    Tests->>AssertJ: 12. assertThat(response.id()).isNotNull()
    J->>BT: 13. AfterEach Test
```

### UI Test Execution Workflow
This diagram shows the sequence for a UI test, highlighting the creation of a thread-safe driver session and interaction with PageObjects.

```mermaid
sequenceDiagram
    participant J as JUnit 5
    participant BT as BaseTest
    participant CF as ConfigFactory
    participant WF as WebDriverFactory
    participant S as Selenide
    participant PO as PageObject
    participant C as CompositeComponent
    participant AL as Allure

    J->>BT: 1. BeforeEach Test
    BT->>CF: 2. Get Config Snapshot
    CF-->>BT: 3. Returns Immutable Config
    BT->>WF: 4. createDriver(config)
    WF-->>S: 5. Selenide.setWebDriver(driver)
    J->>Tests: 6. Execute Test Method
    Tests->>PO: 7. loginPage.loginAs(user)
    PO->>AL: 8. step("Login as standard_user")
    PO->>C: 9. loginForm.fillUsername(user.name)
    C->>S: 10. find(inputLocator).setValue(...)
    PO->>C: 11. loginForm.clickLogin()
    C->>S: 12. find(buttonLocator).click()
    J->>BT: 13. AfterEach Test
    BT->>WF: 14. closeDriver()
```

