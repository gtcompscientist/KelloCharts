package co.csadev.kellocharts.util

/**
 * Returns null if the String is empty
 */
fun String.nullIfEmpty() = if (isEmpty()) null else this
