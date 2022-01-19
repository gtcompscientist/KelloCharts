package co.csadev.kellocharts.util

object FloatUtils {
    val POW10 = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000)

    /**
     * Returns next bigger float value considering precision of the argument.
     */
    fun nextUpF(f: Float): Float {
        var f = f
        if (java.lang.Float.isNaN(f) || f == java.lang.Float.POSITIVE_INFINITY) {
            return f
        } else {
            f += 0.0f
            return java.lang.Float.intBitsToFloat(java.lang.Float.floatToRawIntBits(f) + if (f >= 0.0f) +1 else -1)
        }
    }

    /**
     * Returns next smaller float value considering precision of the argument.
     */
    fun nextDownF(f: Float): Float {
        return if (java.lang.Float.isNaN(f) || f == java.lang.Float.NEGATIVE_INFINITY) {
            f
        } else {
            if (f == 0.0f) {
                -java.lang.Float.MIN_VALUE
            } else {
                java.lang.Float.intBitsToFloat(java.lang.Float.floatToRawIntBits(f) + if (f > 0.0f) -1 else +1)
            }
        }
    }

    /**
     * Returns next bigger double value considering precision of the argument.
     */
    fun nextUp(d: Double): Double {
        var d = d
        if (java.lang.Double.isNaN(d) || d == java.lang.Double.POSITIVE_INFINITY) {
            return d
        } else {
            d += 0.0
            return java.lang.Double.longBitsToDouble(java.lang.Double.doubleToRawLongBits(d) + if (d >= 0.0) +1 else -1)
        }
    }

    /**
     * Returns next smaller float value considering precision of the argument.
     */
    fun nextDown(d: Double): Double {
        return if (java.lang.Double.isNaN(d) || d == java.lang.Double.NEGATIVE_INFINITY) {
            d
        } else {
            if (d == 0.0) {
                (-java.lang.Float.MIN_VALUE).toDouble()
            } else {
                java.lang.Double.longBitsToDouble(java.lang.Double.doubleToRawLongBits(d) + if (d > 0.0f) -1 else +1)
            }
        }
    }

    /**
     * Method checks if two float numbers are similar.
     */
    fun almostEqual(a: Float, b: Float, absoluteDiff: Float, relativeDiff: Float): Boolean {
        var a = a
        var b = b
        val diff = Math.abs(a - b)
        if (diff <= absoluteDiff) {
            return true
        }

        a = Math.abs(a)
        b = Math.abs(b)
        val largest = if (a > b) a else b

        return diff <= largest * relativeDiff
    }

    /**
     * Rounds the given number to the given number of significant digits. Based on an answer on [Stack Overflow](http://stackoverflow.com/questions/202302).
     */
    fun roundToOneSignificantFigure(num: Double): Float {
        val d = Math.ceil(Math.log10(if (num < 0) -num else num).toFloat().toDouble()).toFloat()
        val power = 1 - d.toInt()
        val magnitude = Math.pow(10.0, power.toDouble()).toFloat()
        val shifted = Math.round(num * magnitude)
        return shifted / magnitude
    }

    /**
     * Formats a float value to the given number of decimals. Returns the length of the string. The string begins at
     * [endIndex] - [return value] and ends at [endIndex]. It's up to you to check indexes correctness.
     * Parameter [endIndex] can be helpful when you want to append some text to formatted value.
     *
     * @return number of characters of formatted value
     */
    fun formatFloat(
        formattedValue: CharArray,
        value: Float,
        endIndex: Int,
        digits: Int,
        separator: Char
    ): Int {
        var value = value
        var digits = digits
        if (digits >= POW10.size) {
            formattedValue[endIndex - 1] = '.'
            return 1
        }
        var negative = false
        if (value == 0f) {
            formattedValue[endIndex - 1] = '0'
            return 1
        }
        if (value < 0) {
            negative = true
            value = -value
        }
        if (digits > POW10.size) {
            digits = POW10.size - 1
        }
        value *= POW10[digits].toFloat()
        var lval = Math.round(value).toLong()
        var index = endIndex - 1
        var charsNumber = 0
        while (lval != 0L || charsNumber < digits + 1) {
            val digit = (lval % 10).toInt()
            lval /= 10
            formattedValue[index--] = (digit + '0'.toInt()).toChar()
            charsNumber++
            if (charsNumber == digits) {
                formattedValue[index--] = separator
                charsNumber++
            }
        }
        if (formattedValue[index + 1] == separator) {
            formattedValue[index--] = '0'
            charsNumber++
        }
        if (negative) {
            formattedValue[index--] = '-'
            charsNumber++
        }
        return charsNumber
    }

    /**
     * Computes the set of axis labels to show given start and stop boundaries and an ideal number of stops between
     * these boundaries.
     *
     * @param start     The minimum extreme (e.g. the left edge) for the axis.
     * @param stop      The maximum extreme (e.g. the right edge) for the axis.
     * @param steps     The ideal number of stops to create. This should be based on available screen space; the more
     * space
     * there is, the more stops should be shown.
     * @param outValues The destination [AxisAutoValues] object to populate.
     */
    fun computeAutoGeneratedAxisValues(
        start: Float,
        stop: Float,
        steps: Int,
        outValues: AxisAutoValues
    ) {
        val range = (stop - start).toDouble()
        if (steps == 0 || range <= 0) {
            outValues.values = floatArrayOf()
            outValues.valuesNumber = 0
            return
        }

        val rawInterval = range / steps
        var interval = roundToOneSignificantFigure(rawInterval).toDouble()
        val intervalMagnitude = Math.pow(10.0, Math.log10(interval).toInt().toDouble())
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            interval = Math.floor(10 * intervalMagnitude)
        }

        val first = Math.ceil(start / interval) * interval
        val last = nextUp(Math.floor(stop / interval) * interval)

        var intervalValue: Double
        var valueIndex: Int
        var valuesNum = 0
        intervalValue = first
        while (intervalValue <= last) {
            ++valuesNum
            intervalValue += interval
        }

        outValues.valuesNumber = valuesNum

        if (outValues.values.size < valuesNum) {
            // Ensure stops contains at least numStops elements.
            outValues.values = FloatArray(valuesNum)
        }

        intervalValue = first
        valueIndex = 0
        while (valueIndex < valuesNum) {
            outValues.values[valueIndex] = intervalValue.toFloat()
            intervalValue += interval
            ++valueIndex
        }

        if (interval < 1) {
            outValues.decimals = Math.ceil(-Math.log10(interval)).toInt()
        } else {
            outValues.decimals = 0
        }
    }
}
