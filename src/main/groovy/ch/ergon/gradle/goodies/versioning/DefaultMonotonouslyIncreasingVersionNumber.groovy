/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Takes the numbers and the numbers of commits and compounds them into a monotonously increasing long.<br/>
 * It takes the major, minor, fix and other versions plus the number of commits padded to 3 places with zeroes
 * (works up to 999) and formats them into a long.
 */
class DefaultMonotonouslyIncreasingVersionNumber implements MonotonouslyIncreasingVersionNumber {
    Project project

    long calculate() {
        return calculate(project.ergon.versioning.describeLongVersion())
    }

    @Override
    long calculate(String version) {
        int padding = project.ergon.versioning.versionNumberPadding

        try {
            project.logger.info("Figuring out monotonously increasing version number for version $version")

            def (String numbers, String commits, String hash) = version.split("-")
            project.logger.info("Version splits as numbers=$numbers commits=$commits and hash=$hash")
            def components = splitNumbers(numbers) + [Integer.valueOf(commits)]

            return Long.valueOf(components.collect { format(it, padding) }.join())

        } catch (Exception e) {
            project.logger.info("Could not determine monotonously increasing version number! Using 1L")
            return 1L
        }
    }

    private static String format(int number, int padding) {
        String.format("%0${padding}d", number)
    }

    private static List<Integer> splitNumbers(String numbers) {
        numbers.split(/\./).collect {
            try {
                return Integer.valueOf(it)
            } catch (NumberFormatException e) {
                throw new GradleException("Seems your build version numbers '$numbers' are not integer numbers separated with dots.")
            }
        }
    }
}
