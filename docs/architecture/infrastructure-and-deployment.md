# Infrastructure and Deployment

This section describes the continuous integration (CI) and artifact management strategy for the Hex framework. The primary goal is to automate the build, test, and publishing lifecycle.

### Infrastructure as Code
- **Tool:** N/A. The framework itself does not require infrastructure provisioning. The consuming projects are responsible for their own application infrastructure.
- **Location:** N/A.
- **Approach:** The `Jenkinsfile.groovy` provided in the `hex-project-samples` module serves as a "pipeline as code" definition for executing tests within a pre-existing Jenkins environment.

### Deployment Strategy
- **Strategy:** The "deployment" for `hex-core-*` modules is a **publication** to a Maven artifact repository (e.g., Nexus, Artifactory). For the `hex-project-samples`, the "deployment" is the execution of tests and **publication** of test reports.
- **CI/CD Platform:** Jenkins, as specified in the PRD (FR7).
- **Pipeline Configuration:** `hex-project-samples/Jenkinsfile.groovy`. This file will define a declarative pipeline with stages for:
    1.  **Checkout:** Cloning the monorepo.
    2.  **Build:** Compiling all modules and running unit tests (`mvn clean install`).
    3.  **Test Execution (Parallel Matrix):** Running integration/E2E tests from `hex-project-samples` using Maven profiles or test tags to split workloads (e.g., `api-tests`, `ui-tests`).
    4.  **Reporting:** Aggregating test results and publishing the Allure report.
    5.  **Publish (Optional):** A stage to publish the `hex-core-*` JARs to a Maven repository, triggered on version changes.

### Environments
- **local:** Developer machine. Configuration is driven by `src/main/resources/hex.properties` and can be overridden by system properties.
- **ci:** Jenkins agent environment. Configuration is driven by `hex-ci.properties` and environment variables injected by the Jenkins pipeline.

### Environment Promotion Flow
The promotion flow applies to the `hex-core` artifacts, not a deployed application.
```
Development (Local SNAPSHOT) -> CI Build (SNAPSHOT) -> Manual Release -> Publish to Maven Repo (Release Version)
```

### Rollback Strategy
- **Primary Method:** Rollback is managed through dependency management. If a bug is found in a released version of a `hex-core` module, projects can revert to the previous stable version in their `pom.xml` file.
- **Trigger Conditions:** Critical bug discovered post-release, major incompatibility found with a consuming project.
- **Recovery Time Objective:** Near-instantaneous, limited only by the time to update a `pom.xml` and rebuild the consuming project.
