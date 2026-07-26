package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.verify

@AlwaysVerify
fun probeIntArithmetic(a: Int, b: Int): Int {
    preconditions {
        a > 0
        b > 0
    }
    val s = a + b
    verify(s > a)
    return s
}

@AlwaysVerify
fun probeIntDivMod(a: Int): Int {
    preconditions { a > 0 }
    val q = a / 2
    val r = a % 2
    verify(r == 0 || r == 1)
    return q
}

@NeverConvert   // Constant Expression of type Long is not yet implemented
@AlwaysVerify
fun probeLongArithmetic(a: Long, b: Long): Long {
    preconditions { a > 0L }
    return a + b
}

@AlwaysVerify
fun probeDoubleArithmetic(a: Double, b: Double): Double {
    return a + b
}

@NeverConvert   // Constant Expression of type Float is not yet implemented
@AlwaysVerify
fun probeFloatArithmetic(a: Float): Float = a + 1.0f

@AlwaysVerify
fun probeCharComparison(c: Char): Boolean = c == 'a'

@AlwaysVerify
fun probeCharOrdering(c: Char): Boolean = c >= 'a' && c <= 'z'

@AlwaysVerify
fun probeByteAndShort(b: Byte, s: Short): Int = b.toInt() + s.toInt()

@AlwaysVerify
fun probeIntToLongConversion(a: Int): Long = a.toLong()

@AlwaysVerify
fun probeBitwiseAnd(a: Int, b: Int): Int = a and b

@AlwaysVerify
fun probeBitShift(a: Int): Int = a shl 1

@AlwaysVerify
fun probeBooleanOps(a: Boolean, b: Boolean): Boolean {
    val r = (a && b) || (!a && !b)
    verify(r == (a == b))
    return r
}

@AlwaysVerify
fun probeStringLength(s: String): Int {
    val n = s.length
    verify(n >= 0)
    return n
}

@AlwaysVerify
fun probeStringIndexing(s: String): Char {
    preconditions { s.length > 0 }
    return s.get(0)
}

@AlwaysVerify
fun probeStringConcat(a: String, b: String): String = a + b

@NeverConvert   // FirPropertyAccessExpressionImpl is not supported as an element of string concatenation
@AlwaysVerify
fun probeStringInterpolation(x: Int): String = "value is $x"

@AlwaysVerify
fun probeStringEquality(a: String, b: String): Boolean = a == b

@AlwaysVerify
fun probeStringLiteral(): String = "hello"
