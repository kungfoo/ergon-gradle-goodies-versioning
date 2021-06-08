/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

/**
* Generate a monotonously increasing version number using some smart scheme.
* This is especially useful for android projects, where you need such a version number.
*/
interface MonotonouslyIncreasingVersionNumber {
	/**
	* Calculate a monotonically rising version number on your own. Have fun.
	*/
	long calculate(String version)
}
