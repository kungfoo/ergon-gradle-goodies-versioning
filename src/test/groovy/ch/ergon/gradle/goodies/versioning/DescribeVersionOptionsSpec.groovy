/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DescribeVersionOptionsSpec extends Specification {

    def project = ProjectBuilder.builder().build()

    String[] describe(opts = [:]) {
        def versioningExtension = new VersioningExtension(project)
        opts.each { k, v ->
            versioningExtension[k] = v
        }
        return versioningExtension.describeCommand()
    }

    def "options can match annotated tags only "() {
        expect:
        !describe(annotatedTagsOnly: true)
                .contains("--tags")
    }

    def "options can match all tags"() {
        expect:
        describe().contains("--tags")
    }

    def "options can toggle first parent only"() {
        expect:
        describe().contains("--first-parent")
    }

    def "options can toggle first paren only"() {
        expect:
        !describe(firstParentOnly: false).contains("--first-parent")
    }

    def "options can contains a match argument"() {
        expect:
        describe(match: "asdf").contains("--match=asdf")
    }

    def "options can take closure to deal with the described version"() {
        expect:
        describe(replaceGlobWith: { glob, tag -> return "asdf is king" })
    }
}
