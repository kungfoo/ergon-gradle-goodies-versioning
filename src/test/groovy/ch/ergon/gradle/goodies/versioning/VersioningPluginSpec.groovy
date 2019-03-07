/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import spock.lang.Ignore
import spock.lang.Specification

import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.*

/**
 * Tests the effects of applying the 'egg-versioning' plugin.
 */
class VersioningPluginSpec extends Specification {

    VersioningPluginTestProject project

    void setup() {
        project = new VersioningPluginTestProject()
    }

    void checkout(sha) {
        project.checkout(sha)
    }

    String version(options = [:]) {
        project.version(options)
    }

    boolean exactMatch() {
        project.exactMatch()
    }

    def "version exactly on tag is described as 1.0.2"() {
        when:
        checkout ON_TAG

        then:
        version() == "1.0.2"
    }

    def "version after tag is described properly"() {
        when:
        checkout NOT_ON_TAG
        then:
        version() == "1.0.0-2-g59622ed"
    }

    def "version on a branch is described with tags from branch"() {
        when:
        checkout '8cc2b58'
        then:
        version() == 'random_tag'
    }


    def "can add match clause to match database-* and replace the prefix"() {
        when:
        checkout DATABASE_TAG
        then:
        version(match: 'database-*') == "1.0.0"
    }

    def "can add match clause and leave the tag alone"() {
        when:
        checkout DATABASE_TAG
        then:
        version(match: "database-*", replaceGlobWith: GlobReplace.NO_REPLACE) == "database-1.0.0"
    }

    def "can find tag of main 'artifact' using no match"() {
        when:
        checkout '6bff2b88e5'
        then:
        version() == "snap-1.0.3"
    }

    def "project has versioning extension"() {

        expect:
        project.project.ergon.versioning != null
    }

    def "long version should be the same as non-tag version"() {
        when:
        checkout DATABASE_TAG

        then:
        version(match: 'database-*', longFormat: true) == project.longVersion()
    }

    def "exactMatch should return true when on a tag"() {
        when:
        checkout ON_TAG
        then:
        version() == "1.0.2"
        exactMatch()
    }

    def "exactMatch() should return false when NOT on a tag"() {
        when:
        checkout NOT_ON_TAG
        then:
        !exactMatch()
    }

    def "dirty projects should be described as -dirty"() {
        when:
        checkout ON_TAG
        project.createFile("./snip.txt", "Sample dirty file.")

        then:
        version().endsWith("-dirty")
    }

    @Ignore("This test is ignored until we have figured out how to do the intermediate/snapshot repository outside of ergon.")
    def "nexus repository url is intermediate repo when not on a tag"() {
        when:
        checkout NOT_ON_TAG

        project.apply plugin: 'egg-versioning'
        project.apply plugin: 'egg-publish'

        then:
        project.
                project.
                uploadArchives.
                repositories.
                mavenDeployer.
                repository.url.endsWith("/intermediates")
    }
}