# gradle goodies versioning plugin

A plugin for dead-simple versioning of gradle projects
using your git history and tags.

No more file touching for any of your builds.

Also supports android projects including generating
the `versionNumber` from your history.

Also adds a `version.properties` file to your
assembled artifact for maximum traceability.
You can use these files to easily figure out
all versions of `.jar`s that are on the classpath.

Makes for nice about screens. :)

TODO: Add sample projects using this in

- java project with multiple artifacts
- simple android project
 

# Where this came from

This source code was graciously donated to me by @ergon: https://github.com/ergon,
since I initially lost it while working there. The original history is lost, because
there were other plugins that are not universally applicable.

# Cloning this repo

Since this repo now has a submodule, cloning should be done in one of the two following ways:

    git clone https://github.com/kungfoo/ergon-gradle-goodies-versioning.git --recursive

or
    git clone path-to-repo.git
    git submodule init
    git submodule update

This will ensure that the test data for the versioning
plugin is there.

# Running the code/tests

Just do:

    ./gradlew build

# Development and testing

This project now uses itself to to versioning, so versioning
is done using git tags.

If you wanna try out a version you installed locally in another
project, you can specify the version as follows:

    classpath ("ch.ergon:ergon-gradle-goodies-versioning:${newest_version_you_dare_to_use}")

This will pick up any version you build following that
tag (sort of snapshot), even -dirty ones (where the working
directory still had uncommitted changes).

# Releasing a version

Since this project itself now uses the versioning plugin:

- Update releasenotes and readme
- Commit
- Create a tag
- git push --tags
- gradle uploadArchives releases the version described by
  that tag.
- or: use this job: http://jenkins.ergon.ch/job/ergon.gradle.goodies.release
- rinse, repeat.

