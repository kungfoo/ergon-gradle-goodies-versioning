/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Utility methods for the plugins. 
 */
class Eggs {

    /** The name of the auditing report task. */
    public static final String AUDITING_REPORTS_TASK_NAME = "auditingReports"

    /** The name of the documentation task group. */
    public static final String DOCUMENTATION_GROUP = "documentation"

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

    /**
     * Returns the task for auditing reports generation. If the task has not been created yet then
     * the method will create and configure the task. The meaning of the task is similar to the
     * check task from the java plugin. It groups together all tasks generating human readable
     * reports.
     *
     * @return the task for auditing reports generation.
     */
    static Task getAuditingReportTask(Project project) {
        Task task = project.tasks.findByPath(AUDITING_REPORTS_TASK_NAME)
        if (task == null) {
            task = project.getTasks().create(AUDITING_REPORTS_TASK_NAME)
            task.setDescription("Runs generation of human readable reports.")
            task.setGroup(DOCUMENTATION_GROUP)
        }
        return task
    }

}
