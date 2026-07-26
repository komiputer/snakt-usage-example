package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert

// Function types and lambdas: characterised one crasher at a time, since an
// uncaught NotImplementedError aborts the whole compilation rather than
// reporting a per-declaration diagnostic.

fun applyTwice(f: (Int) -> Int, x: Int): Int = f(f(x))

@NeverConvert   // lambda literal: aborts compilation in visitLambdaExp
@AlwaysVerify
fun probeHigherOrderCall(x: Int): Int = applyTwice({ it + 1 }, x)

@NeverConvert   // lambda literal: aborts compilation in visitLambdaExp
@AlwaysVerify
fun probeLambdaValue(x: Int): Int {
    val f: (Int) -> Int = { it * 2 }
    return f(x)
}

@NeverConvert   // Not yet implemented for FirCallableReferenceAccessImpl (::probeDefaultArgument)
@AlwaysVerify
fun probeFunctionReference(x: Int): Int = applyTwice(::probeDefaultArgument, x)

inline fun runInline(f: () -> Int): Int = f()

@NeverConvert   // lambda literal: aborts compilation in visitLambdaExp
@AlwaysVerify
fun probeInlineFunctionCall(): Int = runInline { 42 }

// An anonymous object cannot be quarantined with @NeverConvert: the object
// literal is converted as its own local class regardless of the annotation on
// the enclosing function, and the type embedding for it aborts compilation.
//
// @AlwaysVerify
// fun probeAnonymousObject(): Int {
//     val g = object : Greeter {
//         override fun greet(): Int = 3
//     }
//     return g.greet()
// }

class DelegatingGreeter(g: Greeter) : Greeter by g

@AlwaysVerify
fun probeClassDelegation(g: Greeter): Int = DelegatingGreeter(g).greet()
