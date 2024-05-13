# Quick Start

First follow the installation instructions.

You should be able to see the conductor version.

    $ con -version
    Conductor Version: 1.2.0

If no configuration file is present conductor uses the default
configuration in `~/.conductor/` It generates a default project with the current path.

It's possible to use conductor in each project without
configuration. Switch into your project directory and execute

    con build

To build the project using the lifecycle *build*. Conductor detects and
uses the build tool of the project. For example if a pom.xml present it
will use maven to build the project.

Detection for a wide range of build tools is build in but support for
the tools must be enhanced in custom configurations.

If you need to handle more then one project at once you need to create a
configuration file *conductor.yaml*. Create a list of projects like
this:

    projects:
      - name: parent
        path: .
        labels:
          sort: 0
      - name: core
        path: ../sample-core
        labels:
          sort: 1
      - name: api
        path: ../sample-product
        labels:
          sort: 2

Use the project path relative to the configuration file location. Best
practice is to create the file in the root of a parent project.

This is a list of lifecycles out of the box:

-   *con git.pull* - clone and pull all projects

-   *con git.reset* - remove all modifications on projects

-   *con rebuild* - clean and build all projects

-   *con update* - build all project with changed files (compare and set
    the timestamp of the project folder)

-   *con clean* - clean the project temp files

To test a real live example you can download and build the m.h.u. suite
with the following commands:

    Clone only the parent project:

    $ git clone https://github.com/mhus/mhus-parent.git

    change into the root directory of the project:

    $ cd mhus-parent

    now check out and build all other dependent projects with conductor

    $ con git - build
