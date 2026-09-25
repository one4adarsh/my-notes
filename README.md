# my-notes

Multi-module Gradle project (Kotlin DSL) for interview-prep notes and hands-on Java practice
covering high-level design (HLD) and low-level design (LLD).

## Prerequisites

- **JDK 21** – the build uses a Java toolchain; if JDK 21 is not installed, the
  [foojay-resolver](https://github.com/gradle/foojay-toolchains) plugin downloads it automatically.
- **Gradle** – not required locally. Use the included wrapper (`./gradlew`, Gradle 9.5.1).

## Project structure

```
my-notes/
├── app/                              # notes (Markdown) + sample Java CLI
│   ├── build.gradle.kts
│   └── src/main/java/org/example/
│       ├── domain_1_high_level_design/    # HLD notes, start at HLD.md
│       └── domain_2_low_level_design/     # LLD notes (patterns, principles, refactoring)
├── lld-design-patterns/              # runnable design-pattern implementations
│   ├── build.gradle.kts
│   └── src/main/java/org/example/
│       └── behavioural/strategy/          # each pattern has its own package + README
├── lld-jira/                         # Jira-like LLD exercise (spec-driven via OpenSpec)
│   ├── build.gradle.kts
│   ├── openspec/                          # OpenSpec config
│   └── src/{main,test}/java/org/example/lldjira/
├── gradle/
│   ├── libs.versions.toml            # shared dependency version catalog
│   └── wrapper/
├── gradle.properties                 # Gradle settings (configuration cache enabled)
└── settings.gradle.kts               # root project name + included modules
```

## Modules

| Module                | Main class                      | Description                                                                 |
|-----------------------|---------------------------------|-----------------------------------------------------------------------------|
| `app`                 | `org.example.App`               | Markdown notes for HLD & LLD, plus a sample CLI (uses Guava)                |
| `lld-design-patterns` | `org.example.LldDesignPatterns` | Runnable examples of design patterns, one package + README per pattern      |
| `lld-jira`            | `org.example.lldjira.LldJira`   | Jira-like system LLD exercise; changes are proposed and tracked via OpenSpec |

## Notes

Notes live as Markdown under `app/src/main/java/org/example/` and are organised as
`domain_N_<name>/module_N_<name>/_NN_<topic>.md` (sub-topics use `_NN~MM_<subtopic>.md`).

- **High-level design** – start at
  [`domain_1_high_level_design/HLD.md`](app/src/main/java/org/example/domain_1_high_level_design/HLD.md),
  which links every module and topic (delivery framework, CAP theorem, consistency patterns,
  scalability/reliability/availability, failover & disaster recovery, ...).
- **Low-level design** – under
  [`domain_2_low_level_design/`](app/src/main/java/org/example/domain_2_low_level_design/):
  design patterns (creational, structural, behavioural), design principles, OOP & SOLID,
  and refactoring principles.

## Design pattern examples

`lld-design-patterns` contains a package per pattern (e.g. `behavioural/strategy`) with a `Main`
class and a `README.md` explaining the roles and thought process. Run a specific demo by passing
its main class:

```sh
./gradlew :lld-design-patterns:run -PmainClass=org.example.behavioural.strategy.Main
```

Without `-PmainClass` the module's default entry point (`org.example.LldDesignPatterns`) runs.

## Build & run

```sh
./gradlew build
./gradlew :app:run
./gradlew :lld-design-patterns:run
./gradlew :lld-jira:run
```

> **Note:** when copy-pasting a command that has a trailing `# comment` into an interactive
> zsh shell, drop the comment (or `setopt INTERACTIVE_COMMENTS`), otherwise zsh passes `#`
> to Gradle as a task name.

## Testing

```sh
./gradlew test               # run tests in all modules
./gradlew :lld-jira:test     # run tests for a single module
```

HTML reports are written to `<module>/build/reports/tests/test/index.html`.

## Dependency management

Dependency versions are centralised in `gradle/libs.versions.toml`. To add a library:

1. Add a version under `[versions]` and an entry under `[libraries]`:

   ```toml
   [versions]
   gson = "2.11.0"

   [libraries]
   gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
   ```

2. Reference it from any module's `build.gradle.kts`:

   ```kotlin
   dependencies {
       implementation(libs.gson)
   }
   ```

## Adding a new module

1. Create the module directory with a `build.gradle.kts` (copy `lld-jira/build.gradle.kts` as a starting point).
2. Add sources under `<module>/src/main/java` and tests under `<module>/src/test/java`.
3. Register the module in `settings.gradle.kts`:

   ```kotlin
   include("my-new-module")
   ```

4. Verify with `./gradlew :my-new-module:build`.

## Adding a new design pattern example

1. Create a package under `lld-design-patterns/src/main/java/org/example/<category>/<pattern>/`
   (categories: `creational`, `structural`, `behavioural`).
2. Add the pattern classes, a `Main` class demonstrating usage, and a `README.md` describing the
   roles, steps, and example (see `behavioural/strategy/README.md`).
3. Run it with `./gradlew :lld-design-patterns:run -PmainClass=org.example.<category>.<pattern>.Main`.

## Useful Gradle commands

```sh
./gradlew projects                 # list all modules
./gradlew tasks                    # list available tasks
./gradlew clean                    # remove build outputs
./gradlew :app:installDist         # create a runnable distribution in app/build/install/app
./gradlew :app:distZip             # package the app as a zip
```

## Configuration notes

- The [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html) is
  enabled in `gradle.properties` for faster builds. If you hit build-script issues, retry with
  `--no-configuration-cache` to rule it out.

## IDE setup

Open the root folder in IntelliJ IDEA or VS Code (with the Gradle extension). The Gradle import
picks up all included modules automatically; no per-module setup is required.
