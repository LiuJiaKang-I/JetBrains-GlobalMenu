# globalmenu for IDEA
This plugin adds back the global menu (and support for XDG Decoration) to IntelliJ IDEA on Linux systems.

You can download the plugin from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/25530-global-menu-and-decoration)

## Contributing
If you want to contribute, feel free to fork the [GitLab repository](https://gitlab.com/JFronny/globalmenu) and submit a merge request there.
I'm happy to accept any help, be it bug fixes, new features, or just code cleanup.
Please note that I will merge MRs locally, so don't be surprised if your MR is marked as closed without merging.
Also note that globalmenu uses Gradle to build but does not use the gradle wrapper and requires a native toolchain, so make sure both are installed.
Also take a look at the references below for some resources for developing this plugin.

## References
- [the removal commit](https://github.com/JetBrains/intellij-community/commit/336265215c1ae9bf9fd7f9c23ebfabc8fc810743)
- [a swing menu library](https://github.com/Vitaliy-Yakovchuk/dbusmenu-swing)

## Publishing
Note: This section is only relevant for the maintainer of this repository and provided mostly for reference.

- To publish a new version, first create a new tag for that version and push it to the repository.
- To do so, use the `bumpVersion` task provided by `jf.autoversion` ([source code](https://git.frohnmeyer-wds.de/Johannes/Scripts)) by running `gradle bumpVersion -PnextVersionType=release`.
- Then push the tag to the repository using `git push --tags`.
- After that, run `gradle buildPlugin -Prelease` to build the plugin. You can find the output of that task as a zip file in the `build/distributions` directory.
- Finally, upload the zip file to the JetBrains Marketplace.