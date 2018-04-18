/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies

import org.gradle.api.Project

/**
 * Utility methods for the plugins. 
 */
class Eggs {

    /**
     * The name of the container for all egg extension within the project.
     */
    public static final String ERGON_EXTENSION = 'ergon'

    /**
     * Ensures that the passed project has an extensible 'eggs' extension.
     * @param project the project that requires an 'eggs' extension container.
     * @return the project's extension for storing egg-related properties and nested extensions.
     */
    static ErgonExtension getExtension(Project project) {
        project.extensions.findByType(ErgonExtension) ?: project.extensions.create(ERGON_EXTENSION, ErgonExtension)
    }

}
