/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies

import org.gradle.api.plugins.ExtensionAware

/**
 * Common settings for Ergonized builds.
 */
class ErgonExtension {

    public static final String DEFAULT_FILE_ENCODING = 'UTF-8'
    /**
     * The value of the line break as it is written in the settings file in eclipse. This is not
     * the value to be used to append new lines to a string.
     */
    public static final DEFAULT_LINE_BREAK = '\\n'

    /**
     * The file encoding used in the project.
     */
    String fileEncoding

    String getFileEncoding() {
        return fileEncoding ?: DEFAULT_FILE_ENCODING
    }

    /**
     * The line break to use in eclipse settings.
     * If undefined a default line break is used.
     */
    String lineBreak

    String getLineBreak() {
        return this.lineBreak ?: DEFAULT_LINE_BREAK
    }

    /**
     * Creates a nested extension object of the specified type.
     * @param name the name of the nested extension object, usually the same as the plugin using the
     *        extension.
     * @param type the type of the nested extension object.
     * @param arguments the optional sequence of arguments for constructing the extension object.
     * @return the created extension object.
     */
    def <T> T create(String name, Class<T> type, Object... arguments) {
        ((ExtensionAware) this).extensions.create(name, type, arguments)
    }
}
