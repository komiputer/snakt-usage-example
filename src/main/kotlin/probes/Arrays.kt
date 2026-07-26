package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.loopInvariants
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.verify

// `a.size >= 0` does not hold: SnaKt models array length as an unconstrained
// integer, so nothing rules out a negative size.
@AlwaysVerify
fun probeIntArraySize(a: IntArray): Int {
    val n = a.size
    verify(n >= 0)
    return n
}

// The same function verifies once non-negativity is assumed, which pins the
// gap to the missing axiom rather than to array support in general.
@AlwaysVerify
fun probeIntArraySizeAssumedNonNegative(a: IntArray): Int {
    preconditions { a.size >= 0 }
    val n = a.size
    verify(n >= 0)
    return n
}

@AlwaysVerify
fun probeIntArrayRead(a: IntArray): Int {
    preconditions { a.size > 0 }
    return a[0]
}

@NeverConvert   // Not yet implemented for FirUnitExpression (a[0] = 5) -- indexed assignment
@AlwaysVerify
fun probeIntArrayWrite(a: IntArray) {
    preconditions { a.size > 0 }
    a[0] = 5
    verify(a[0] == 5)
}

@AlwaysVerify
fun probeIntArrayConstruction(): Int {
    val a = IntArray(3)
    return a.size
}

@NeverConvert   // Vararg arguments are currently supported for `verify` function only
@AlwaysVerify
fun probeIntArrayLiteral(): Int {
    val a = intArrayOf(1, 2, 3)
    return a.size
}

@AlwaysVerify
fun probeObjectArrayRead(a: Array<Point>): Int {
    preconditions { a.size > 0 }
    return a[0].x
}

// Needs the explicit `a.size >= 0` precondition, otherwise the invariant
// `i <= a.size` fails on entry with i == 0.
@AlwaysVerify
fun probeArrayLoop(a: IntArray): Int {
    preconditions { a.size >= 0 }
    var acc = 0
    var i = 0
    while (i < a.size) {
        loopInvariants {
            0 <= i
            i <= a.size
        }
        acc += a[i]
        i += 1
    }
    return acc
}

@AlwaysVerify
fun probeForOverArray(a: IntArray): Int {
    var acc = 0
    for (x in a) {
        loopInvariants { acc >= acc }
        acc += x
    }
    return acc
}
