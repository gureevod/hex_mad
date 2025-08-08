# Epic 5: Migration Toolkit and Compatibility
**Epic Goal:** Reduce friction for existing teams by providing compatibility guidance, examples, and a maintained version matrix.

**Story 5.1: Compatibility Matrix and Versioning**
As a maintainer,
I want a compatibility matrix,
so that teams know supported versions across libraries.
**Acceptance Criteria:**
1. Matrix for Java 17, Selenide, RestAssured, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, AssertJ, and jsonschema2pojo is created.
2. Semantic versioning and upgrade notes for `hex-core` modules are documented.
3. Example upgrade path notes for minor/patch bumps are provided.
4. The matrix is published in the repository documentation in Russian.

**Story 5.2: Migration Guides and Patterns**
As a migrating team,
I want guides to convert ad hoc wrappers/services,
so that I can adopt Hex incrementally.
**Acceptance Criteria:**
1. Patterns for mapping existing wrappers to Hex wrapper base.
2. Service refactoring guide to `BaseService` pattern.
3. API-first adoption walkthrough before UI.
4. CI migration notes using the Jenkinsfile quickstart.

**Story 5.3: Samples for Incremental Adoption**
As a team lead,
I want minimal samples,
so that we can adopt one layer at a time.
**Acceptance Criteria:**
1. `hex-samples-api-only` and `hex-samples-ui-api` modules are provided.
2. Clear READMEs in Russian on adding dependencies and running tests.
3. Allure/Logback/Owner configuration examples included.
4. Parallel run notes and troubleshooting.