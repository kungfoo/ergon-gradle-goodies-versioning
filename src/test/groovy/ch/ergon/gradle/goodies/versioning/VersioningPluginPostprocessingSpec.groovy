/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import spock.lang.Specification

import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.NOT_ON_TAG
import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.ON_TAG

/**
 * Tests the effects of applying the 'egg-versioning' plugin and
 * post-processing the version.
 */
class VersioningPluginPostprocessingSpec extends Specification {

    VersioningPluginTestProject project

    void setup() {
        project = new VersioningPluginTestProject()
    }

    void checkout(String sha) {
        project.checkout(sha)
    }

    String version(options = [:]) {
        project.version(options)
    }

    def "short version is left alone by default"() {
        when:
        checkout ON_TAG

        then:
        version() == "1.0.2"
    }

    def "long version is left alone by default"() {
        when:
        checkout NOT_ON_TAG

        then:
        version() == "1.0.0-2-g59622ed"
    }

    def "short version is left alone by STRIP_NR_COMMITS_AND_G"() {
        when:
        checkout ON_TAG

        then:
        version(postProcessVersion: PostProcessVersion.STRIP_NR_COMMITS_AND_G) == "1.0.2"
    }

    def "long version is properly processed by STRIP_NR_COMMITS_AND_G"() {
        when:
        checkout NOT_ON_TAG

        then:
        version(postProcessVersion: PostProcessVersion.STRIP_NR_COMMITS_AND_G) == "1.0.0-59622ed"
    }

    def "one can add a custom closure to process the version"() {
        when:
        checkout NOT_ON_TAG

        then:
        version(
                postProcessVersion: { version -> "can-haz-special-k-${version}" }
        ) == "can-haz-special-k-1.0.0-2-g59622ed"
    }
}