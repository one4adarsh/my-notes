# my-notes

Multi-module Gradle project (Kotlin DSL) for Java notes and low-level design (LLD) practice.

## Prerequisites

- **JDK 21** – the build uses a Java toolchain; if JDK 21 is not installed, the
  [foojay-resolver](https://github.com/gradle/foojay-toolchains) plugin downloads it automatically.
- **Gradle** – not required locally. Use the included wrapper (`./gradlew`, Gradle 9.5.1).

## Project structure

```
my-notes/
├── app/                      # sample Java CLI application
│   ├── build.gradle.kts
│   └── src/{main,test}/java/org/example/
├── lld-jira/                 # Hello World starter for a Jira-like LLD exercise
│   ├── build.gradle.kts
│   └── src/{main,test}/java/org/example/lldjira/
├── gradle/
│   ├── libs.versions.toml    # shared dependency version catalog
│   └── wrapper/
├── gradle.properties         # Gradle settings (configuration cache enabled)
└── settings.gradle.kts       # root project name + included modules
```

## Modules

| Module     | Main class                    | Description                                         |
|------------|-------------------------------|-----------------------------------------------------|
| `app`      | `org.example.App`             | Sample Java CLI application (uses Guava)            |
| `lld-jira` | `org.example.lldjira.LldJira` | Simple Hello World; placeholder for a Jira-like LLD |

## Build & run

```sh
./gradlew build              # compile + test all modules
./gradlew :app:run
./gradlew :lld-jira:run
```

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
