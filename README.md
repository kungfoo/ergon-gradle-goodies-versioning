# Cloning this repo

Since this repo now has a submodule, cloning should be done in one of the two following ways:

    git clone ssh://git@stash.ergon.ch:7999/goodies/ergon-gradle-goodies.git --recursive

or
    git clone path-to-repo.git
    git submodule init
    git submodule update

This will ensure that the test data for the versioning
plugin is there.

# Running the code/tests

Just do:

    ./gradlew

# Development and testing

This project now uses the versioning plugin, so versioning
is done using git tags.
If you wanna try out a version you installed locally in another
project, you can specify the version as follows:

    classpath ("ch.ergon:ergon-gradle-goodies:${newest_version_you_dare_to_use}")

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

# Known issues

## Gradle and PMD

Since Gradle 2.14.1 / PMD 5.5.1 the pmdMain task fails with this exception

		java.lang.NullPointerException
		    at net.sourceforge.pmd.lang.java.rule.logging.InvalidSlf4jMessageFormatRule.expectedArguments(InvalidSlf4jMessageFormatRule.java:117)

The bug is fixed in PMD Version 5.5.3.
Until the default PMD version of the Gradle PMD plugin is updated the workaround is to override the default version in your project with a version >= 5.5.3

		pmd {
			// Use at least version 5.5.3 to prevent java.lang.NullPointerException in pmdMain task in version 5.5.1 which is used as default in Gradle 3.5.
			toolVersion = '5.5.3'
		}


## Gradle and Findbugs

Since Gradle 3.5 / Findbugs 3.0.1 the findbugsReport might fail with this exception

		Caused by: javax.xml.transform.TransformerException: java.lang.NullPointerException
		    at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:746)

I (jannethm) could not find out what causes the bug but downgrading to Findbugs 3.0.0 solves the problem.
Until the default Findbugs version of the Gradle PMD plugin is updated to a version that fixes the bug the workaround is to override the default version in your project:

		findbugs {
			// Downgrade to version 3.0.0 to prevent java.lang.NullPointerException in findbugsReport task in version 3.0.1 which is used as default in Gradle 3.5.
			toolVersion = '3.0.0'
		}
