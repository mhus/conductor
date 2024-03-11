## Quick Start

First follow the installation instructions.

You should be able to see the conductor version.

```bash
$ con -version
Conductor Version: ...
```

If no configuration file is present conductor uses the 
default configuration from
'https://repo1.maven.org/maven2/de.mhus/conductor/conductor-plugin/${version}/conductor-plugin-${version}-configuration-default.yml'.
It define a default project under the current path '.'.

However, it is possible to use conductor in each 
project without configuration. Switch into your 
project directory and execute

```bash
con build
```

To build the project using the lifecycle 'build'. 
Conductor detects and uses the build tool of the 
project. For example if a pom.xml present it will use 
maven to build the project.

NOTE: Detection for a wide range of build tools is 
build in but support for the tools must be enhanced 
in custom configurations.

If you need to handle more than one project at once 
you need to create a configuration file 
'conductor.yaml'. Create a list of projects like this:

```yaml
projects:
- name: parent
  path: .
- name: core
  path: ../sample-core
  labels:
    dependencies:
      - parent
      - api
- name: api
  path: ../sample-product
  labels:
    dependencies: parent
```

Use the project path relative to the configuration 
file location. Best practice is to create the file 
in the root of a parent project.

This is a list of lifecycles out of the box:

* 'con git.pull' - clone and pull all projects
* 'con git ...' - git command onb each projects
* 'con build' - clean and build all projects
* 'con update' - build all project with changed files (compare and set the timestamp of the project folder)
* 'con clean' - clean the project temp files

To test a real live example you can download and build the m.h.u. suite with the following commands:

----

Clone the common root directory:

```bash
git clone https://github.com/mhus/project-root.git
```

change into the root directory of the project:

```bash
cd common-root
```

Now check out and build all other dependent projects with conductor

```bash
con -co git.pull - mvn.build
```

Try again build, it should not build again:

```bash
con mvn.build
```

Then clean and build

```bash
con -co mvn.clean - reset - mvn.build
```
