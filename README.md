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


dependencies {
    //Always use the same version for annotations and processor
    val mockupVersion= "1.2.0"
    implementation("com.github.miroslavhybler:ksp-mockup-annotations:$mockupVersion")
    ksp("com.github.miroslavhybler:ksp-mockup-annotations:$mockupVersion")
    ksp("com.github.miroslavhybler:kps-mockup-processor:$mockupVersion")
}
```

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

Build your project and access generated data through generated Mockup object.

```kotlin
//accessing same single user instance
val user = Mockup.user.single

//Getting list of items
val usersList = Mockup.user.list

//Getting random user instance from the list
val userRandom = Mockup.user.random
```

Since version 1.1.8 is it also possible to use as PreviewParameterProvider

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