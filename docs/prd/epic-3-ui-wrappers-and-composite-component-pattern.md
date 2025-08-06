# Epic 3: UI Wrappers and Composite Component Pattern
**Epic Goal:** Deliver extensible Selenide wrappers and a composite component base so teams can implement business-centric components and compose PageObjects cleanly.

**Story 3.1: Wrapper Base and Core Elements**
As a UI test developer,
I want robust element wrappers,
so that common actions are reliable and expressive.
**Acceptance Criteria:**
1. Base `Element` with locator, waits, visibility, and common assertions with Allure steps.
2. Concrete wrappers: `Button`, `Input`, `Checkbox`, `Select`, extend base and add idiomatic actions.
3. SLF4J-based logging for actions; no direct `SelenideElement` exposure in tests.
4. Fluent assertion helpers with clear failure messages.

**Story 3.2: Composite Component Base**
As a framework user,
I want a base for composite components,
so that I can define business-level components once and reuse them.
**Acceptance Criteria:**
1. `Component` base supports nested elements, local waits, and domain methods.
2. Guidance on parametrized components and scoping (root locator).
3. Thread-safe design—no shared state; page/context passed explicitly as needed.
4. Allure step conventions for component actions.

**Story 3.3: PageObject Composition Patterns**
As a developer,
I want PageObject patterns that compose wrappers and composites,
so that tests are readable and maintainable.
**Acceptance Criteria:**
1. Sample `hex-project` PageObjects composed from wrappers and composites.
2. Clear guidance discouraging test-level raw locators.
3. Examples for synchronization patterns and common gotchas (stale elements).
4. Demonstrate assertions at component and page levels.

**Story 3.4: Assertions and Waiting Utilities**
As a tester,
I want consistent waiting and assertion utilities,
so that tests fail fast with actionable feedback.
**Acceptance Criteria:**
1. Utilities expose common conditions with timeouts and polling defaults.
2. Assertion helpers integrate with Allure and use informative messages.
3. Thread-safe configuration of timeouts per test.
4. Examples cover dynamic content (modals, toasts).

**Story 3.5: Sample Composites in `hex-project` (not in core)**
As a maintainer,
I want non-core sample composites,
so that teams see practical usage without polluting `hex-core`.
**Acceptance Criteria:**
1. Sample `Table`, `Navbar`, `Modal` in `hex-project` only.
2. Example tests demonstrate composition and business methods.
3. Docs explicitly state no concrete composites ship in `hex-core`.
4. CI runs UI examples headless.

**Story 3.6: Collection Abstractions over ElementsCollection**
As a UI developer,
I want to work with collections of elements easily,
so that I can filter, map, and assert on lists of components.
**Acceptance Criteria:**
1. A typed wrapper around `ElementsCollection` provides common operations (size, filter, find, text assertions).
2. The collection wrapper includes built-in waits and Allure step conventions.
3. The design is thread-safe with no shared mutable state.
4. Example usage is provided in `hex-project` with a Russian README.
