# User Interface Design Goals

### Overall UX Vision
- Provide a clean, discoverable abstraction layer over Selenide with minimal ceremony, consistent naming, and predictable behavior.
- Encourage composition: small element wrappers plus user-defined composite components encapsulating business logic usable within PageObjects.
- Make failure states actionable: enriched messages, Allure steps, and logs that pinpoint locator, condition, and context.

### Key Interaction Paradigms
- Wrapper-first approach: `Button`, `Input`, `Checkbox`, `Select`, etc., exposing common actions with built-in waits and assertions.
- Composite components pattern: user-defined `Component` base enabling nested elements, local waits, domain methods, and reusability.
- PageObjects composed of wrappers and composites; discourage raw `SelenideElement` usage in tests.
- Structured assertions with clear, fluent APIs and Allure steps.
