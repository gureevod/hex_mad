# Source Tree

The Hex framework will be organized as a Maven multi-module project within a single monorepo. This structure facilitates shared dependency management, consistent build processes, and atomic commits across the framework's components.

```plaintext
hex-automation-framework/
├── .github/
│   └── workflows/
│       └── ci.yaml                     # GitHub Actions CI/CD pipeline
├── hex-core-api/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/api/
│   │   │           ├── config/         # Owner interfaces for API config
│   │   │           ├── dto/            # Base DTO interfaces/classes
│   │   │           └── service/        # BaseApiService and helpers
│   │   └── test/
│   │       └── java/                   # Unit tests for core API components
│   └── pom.xml                         # Module POM for hex-core-api
├── hex-core-ui/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/ui/
│   │   │           ├── config/         # Owner interfaces for UI config
│   │   │           ├── core/           # BaseElement, BaseComponent
│   │   │           └── wrappers/       # Button, Input, Select, etc.
│   │   └── test/
│   │       └── java/                   # Unit tests for core UI components
│   └── pom.xml                         # Module POM for hex-core-ui
├── hex-core-testing/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/testing/
│   │   │           ├── allure/         # Allure integration helpers
│   │   │           ├── config/         # ConfigFactory
│   │   │           ├── di/             # DI Providers/Factories
│   │   │           ├── lifecycle/      # BaseTest, JUnit extensions
│   │   │           └── logging/        # Logging configuration helpers
│   │   └── test/
│   │       └── java/                   # Unit tests for core testing components
│   └── pom.xml                         # Module POM for hex-core-testing
├── hex-project-samples/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/project/
│   │   │           ├── api/
│   │   │           │   ├── dto/        # Project-specific DTOs (generated)
│   │   │           │   └── services/   # Project-specific API services
│   │   │           ├── pages/          # PageObjects
│   │   │           ├── components/     # Custom composite components
│   │   │           └── config/         # Project-specific configuration
│   │   └── test/
│   │       └── java/
│   │           └── com/company/hex/project/tests/
│   │               ├── api/            # API tests
│   │               └── ui/             # UI tests
│   ├── pom.xml                         # Module POM for the sample project
│   └── Jenkinsfile.groovy              # CI/CD pipeline definition for samples
├── docs/
│   ├── architecture.md                 # This document
│   └── prd.md                          # Product Requirements Document
├── .gitignore
├── pom.xml                             # Parent POM for the entire monorepo
└── README.md                           # Project-level README (in Russian)
```

