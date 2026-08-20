# REST API Test Automation with Java & REST Assured

A Java-based API test automation project using REST Assured, focused on functional API testing, negative scenarios, contract/schema validation, data-driven testing, performance testing, CI/CD and automated reporting.

The project demonstrates a layered approach to API test automation against publicly available REST APIs, with an emphasis on test coverage, maintainability, risk-based decisions and reliable reporting.

[![Allure Report](https://img.shields.io/badge/Allure_Report-view%20results-orange)](https://magdau.github.io/restAssured-test-with-extends/)
[![CI](https://github.com/magdaU/restAssured-test-with-extends/actions/workflows/allure-report.yml/badge.svg)](https://github.com/magdaU/restAssured-test-with-extends/actions/workflows/allure-report.yml)

**Live Allure Report:** https://magdau.github.io/restAssured-test-with-extends/  
**Test Strategy:** see [TEST_STRATEGY.md](TEST_STRATEGY.md) for the reasoning behind what's tested, how, and what's deliberately out of scope.

---

## 🎯 What This Project Demonstrates

- **API testing** — functional, negative, and boundary-value/parameterized testing (CRUD, status codes, response fields, response time)
- **Contract validation** — JSON Schema and XSD validation against live responses
- **Query & data** — JsonPath/XmlPath/GPath querying, POJO (de)serialization with Jackson
- **Non-functional testing** — k6 load testing with an SLA-aligned latency threshold
- **Engineering practices** — CI/CD (GitHub Actions), Allure reporting with historical trend, runtime token injection, risk-based test scoping

---

## 📋 Technology Stack

| Technology            | Version  |
|-----------------------|----------|
| Java                  | 18       |
| Maven                 | 3.x      |
| REST Assured          | 5.3.0    |
| JUnit                 | 4.13.2   |
| Jackson Databind      | 2.14.2   |
| JSON Schema Validator | 5.3.0    |
| Allure Report         | 2.27.0   |
| k6                    | latest   |

---

## 🏗️ Project Structure

```
src/
├── test/
│   ├── java/
│   │   ├── config/           # Base configs, endpoints, and the main CRUD/schema/negative/parameterized test classes
│   │   ├── objects/          # VideoGame POJO
│   │   ├── FootbalTests.java
│   │   ├── GpathJSONTest.java
│   │   ├── GpathXMLTests.java
│   │   └── MyFirstVideoGame.java
│   └── resources/            # JSON Schema + XSD for response validation
performance/
└── videogame-load-test.js    # k6 load test
```

---

## 🎮 APIs Under Test

**Video Game DB** — `https://videogamedb.uk/api/v2/` ([Swagger](https://videogamedb.uk/swagger-ui/index.html)). Public, read-only sandbox — writes are accepted but never persisted. Covered by `VideoGameTests`, `VideoGameNegativeTests`, `VideoGameParameterizedTests`, `VideoGameNegativeParameterizedTests`, `GpathJSONTest`, `GpathXMLTests` — CRUD, JSON Schema/XSD validation, POJO (de)serialization, GPath querying, negative and parameterized cases.

**Football Data** — `https://api.football-data.org/v4/` ([docs](https://www.football-data.org/)). Requires a free API token; without one, `FootbalTests` returns HTTP 403. Free tier is rate-limited to 10 requests/minute.

```powershell
$env:FOOTBALL_DATA_API_TOKEN="your-token"
mvn -Dtest=FootbalTests test
```

---

## ▶️ Running Tests

```powershell
mvn test                                                              # all tests
mvn -Dtest=VideoGameTests test                                        # one class
mvn "-Dtest=VideoGameTests,GpathJSONTest,GpathXMLTests,MyFirstVideoGame" test   # no token required
```

**Allure report:**
```powershell
mvn -Dtest=VideoGameTests test
mvn allure:report     # HTML → target/site/allure-maven-plugin/
mvn allure:serve      # open in browser
```

**k6 load test** (VideoGame DB only — Football API is token-gated and rate-limited, so it's excluded):
```powershell
k6 run performance/videogame-load-test.js
```

| Test class | Tests | Stable | Known failure causes |
|---|---|---|---|
| `VideoGameTests` | 14 | ✅ | None |
| `VideoGameNegativeTests` | 4 | ✅ | None |
| `VideoGameParameterizedTests` | 5 | ✅ | None |
| `VideoGameNegativeParameterizedTests` | 4 | ✅ | None |
| `FootbalTests` | 12 | ⚠️ | HTTP 403 (no token), 429 (rate limit), 500 (transient) |
| `GpathJSONTest` | 5 | ✅ | None |
| `GpathXMLTests` | 5 | ✅ | None |
| `MyFirstVideoGame` | 2 | ✅ | None |
| **Total** | **51** | | |

---

## 🚀 CI/CD & Reporting

Every push to `main` runs the non-Football suite via GitHub Actions and publishes an Allure report — with historical trend, Environment, and Categories widgets — to GitHub Pages (`gh-pages` branch). CI also runs (tests only, no deploy) on `feature/**` and `fix/**` branches.

The k6 load test runs on manual trigger only (`Actions → k6 Load Test → Run workflow`), to avoid hammering the shared sandbox API.

---

## 🔧 Useful Tools

| Tool                | Description                             | Link                                    |
|---------------------|-----------------------------------------|-----------------------------------------|
| jsonschema2pojo     | Generate POJOs from JSON / JSON Schema  | https://www.jsonschema2pojo.org/        |
| freeformatter       | Formatters, validators, minifiers       | https://freeformatter.com/              |
| jsonschemavalidator | Interactive JSON Schema validator       | https://www.jsonschemavalidator.net/    |

---

## ✅ Completed Improvements

| # | Improvement |
|---|-------------|
| 1 | Runtime API token injection for Football API |
| 2 | Rate limit throttling for Football API (free tier 10 req/min) |
| 3 | Position-independent assertion in `getFirstTeamName` |
| 4 | Allure reporting integration (JUnit 4 + REST Assured filter) |
| 5 | Automated Allure report published to GitHub Pages via CI |
| 6 | CI runs on `feature/**` and `fix/**` branches (tests only, no deploy) |
| 7 | Negative tests — 404, invalid body, null fields (`VideoGameNegativeTests`) |
| 8 | `@Step` annotations in Allure for multi-step tests |
| 9 | Parameterized tests — valid and invalid IDs (`@RunWith(Parameterized.class)`) |
| 10 | Allure historical trend — `history/` preserved between CI runs |
| 11 | k6 performance/load testing for VideoGame DB API |
| 12 | Allure report Environment and Categories widgets |

---

## 📚 Project Background

The first version of this project started from the [Rest Assured Fundamentals](https://www.udemy.com/course/rest-assured-fundamentals/?referralCode=2A76479D71A62609414D) course on Udemy. Since then it's been independently developed and extended well past the original tutorial scope — Allure reporting with historical trend, GitHub Actions CI, k6 load testing, negative and parameterized tests, contract/schema validation, dynamic Football API token handling, and a full test strategy, among other things (see [Completed Improvements](#-completed-improvements) above and [TEST_STRATEGY.md](TEST_STRATEGY.md)).
