# ksp-mockup
Ksp mockup is simple ksp ([Kotlin Symbol Processing](https://kotlinlang.org/docs/ksp-overview.html#supported-libraries)) 
library for generating fake mockup data. These are handy  and convenient to be used in @Preview 
functions in Jetpack Compose applications. Instead of creating @PreviewParameterProvider just get 
mockup data with Mockup object.

## Add Library
Add maven repository and library dependency to your app's gradle files. Make sure to add ksp plugin too.

**Project's build.gradle.ksp**
```kotlin
// Project's build.gradle.kts make sure to keep compatible ksp version with your kotlin version 
plugins {
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}
```

**Project's settings.gradle.kts**
```kotlin
// Adds maven 
dependencyResolutionManagement {
    repositories {
        maven(url="https://jitpack.io")
    }
}
```

**Application's module build.gradle.kts**
```kotlin
// Apply ksp plugin
plugins {
    id("com.google.devtools.ksp")
}

ksp {
    //Optional: generated providers also implement PreviewParameterProvider
    arg("mockup.usePreviewParameterProviders", "true")
}

dependencies {
    // Always use the same version for all Mockup artifacts.
    val mockupVersion = "2.0.0"

    // @Mockup models and Mockup calls in src/main previews must compile in every variant,
    // but neither artifact is packaged in release builds.
    compileOnly("com.github.miroslavhybler:mockup-annotations:$mockupVersion")
    compileOnly("com.github.miroslavhybler:mockup-core:$mockupVersion")

    // Android Studio previews and debug runtime code need the core implementation.
    debugRuntimeOnly("com.github.miroslavhybler:mockup-core:$mockupVersion")

    // Only when instrumentation tests call Mockup directly:
    androidTestImplementation("com.github.miroslavhybler:mockup-core:$mockupVersion")

    // Likewise, only when local JVM tests call Mockup directly:
    testImplementation("com.github.miroslavhybler:mockup-core:$mockupVersion")

    // Concrete providers and data are generated only for debug.
    kspDebug("com.github.miroslavhybler:mockup-processor:$mockupVersion")
}
```

This is the recommended release-safe setup for 2.x. `Mockup` is a stable type from
`mockup-core`, so preview functions in `src/main` can compile for release through `compileOnly`
without running the processor or packaging the library. `debugRuntimeOnly` then makes the same
type available when a preview is rendered or debug code is executed. Generated providers and
their data exist only in the debug variant because the processor is attached with `kspDebug`.

Apply this configuration to every module that owns `@Mockup` models. In a multi-module project,
using `kspDebug` only in the application does not generate providers for models owned by a library
module.

Use the generic core API (`Mockup.get<T>()`, `Mockup.getList<T>()`, and similar methods) from
shared `src/main` preview code. Concrete provider classes and legacy generated accessors such as
`UserMockupProvider` or `Mockup.userMockupProvider` exist only in debug, so references to them must
live under `src/debug` (or be replaced by the generic API). If a showcase or internal tool also uses
mockups at debug runtime, isolate that integration in `src/debug`; ordinary preview-only apps do not
need a runtime data-source wrapper or release fallback.

If every `Mockup` call already lives under `src/debug`, `debugImplementation(mockup-core)` can be
used instead of the `compileOnly` plus `debugRuntimeOnly` pair.

### Executable example versus a real app

Real applications do not need `ShowcaseDataSource` or release source-set fallbacks. Their screens
should receive state from REST APIs, databases, repositories, use cases, or other production data
sources, while `Mockup` remains confined to Compose preview functions.

The executable `example-app` is deliberately different: it has no backend or domain layer but still
needs populated screens to showcase the generated values. Its debug-only `ShowcaseDataSource` uses
`Mockup` for that purpose. Release and benchmark implementations return empty lists, demonstrating
that this showcase shortcut does not add generated data or `mockup-core` to production runtime.

`mockup.usePreviewParameterProviders` defaults to `false`.
When it is `false`, generated providers are plain `MockupDataProvider`s and the generating module does not need
`androidx.compose.ui:ui-tooling-preview`.
When it is `true`, generated providers also implement `PreviewParameterProvider`, so that module must include the
Compose preview dependency.

### Usage
Annotate desired classes with @Mockup and that's it

```kotlin
@Mockup
data class User constructor(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    
    //Use IgnoreOnMockup annotation for properties that should be excluded from mockup generation
    @IgnoreOnMockup
    val registrationDate: DateTime?  = null
}
```

Build the debug variant and access its generated providers through the `Mockup` object from
`mockup-core`.

```kotlin
//accessing same single user instance
val user: User = Mockup.get()
val user2: User? = Mockup.getOrNull()
//Getting list of items
val usersList: List<User> = Mockup.getList()

//Getting random user instance from the list
val userRandom: User = Mockup.getRandom()
```

## Custom mockup data (JSON)
You can register hand-crafted mockup data directly in code. Custom data are prepended before
generated values and are ideal for previews or debug-only usage.

### Auto-init registry (recommended)
Implement `CustomMockupProvider<T>` and the processor will generate a registry automatically.
The generated registry is invoked on every provider creation, so previews and runtime both receive
the custom values without any manual calls.
Providers must be `object`s or have a no-arg constructor.

```kotlin
object UserCustomProvider : CustomMockupProvider<User> {
    override val clazz = User::class
    override val values: List<User> = Mockup.fromJsonList(
        json = """[{"id":1,"name":"Jane"}]"""
    )
}
```

```kotlin
Mockup.add<User>(json = """{"id":1,"name":"Jane"}""")
Mockup.addList<User>(json = """[{"id":1,"name":"Jane"}]""")
```

It is also possible to use generated providers as `PreviewParameterProvider` when
`mockup.usePreviewParameterProviders=true`.

```kotlin
//For class "User" the name would be UserMockupProvider by default
@Composable
@PreviewLightDark
private fun UserScreen(
    @PreviewParameter(provider = UserMockupProvider::class)
    user: Publisher,
) {
    //Preview content
}
```

### Supported types
- Simple kotlin types like Int, Long, ...
- String which will contain "Lorem ipsum..." text
- List (kotlin.List), but only when list contains simple type or @Mockup annotated classes, other types won't work
- Arrays with known type (like IntArray, FloatArray, ...) are generated empty
- Classes annotated with @Mockup annotation
- Enum types are supported since 1.1.7 (no need to annotate them)


### Limitations 
- Not applicable to to java classes and java types
- Any other types that are not specified in [Supported types](#Supported-types) are **not** supported and code generation will fail.
- Generated data are **not** aggregated between each other! Don't try to access data related on id's or something.

### Used resources
🏞 Images for previews taken from [Pixabay](https://www.pixabay.com/).

### Debugging
Run this in terminal
```
./gradlew :example-app:build --no-daemon -Dorg.gradle.debug=true -Dkotlin.compiler.execution.strategy=in-process
```
Then run **Remote JVM Debug** configuration (attach to remote JVM) on localhost port 5005 with classpath on ksp-mockup.mockup-processor
