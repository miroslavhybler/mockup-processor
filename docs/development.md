# Development

This project uses Git submodules to manage the `mockup-core` and `mockup-annotations` dependencies. To properly clone the repository and all its submodules, use the following command:

```bash
git clone --recurse-submodules https://github.com/miroslavhybler/ksp-mockup-processor.git
```

If you have already cloned the main repository without the submodules, navigate into the project directory and run this command to initialize and download the submodule content:

```bash
git submodule update --init --recursive
```

## Local Development

For local development, it is recommended to publish the `mockup-core` and `mockup-annotations` libraries to your local Maven repository (`~/.m2`).

To publish the libraries to Maven Local, run the following Gradle commands from the root of the project:

```bash
./gradlew :mockup-core:publishToMavenLocal
./gradlew :mockup-annotations:publishToMavenLocal
```

After publishing the libraries locally, you can then build and run the `example-app` to test your changes.
