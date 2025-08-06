# Database Schema

The Hex framework is a test automation tool and, as such, does not have its own production database or schema. It is designed to be database-agnostic, capable of testing applications regardless of their underlying data storage technology (e.g., PostgreSQL, MySQL, MongoDB, etc.).

### Architectural Assumptions for Consuming Projects
- **Data Seeding & Teardown:** Projects adopting Hex are responsible for their own test data management strategies. This typically involves seeding a known data state before a test run and tearing it down afterward.
- **Data Access:** The framework will interact with data indirectly through the application's UI or API. There will be no direct database connections from the test code itself.
- **Test Data Generation:** The `JavaFaker` library, included in `hex-project-samples`, is the recommended tool for generating realistic and random test data for API payloads and UI form inputs. This avoids hard-coded data and improves test coverage.
