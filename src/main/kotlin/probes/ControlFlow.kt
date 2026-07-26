package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.loopInvariants
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.verify

// Baseline: a `while` loop with explicit invariants is known to work.
@AlwaysVerify
fun probeWhileLoop(n: Int): Int {
    preconditions { n >= 0 }
    var i = 0
    while (i < n) {
        loopInvariants {
            0 <= i
            i <= n
        }
        i += 1
    }
    verify(i == n)
    return i
}

@NeverConvert   // Not yet implemented for FirDoWhileLoopImpl
@AlwaysVerify
fun probeDoWhileLoop(n: Int): Int {
    preconditions { n >= 1 }
    var i = 0
    do {
        loopInvariants {
            0 <= i
            i <= n
        }
        i += 1
    } while (i < n)
    verify(i == n)
    return i
}

// `for` over a range converts, but `loopInvariants` has no effect inside one:
// even `acc >= 0` fails after the loop, while the byte-for-byte equivalent
// `while` loop in probeWhileLoop verifies a much stronger property. Nothing
// about a variable assigned in a `for` body survives the loop.
@AlwaysVerify
fun probeForOverRange(n: Int): Int {
    preconditions { n >= 0 }
    var acc = 0
    for (i in 0 until n) {
        loopInvariants { acc >= 0 }
        acc += 1
    }
    verify(acc >= 0)
    return acc
}

@AlwaysVerify
fun probeForOverDownTo(n: Int): Int {
    preconditions { n >= 0 }
    var acc = 0
    for (i in n downTo 0) {
        loopInvariants { acc >= 0 }
        acc += 1
    }
    return acc
}

@AlwaysVerify
fun probeForOverStep(n: Int): Int {
    preconditions { n >= 0 }
    var acc = 0
    for (i in 0 until n step 2) {
        loopInvariants { acc >= 0 }
        acc += 1
    }
    return acc
}

// `x in 1..10` converts, but carries no semantics: relating it to the
// equivalent conjunction of comparisons does not verify.
@AlwaysVerify
fun probeRangeMembership(x: Int): Boolean {
    val inRange = x in 1..10
    verify(inRange == (x >= 1 && x <= 10))
    return inRange
}

@AlwaysVerify
fun probeLabeledBreak(n: Int): Int {
    preconditions { n >= 0 }
    var found = -1
    outer@ for (i in 0 until n) {
        loopInvariants { found >= -1 }
        for (j in 0 until n) {
            loopInvariants { found >= -1 }
            if (i + j == 3) {
                found = i
                break@outer
            }
        }
    }
    return found
}

@AlwaysVerify
fun probeContinue(n: Int): Int {
    preconditions { n >= 0 }
    var acc = 0
    var i = 0
    while (i < n) {
        loopInvariants {
            0 <= i
            i <= n
            acc >= 0
        }
        i += 1
        if (i % 2 == 0) continue
        acc += 1
    }
    return acc
}

@AlwaysVerify
fun probeWhenWithSubject(x: Int): Int = when (x) {
    0 -> 100
    1 -> 200
    else -> 300
}

@AlwaysVerify
fun probeWhenWithRanges(x: Int): Int = when (x) {
    in 0..9 -> 1
    in 10..99 -> 2
    else -> 3
}

@AlwaysVerify
fun probeWhenWithoutSubject(x: Int): Int = when {
    x < 0 -> -1
    x == 0 -> 0
    else -> 1
}

// try/catch converts, but the result carries no type information: the implicit
// postcondition `isSubtype(typeOf(result), intType())` fails.
@AlwaysVerify
fun probeTryCatch(x: Int): Int {
    return try {
        x / 1
    } catch (e: ArithmeticException) {
        0
    }
}

@NeverConvert   // Not yet implemented for FirThrowExpressionImpl
@AlwaysVerify
fun probeThrow(x: Int): Int {
    if (x < 0) throw IllegalArgumentException("negative")
    return x
}

@AlwaysVerify
fun probeTryFinally(x: Int): Int {
    var r = 0
    try {
        r = x
    } finally {
        r += 1
    }
    return r
}
