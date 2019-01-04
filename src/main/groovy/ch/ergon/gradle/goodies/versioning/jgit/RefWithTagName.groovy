package ch.ergon.gradle.goodies.versioning.jgit

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.eclipse.jgit.lib.Ref

@ToString(includeFields = true)
@EqualsAndHashCode
@TupleConstructor
class RefWithTagName {
    public Ref ref
    public String tag
}