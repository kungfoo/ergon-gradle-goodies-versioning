# gradle goodies versioning plugin

[![Build Status](https://travis-ci.org/kungfoo/ergon-gradle-goodies-versioning.svg?branch=master)](https://travis-ci.org/kungfoo/ergon-gradle-goodies-versioning)

A plugin for dead-simple versioning of gradle projects
using your git history and tags.

No more file touching for any of your build files or properties to set the version before a release.

Also supports android projects including generating
the `versionNumber` (a monotonously increasing number for android) from your history.

It also adds a `version.properties` file to your
assembled artifact for maximum traceability. This file is not time-stamped,
so it does not interfere with up-to-date checks of gradle.

You can use these files to easily figure out
all versions of `.jar`s that are on the classpath.

Makes for nice about screens. :)

# Usage

The simplest usage is to just apply the plugin via the `plugins` block:

```groovy
plugins {
    id "ch.ergon.gradle.goodies.versioning" version "${insert-plugin-version-here}"
}
```

This is it, that's all there is to it. 🙌

## More advanced usage

This plugin allows for configuration, should you not be happy with the default or require special
processing of the tags in your git history.

### Always use the long format

When executed directly on a tagged commit, by default the version will be that, the name of the tag. If you want
to always use the _long_ version:

```groovy
versioning {
    longFormat = true
}
```

### Filtering tags that should be used

Maybe you have more tags than are relevant strictly for versioning (of one artifact). Let's assume the
ones that are relevant are like this:

- `can-has-prefix-0.2.0.1`
- `can-has-prefix-0.4.5`
- ...

You can match only these tags using:

```groovy
versioning {
    match = 'can-has-prefix-*'
}
```

### Use only annotated tags

```groovy
versioning {
    annotatedTagsOnly = true
}
```

### Post process the `describe` string from git

```groovy
versioning {
    // returns 1.3.4-f76423 instead of 1.3.4-19-gf76423
    postProcessVersion = ch.ergon.gradle.goodies.versioning.PostProcessVersion.STRIP_NR_COMMITS_AND_G
}
```

Or bring your own code:

```groovy
versioning {
    postProcessVersion = { version -> doMagicWithVersion(version) }
}
```

### Not trimming the prefix when matching

By default, this plugin trims the prefix when using the `match` option, such that

```groovy
versioning {
    match = 'api-*'
}
```

would use the tags and then produce the following versions:
- `api-1.3.4` yields `1.3.4`
- `api-5.3.2` plus 5 commits yields `5.3.2-5-gf71872`

If you want to deal with the prefix differently, provide some code to do it:

```groovy
versioning {
    replaceGlobWith = { matchedTag -> doSomeMoreMagicWithTheMatchedTag(matchedTag) }
}
```

You can also turn off replacement of the tags by using:

```groovy
versioning {
    replaceGlobWith = ch.ergon.gradle.goodies.versioning.GlobReplace.NO_REPLACE
}
```

# TODO

Add more sample projects using this in

- java project with multiple artifacts and tags
- simple android project
 

# Where this came from

This source code was graciously donated to me by @ergon: https://github.com/ergon,
since I initially wrote it while working there. It was of great use to us, so we figured
it might also be useful for everyone else.

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

- Update release notes and readme
- Commit
- Create a tag
- git push --tags
- release to the wild, wild web.
- rinse, repeat.

