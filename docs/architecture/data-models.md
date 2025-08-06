# Data Models

This section defines the conceptual structure of key data-holding objects within the Hex framework. These models serve as blueprints for creating consistent, maintainable, and type-safe test automation code.

### API Data Transfer Object (DTO)
**Purpose:** To provide a standardized, immutable representation of data payloads for API requests and responses. DTOs ensure type safety and decouple the test logic from the raw JSON format.

**Key Attributes (Example: `UserDTO`):**
- `id`: `Integer` - Unique identifier for the user.
- `email`: `String` - The user's email address.
- `firstName`: `String` - The user's first name.
- `lastName`: `String` - The user's last name.
- `avatar`: `String` - URL to the user's avatar image.

**Relationships:**
- DTOs can be nested within other DTOs to represent complex object graphs (e.g., a `ListUsersResponseDTO` containing a `List<UserDTO>`).
- They are primarily used by the API Service Layer and test assertions.

**Implementation Notes:**
- Will be implemented as POJOs (Plain Old Java Objects).
- Heavy reliance on Project Lombok annotations (`@Value`, `@Builder`, `@With`) to eliminate boilerplate code.
- Will use Jackson annotations for fine-grained control over JSON serialization and deserialization.
- The `jsonschema2pojo` plugin will be configured to generate these DTOs automatically from JSON schemas.

### Framework Configuration Model (Owner Interface)
**Purpose:** To provide a type-safe, hierarchical, and immutable interface for accessing configuration properties from various sources (e.g., `.properties` files, environment variables).

**Key Attributes (Example: `ApiConfig`):**
- `baseUrl()`: `String` - The base URL for the API under test.
- `port()`: `int` - The port number for the API service.
- `readTimeout()`: `int` - The timeout in milliseconds for reading a response.
- `logRequests()`: `boolean` - A flag to enable or disable request logging.

**Relationships:**
- Configuration interfaces can be composed by extending multiple parent interfaces (e.g., `WebAppConfig extends UiConfig, ApiConfig`).
- A central `ConfigFactory` will provide thread-safe, immutable instances of these configuration models to tests.

**Implementation Notes:**
- Implemented as Java interfaces using the **Owner** library.
- Annotations like `@Config.Sources`, `@Key`, and `@DefaultValue` will control property loading.
- Profiles will be used to manage different environments (e.g., `local`, `ci`).

### UI Composite Component Model
**Purpose:** To define a reusable, business-oriented UI component that encapsulates a group of web elements and their specific interactions. This is the core pattern for building scalable PageObjects.

**Key Attributes:**
- `rootElement`: `SelenideElement` - The root DOM element that contains the entire component, used for scoping.
- `childElements`: `Map<String, Element>` - A collection of named `Element` wrappers (e.g., `Button`, `Input`) that belong to this component.
- `businessLogicMethods`: `Methods` - Public methods that expose business actions (e.g., `login(user, pass)`, `search(term)`).

**Relationships:**
- Composite Components are contained within PageObjects.
- They can contain other, smaller Composite Components, allowing for complex, hierarchical UI modeling.

**Implementation Notes:**
- An abstract `BaseComponent` class will be provided in `hex-core-ui`.
- Project-specific components (e.g., `LoginForm`, `NavigationBar`) will extend this base class.
- The constructor will typically take a `SelenideElement` as the root to bind the component to a specific part of the page.
