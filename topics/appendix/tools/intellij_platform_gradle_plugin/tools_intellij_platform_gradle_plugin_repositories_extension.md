<!-- Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license. -->

# Repositories Extension

<link-summary>IntelliJ Platform Gradle Plugin repositories extension.</link-summary>

<include from="tools_intellij_platform_gradle_plugin.md" element-id="EAP_Status"/>

This is an extension class for managing IntelliJ Platform repositories in a Gradle build script.
It's applied to the `RepositoryHandler`.

Available in both `Project` scope and Gradle Settings for `DependencyResolutionManagement`.

It provides methods to add:

- IntelliJ Platform repositories (for releases, snapshots, and nightly builds)
- JetBrains Marketplace repository for fetching plugins
- JetBrains Runtime repository
- Android Studio and IntelliJ Platform binary release repositories (for IntelliJ Plugin Verifier)
- Ivy local repository (for correct access to local dependencies)

<snippet id="recommendedCallout">

> In most cases, simply using [`defaultRepositories()`](tools_intellij_platform_gradle_plugin_repositories_extension.md#default-repositories) repository will be sufficient.
>
{style="tip"}

**Example:**

Setup Maven Central and [`defaultRepositories()`](tools_intellij_platform_gradle_plugin_repositories_extension.md#default-repositories) repositories:

```kotlin
repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}
```

</snippet>

## Default Repositories

The default repository definition suitable for most plugins.

| Function                | Description                                                                                                                           |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `defaultRepositories()` | Applies a set of recommended repositories required for running the most common tasks provided by the IntelliJ Platform Gradle Plugin. |

It includes:
- `localPlatformArtifacts()` — required to use plugins bundled with IntelliJ Platform or refer to the local IDE
- `releases()` and `snapshots()` — IntelliJ Platform releases channels
- `marketplace()` — JetBrains Marketplace plugins repository
- `binaryReleases()` — JetBrains IDEs releases required for running the IntelliJ Plugin Verifier


## IDE Releases

| Function      | Description                                                                                 |
|---------------|---------------------------------------------------------------------------------------------|
| `releases()`  | Adds a repository for accessing IntelliJ Platform stable releases.                          |
| `snapshots()` | Adds a repository for accessing IntelliJ Platform snapshot releases.                        |
| `nightly()`   | Adds a repository for accessing IntelliJ Platform nightly releases, not available publicly. |

See also:
- [](intellij_artifacts.md)

## Binary IDE Releases

| Function                        | Description                                                                                                  |
|---------------------------------|--------------------------------------------------------------------------------------------------------------|
| `binaryReleases()`              | Adds a repository for accessing IntelliJ Platform IDE binary releases for use with IntelliJ Plugin Verifier. |
| `binaryReleasesAndroidStudio()` | Adds a repository for accessing Android Studio binary releases for use with IntelliJ Plugin Verifier.        |

See also:

- [](verifying_plugin_compatibility.md)

## Additional Repositories

| Function                   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `localPlatformArtifacts()` | Certain dependencies, such as the [local IntelliJ Platform instance](tools_intellij_platform_gradle_plugin.md#dependenciesLocalPlatform) and bundled IDE plugins, need extra pre-processing before they can be correctly used by the IntelliJ Platform Gradle Plugin and loaded by Gradle. <p>This pre-processing involves generating XML files that detail these specific artifacts. Once created, these are stored in a unique custom [Ivy](https://ant.apache.org/ivy/) repository directory.</p> |
| `jetbrainsRuntime()`       | Adds a repository for accessing [JetBrains Runtime](ide_development_instance.md#using-a-jetbrains-runtime-for-the-development-instance) releases.                                                                                                                                                                                                                                                                                                                                                    |
| `marketplace()`            | Adds a repository for accessing plugins hosted on [JetBrains Marketplace](https://plugins.jetbrains.com).                                                                                                                                                                                                                                                                                                                                                                                            |

See also:

- [](plugin_dependencies.md)

<snippet id="localPlatformArtifacts_required">

> Note that the [`localPlatformArtifacts()`](tools_intellij_platform_gradle_plugin_repositories_extension.md#additional-repositories)
> entry needs to be added to the `repositories {}` block as all local dependencies (bundled plugins, local IDE, etc.) require it.
>
> It is applied automatically when using the [`defaultRepositories()`](tools_intellij_platform_gradle_plugin_repositories_extension.md#default-repositories) entry.
>
{style="tip" title="localPlatformArtifacts() and defaultRepositories()"}

</snippet>

<include from="snippets.md" element-id="missingContent"/>
