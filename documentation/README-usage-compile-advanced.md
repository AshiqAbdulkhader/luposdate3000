## Usage / Compile (advanced)

First, move to the project directory.

Than you need to setup the buildfiles.

```bash
./launcher.main.kts --setupCommandline
```

If you pull from the git, than execute this setup-script again.
If you are adding new dependencies to a module, you need to execute this script again too.

Here you can specify many options like ("target, inlineMode, releaseMode, compiler, ...").
To find out the possible options run

```bash
./launcher.main.kts --help.
```

afterwards you can compile it using gradle.

```bash
./gradlew build
```

