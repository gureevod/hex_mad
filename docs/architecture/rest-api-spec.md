# REST API Spec

The Hex framework does not expose its own REST API. Instead, it provides patterns and tools to test them. This section defines a sample OpenAPI 3.0 specification that will be used for the reference implementation (`hex-project-samples`). It serves as the recommended standard for documenting APIs that will be tested with Hex.

```yaml
openapi: 3.0.0
info:
  title: "Hex Sample Project API"
  version: "1.0.0"
  description: "A sample API for demonstrating the Hex testing framework. This specification is the contract that hex-project-samples will test against."
servers:
  - url: "https://reqres.in/api"
    description: "Public mock server (reqres.in)"

paths:
  /users:
    post:
      summary: "Create a new user"
      tags:
        - "Users"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserCreateRequest'
      responses:
        '201':
          description: "User created successfully"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserCreateResponse'
        '400':
          description: "Invalid input"

  /users/{id}:
    get:
      summary: "Get a single user by ID"
      tags:
        - "Users"
      parameters:
        - name: "id"
          in: "path"
          required: true
          schema:
            type: "integer"
      responses:
        '200':
          description: "Successful operation"
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: "User not found"

components:
  schemas:
    User:
      type: "object"
      properties:
        id:
          type: "integer"
        email:
          type: "string"
          format: "email"
        first_name:
          type: "string"
        last_name:
          type: "string"
        avatar:
          type: "string"
          format: "uri"

    UserCreateRequest:
      type: "object"
      properties:
        name:
          type: "string"
        job:
          type: "string"
      required:
        - "name"
        - "job"

    UserCreateResponse:
      type: "object"
      properties:
        name:
          type: "string"
        job:
          type: "string"
        id:
          type: "string"
        createdAt:
          type: "string"
          format: "date-time"
```
