package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.verify

@AlwaysVerify
fun probeDefaultArgument(x: Int = 5): Int = x + 1

@AlwaysVerify
fun probeCallWithDefaultOmitted(): Int = probeDefaultArgument()

@AlwaysVerify
fun probeCallWithNamedArgument(): Int = probeDefaultArgument(x = 7)

@AlwaysVerify
fun probeVararg(vararg xs: Int): Int = xs.size

@NeverConvert   // Not yet implemented for FirSimpleFunctionImpl -- local function declaration
@AlwaysVerify
fun probeLocalFunction(n: Int): Int {
    fun double(k: Int): Int = k * 2
    return double(n)
}

// A local function that captures an enclosing local reports "Property base not
// found in scope." @NeverConvert on the outer function does not suppress it:
// the local function is converted as its own declaration.
//
// @AlwaysVerify
// fun probeClosureOverLocal(n: Int): Int {
//     val base = n
//     fun addBase(k: Int): Int = k + base
//     return addBase(1)
// }

fun Int.doubled(): Int = this * 2

@AlwaysVerify
fun probeExtensionFunctionCall(x: Int): Int = x.doubled()

@AlwaysVerify
fun Int.probeExtensionReceiver(): Int {
    preconditions { this@probeExtensionReceiver > 0 }
    return this + 1
}

infix fun Int.plusTwo(other: Int): Int = this + other + 2

@AlwaysVerify
fun probeInfixCall(x: Int): Int = x plusTwo 3

@AlwaysVerify
tailrec fun probeTailrec(n: Int, acc: Int): Int {
    preconditions { n >= 0 }
    if (n == 0) return acc
    return probeTailrec(n - 1, acc + 1)
}

@AlwaysVerify
fun <T> probeGenericIdentity(x: T): T = x

@AlwaysVerify
fun probeGenericCall(x: Int): Int = probeGenericIdentity(x)

@AlwaysVerify
fun probeUnitReturn(x: Int) {
    verify(x == x)
}

@NeverConvert   // Not yet implemented for FirThrowExpressionImpl
@AlwaysVerify
fun probeNothingReturn(): Int = throw IllegalStateException("unreachable")
