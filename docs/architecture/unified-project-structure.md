# Source Tree (Revised)

The Hex framework will be organized as a Maven multi-module project within a single monorepo. A new `hex-core` module will house common abstractions to prevent dependency cycles and promote reuse.

```plaintext
hex-automation-framework/
├── .github/
│   └── workflows/
│       └── ci.yaml
├── hex-core/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/core/
│   │   │           ├── config/         # Core Owner interfaces, ConfigFactory
│   │   │           ├── di/             # Core DI Provider interfaces
│   │   │           └── utils/          # Common utilities (e.g., reflection, string helpers)
│   │   └── test/
│   │       └── java/                   # Unit tests for core components
│   └── pom.xml                         # Module POM for hex-core
├── hex-core-api/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/api/
│   │   │           ├── dto/            # Base DTO interfaces
│   │   │           └── service/        # BaseApiService and RestAssured helpers
│   │   └── test/
│   │       └── java/
│   └── pom.xml                         # Depends on hex-core
├── hex-core-ui/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/ui/
│   │   │           ├── core/           # BaseElement, BaseComponent
│   │   │           └── wrappers/       # Button, Input, Select, etc.
│   │   └── test/
│   │       └── java/
│   └── pom.xml                         # Depends on hex-core
├── hex-core-testing/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/testing/
│   │   │           ├── allure/         # Allure integration helpers
│   │   │           └── lifecycle/      # BaseTest, JUnit extensions
│   │   └── test/
│   │       └── java/
│   └── pom.xml                         # Depends on hex-core
├── hex-project-samples/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/company/hex/project/
│   │   │           ├── api/
│   │   │           │   ├── dto/
│   │   │           │   └── services/
│   │   │           ├── pages/
│   │   │           ├── components/
│   │   │           └── config/
│   │   └── test/
│   │       └── java/
│   │           └── com/company/hex/project/tests/
│   │               ├── api/
│   │               └── ui/
│   ├── pom.xml                         # Depends on all hex-core-* modules
│   └── Jenkinsfile.groovy
├── docs/
│   ├── architecture.md
│   └── prd.md
├── .gitignore
├── pom.xml                             # Parent POM
└── README.md
```
