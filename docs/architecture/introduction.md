# Introduction

This document outlines the overall project architecture for Hex, including backend systems, shared services, and non-UI specific concerns. Its primary goal is to serve as the guiding architectural blueprint for AI-driven development, ensuring consistency and adherence to chosen patterns and technologies.

**Relationship to Frontend Architecture:**
This document focuses on the core framework modules (`hex-core-api`, `hex-core-ui`, `hex-core-testing`). While it defines the patterns for UI testing, it is not a frontend application architecture. The UI components referenced are wrappers and composites for testing web applications, not building them.

### Starter Template or Existing Project
N/A - This is a greenfield project. The architecture defined herein will establish the foundational multi-module Maven structure that will serve as the "starter" for any new testing projects adopting the Hex framework.

### Change Log
| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-10-27 | 1.0 | Initial architecture draft | Winston (Architect) |
