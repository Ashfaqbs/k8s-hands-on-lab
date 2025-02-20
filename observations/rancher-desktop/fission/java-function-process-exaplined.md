# Documenting the Process: Creating and Running a Java Function with Fission on Windows

This document explains the step-by-step process to create and run a Java function using Fission on Windows. Each step is accompanied by a brief explanation of why it is necessary.

---

## 1. **Set Up the Fission Environment**
Run the following command to set up the Java environment in Fission:
```bash
fission environment create --name java --image ghcr.io/fission/jvm-env --builder ghcr.io/fission/jvm-builder --keeparchive --version 3
```

### Why?
- **Fission Environment**: The Fission environment acts as the runtime for executing the function. This step ensures that the Java runtime and build tools are available for your function.
- **Keep Archive**: Using the `--keeparchive` flag ensures that the source code remains available for debugging and troubleshooting.

---

## 2. **Prepare the Java Function Code**
### a. Create the Source Directory
```powershell
mkdir -p src/main/java/io/fission/
```

### Why?
- **Source Directory Structure**: Java uses a package structure to organize code. Creating the directory `src/main/java/io/fission` aligns with Maven's standard structure and the Java package naming convention.

### b. Download the Function Code
```powershell
curl -L https://raw.githubusercontent.com/fission/examples/main/java/hello-world/src/main/java/io/fission/HelloWorld.java -o src/main/java/io/fission/HelloWorld.java
```

### Why?
- **HelloWorld.java**: This is a sample Java function that prints "Hello World." This serves as the entry point for your function in Fission.

---

## 3. **Add `pom.xml` File**
```powershell
curl -LO https://raw.githubusercontent.com/fission/environments/master/jvm/examples/java/pom.xml
```

### Why?
- **`pom.xml`**: This is a Maven configuration file that specifies dependencies, build plugins, and other metadata. Fission uses Maven to build the Java function into a runnable format.

---

## 4. **Create a ZIP Archive of the Source Code**
### a. Compress the Files
From the `src` directory, run:
```powershell
Compress-Archive -Path .\main, .\pom.xml -DestinationPath ..\java-src-pkg.zip
```

Alternatively, if in the parent directory:
```powershell
Compress-Archive -Path .\src\main, .\src\pom.xml -DestinationPath java-src-pkg.zip
```

### Why?
- **ZIP File**: Fission requires the source code to be uploaded as a single ZIP archive. This makes it easier to manage and deploy the code.

---

## 5. **Upload the ZIP Archive to Fission**
```bash
fission package create --name hello-pkg --env java --src java-src-pkg.zip
```

### Why?
- **Fission Package**: This step uploads the ZIP file and triggers a build process in Fission to convert the source code into an executable function.

---

## 6. **Verify Package Status**
```bash
fission pkg list | grep hello-pkg
```

### Why?
- **Build Verification**: Ensuring the package status is "succeeded" confirms that the source code was built successfully.

---

## 7. **Create the Function**
```bash
fission function create --name hello-java --env java --pkg hello-pkg --entrypoint io.fission.HelloWorld
```

### Why?
- **Function Creation**: This associates the built package with a function name (`hello-java`) and specifies the entry point (`io.fission.HelloWorld`). The entry point is the fully qualified name of the Java class that contains the `main` method or equivalent logic.

---

## 8. **Test the Function**
```bash
fission function test --name hello-java
```

### Why?
- **Function Testing**: This verifies that the function is running as expected. The output "Hello World!" indicates that the setup is complete and functioning correctly.

---

## Summary
### Steps Recap:
1. Set up the Java environment in Fission.
2. Prepare the source directory and Java code.
3. Add the `pom.xml` file for dependencies.
4. Create a ZIP archive of the source code.
5. Upload the ZIP file as a package to Fission.
6. Verify that the package build succeeded.
7. Create a function using the package.
8. Test the function to ensure it works.

### Key Points:
- **Organization**: Following the Maven structure ensures compatibility with Java build tools.
- **Packaging**: The ZIP format simplifies the upload process.
- **Environment**: Using a pre-built Fission Java environment saves time and effort.

This process demonstrates how to deploy a simple "Hello World" Java function on Fission, tailored for Windows users.

