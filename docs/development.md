# Development

This project uses Git submodules to manage the `mockup-core` and `mockup-annotations` dependencies. To properly clone the repository and all its submodules, use the following command:

```bash
git clone --recurse-submodules https://github.com/miroslavhybler/ksp-mockup-processor.git
```

If you have already cloned the main repository without the submodules, navigate into the project directory and run this command to initialize and download the submodule content:

```bash
git submodule update --init --recursive
```
