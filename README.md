# UITAP Automation

A test automation portfolio built on two practice sites. The same 22 UI scenarios from [uitestingplayground.com](http://uitestingplayground.com) are solved twice, with **Python + Playwright** and **Java + Selenium**, and the REST API of [automationexercise.com](https://automationexercise.com) is tested twice too, with **Python + requests** and **Java + REST Assured**. All suites report through Allure and run automatically in CI.

[![Python Playwright Tests](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/python-playwright.yml/badge.svg)](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/python-playwright.yml)
[![Python API Tests](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/python-api.yml/badge.svg)](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/python-api.yml)
[![Java Selenium Tests](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/java-selenium.yml/badge.svg)](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/java-selenium.yml)
[![Java API Tests](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/java-api.yml/badge.svg)](https://github.com/filip-wierzbicki-qa/uitap-automation/actions/workflows/java-api.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

![Allure graphs for the API suite: 100% pass rate, trend history and durations](docs/allure-overview.png)

**📊 Live Allure reports:** https://filip-wierzbicki-qa.github.io/uitap-automation/

## Why two stacks

Testing the same scenarios in two languages and two frameworks forces an honest comparison: where Playwright's auto-waiting removes boilerplate Selenium needs explicit `WebDriverWait` for, where TestNG's XML suite configuration differs from pytest markers, how BDD (Cucumber vs Behave) changes the shape of a test. It's also just more representative of real teams, where you rarely get to pick one tool forever.

## Tech stack

| | Python | Java |
|---|---|---|
| Browser automation | Playwright | Selenium 4 |
| API testing | requests | REST Assured |
| Test runner | pytest | TestNG |
| BDD | Behave | Cucumber |
| Reporting | Allure | Allure |
| Build/deps | `requirements.txt` (pinned) | Maven |

## API test suites (`python-api/`, `java-api/`)

### Python + requests (`python-api/`)

16 tests against the automationexercise.com REST API: products, search, brands, login and a full account lifecycle.

- **API client** (`api_client.py`): one class wraps a `requests.Session` with the base URL and timeout. Tests call methods like `api.search_products("top")` and never build URLs, the same idea as the Page Object Model, one layer down.
- **Fixtures**: one shared client for the whole run; a fresh account per test where tests change data.
- **Test data with cleanup**: every account gets a unique email (`uuid`) and is deleted by a `yield` fixture, even when the test fails.
- **Data-driven and negative tests**: parametrized search terms, missing login fields, unsupported methods, duplicate accounts.

### Java + REST Assured (`java-api/`)

12 tests ported from the Python suite: products, search, login and the account lifecycle.

- **API client** (`AutomationExerciseClient`): a shared `RequestSpecification` (base URI, base path, Allure filter) plays the role of the Python `Session`. Tests read like `given / when / then` and only assert, the client only calls.
- **Setup and teardown**: `@BeforeMethod` creates a fresh account and checks it really was created; `@AfterMethod(alwaysRun = true)` deletes it, even when the test fails.
- **Data-driven tests**: TestNG `@DataProvider` for search terms and missing login fields, the twin of `pytest.mark.parametrize`.
- **Allure REST Assured filter**: every request and response is attached to the report, so a failure shows the full HTTP exchange.

**Finding:** this API reports errors with HTTP `200 OK` and puts the real status (`405`, `400`, `404`) in the JSON body. Both suites assert both layers, so they would notice if the API ever started using real status codes.

## CI/CD

Four workflows run on GitHub Actions: `python-playwright.yml`, `java-selenium.yml`, `python-api.yml` and `java-api.yml`.

- **On every push** to any branch, and **nightly** (UI suites at 02:00 UTC, API suites at 03:00 and 03:30 UTC).
- **Manually** via `workflow_dispatch`, with dropdowns for browser (chromium/firefox/webkit for Python, chrome/firefox for Java) and test group (`smoke` or `regression`).
- The API suites need no browser and finish in under a minute, so they run their **full regression on every push**; the slower UI suites run smoke on push and full regression nightly.
- On `main`, every pipeline publishes its Allure report to GitHub Pages (`/python/`, `/java/`, `/api/`, `/java-api/`), each with its own trend history. A shared storage branch (`gh-pages-src`) keeps each report in its own subfolder, so no deploy overwrites another.

## Architecture & patterns (Java side)

- **Page Object Model**: `pages/` wraps each page's elements and actions; tests never touch Selenium locators directly.
- **Factory** (`DriverFactory`): one static method builds the right `WebDriver` for the requested browser and headless mode.
- **Singleton** (`DriverManager`): holds exactly one driver instance per test run, exposed via `getDriver()`, torn down via `quitDriver()`.
- **Hooks** (`stepdefinitions/Hooks.java`): Cucumber's `@Before`/`@After` lifecycle wires the Singleton driver into each BDD scenario.

## How to run locally

### Python (Playwright)
```bash
cd python-playwright
python -m venv .venv
.venv\Scripts\activate          # Windows; use "source .venv/bin/activate" on macOS/Linux
pip install -r requirements.txt
playwright install

pytest -m smoke                 # smoke suite
pytest -m regression            # full regression
behave features/                # BDD scenarios

allure serve allure-results     # view the report locally (needs Node.js + `npm install -g allure-commandline`)
```

### Java (Selenium)
```bash
cd java-selenium
mvn test                                        # full TestNG suite (testng.xml)
mvn test -Dtest=TestRunner                      # Cucumber BDD scenarios
mvn test -Dgroups=smoke -Dbrowser=chrome        # smoke only, explicit browser

allure open allure-report                       # after generating with `allure generate allure-results -o allure-report`
```

### Python (API)
```bash
cd python-api
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt

pytest -v                       # all 16 API tests
pytest -m smoke -v              # smoke only
```

### Java (API)
```bash
cd java-api
mvn test                        # all 12 API tests
mvn test -Dgroups=smoke         # smoke only
mvn test -Dtest=AccountTest     # one test class

allure serve allure-results     # view the report locally
```

## What I learned

A few things from building and maintaining this repo that don't show up in a green checkmark:

- **A flaky test is a symptom, not a verdict.** `ProgressBarTest` failed intermittently for weeks before I isolated the cause: the progress bar's animation speed is randomized per page load (1.4s to 25s observed), and the default 500ms `WebDriverWait` polling interval let fast bars blow straight past the target percentage. Fixed by moving both detection and the stop-click into the browser via `executeAsyncScript` and `requestAnimationFrame`, eliminating the WebDriver round-trip entirely. Verified with 40 consecutive runs at a tightened tolerance before trusting it.
- **"It passed" and "it did what I intended" are different claims.** A code review of an already-green Cucumber run caught a real bug: the `browser` system property was being read but never actually passed to the driver factory, every CI run was silently using the hardcoded default.
- **Headless isn't one thing.** `ShadowDomTest`'s clipboard check was guarded against the wrong kind of "headless", JVM headless (`GraphicsEnvironment.isHeadless()`) instead of the browser's own headless flag, two genuinely different conditions that happened to usually agree, until they didn't.
- **Not every test works in every browser.** Chromium exposes clipboard permissions Playwright can grant; Firefox doesn't. `test_22_shadowdom.py` now skips cleanly on non-Chromium browsers instead of failing with a confusing permissions error.
- **Status codes can lie.** The practice API answers "method not supported" with HTTP 200. A test that only checks the status code would pass on every error, so the API tests always check the body too.
- **A silenced error is still an error.** A history download ended in `|| true` (on purpose, the first run has no history yet), so when its URL broke, nothing complained: the Python trend graph quietly reset on every run. Now I check the result some other way whenever I hide errors.
- **Headers can lie too.** The API sends JSON labelled `Content-Type: text/html`. Python's `response.json()` ignores the label, but REST Assured picks its parser from it, so every body check failed until I registered a JSON parser for `text/html`. I proved the fix by removing it on purpose and watching the body checks fail while the status check still passed.
- **Test the test infrastructure.** A retry for 5xx answers can't be checked by waiting for the demo site to break, so I temporarily treated `200` as retryable and watched the retries happen. The first version (a REST Assured filter) crashed on the second attempt, a bug that would otherwise have appeared only on the next real outage.

## Known limitations

- **Shadow DOM clipboard test** skips in headless browsers and on non-Chromium browsers: there is no real clipboard to read there.
- **Python Alerts test** is skipped: the page's JavaScript timer is throttled in headless Chromium, which makes the test unreliable in CI. The Java version covers the scenario.
- **Bot protection on the demo API**: automationexercise.com sometimes answers CI runners with a bot-protection page (HTTP 200 with HTML instead of JSON). All API tests then fail with a JSON parse error; a later run passes without changes.

## License

MIT, see [LICENSE](LICENSE).
