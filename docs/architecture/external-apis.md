# External APIs

The Hex framework itself does not have mandatory dependencies on external APIs for its core functionality. However, it is designed to test applications that do, and it provides a standardized pattern for integrating with third-party services like HashiCorp Vault for secrets management.

### HashiCorp Vault API
- **Purpose:** To securely retrieve secrets (e.g., API keys, passwords, tokens) at runtime, avoiding the need to store them in source control or CI/CD environment variables. This is the recommended pattern for all secrets management.
- **Documentation:** [https://developer.hashicorp.com/vault/api-docs](https://developer.hashicorp.com/vault/api-docs)
- **Base URL(s):** To be provided by the consuming project's environment configuration.
- **Authentication:** Token-based authentication. The Vault token itself will be injected into the CI environment securely.
- **Rate Limits:** Dependent on the project's specific Vault deployment and policies.
- **Key Endpoints Used:**
  - `GET /v1/secret/data/{path}` - To read secrets from a key-value store.
- **Integration Notes:**
  - A `VaultService` helper class will be provided as a reference implementation within the `hex-project-samples`.
  - This service will not be part of `hex-core` to keep the core framework dependency-free of a specific secrets manager client.
  - The Owner configuration library will be integrated with this pattern, allowing properties to be sourced directly from Vault (e.g., `@Sources({"vault:/secret/data/my-app/config"})`). This will be documented as the primary approach.
