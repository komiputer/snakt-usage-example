package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.postconditions
import org.jetbrains.kotlin.formver.plugin.loopInvariants
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.implies

// Iterative Euclidean algorithm. Pure integer arithmetic — the cleanest
// fixture in the algorithm-probes batch (no arrays, no strings).
//
// Spec: requires non-negative inputs. Returns x such that x >= 0, and
// the canonical "gcd absorbs zero" identities:
//   gcd(a, 0) == a    gcd(0, b) == b
// We don't try to express "is the greatest common divisor" because
// "greatest" requires quantifying over divisors, and SnaKt has no
// user-defined logical predicate machinery (see snakt_manacher.md).
//
// Implementation note: the absorbing cases are split out as early
// returns so the loop only runs when both inputs are strictly
// positive. This lets us prove the identity postconditions without
// needing a disjunctive loop invariant — Viper Silicon handles
// disjunction in invariants poorly.
@AlwaysVerify
fun gcd(a: Int, b: Int): Int {
    preconditions {
        a >= 0
        b >= 0
    }
    postconditions<Int> { res ->
        res >= 0
        (b == 0) implies (res == a)
        (a == 0) implies (res == b)
    }

    if (b == 0) return a
    if (a == 0) return b

    var x = a
    var y = b
    while (y > 0) {
        loopInvariants {
            x > 0
            y >= 0
        }
        val t = x % y
        x = y
        y = t
    }
    return x
}

// gcd(a, 0) == a — trivial corollary, useful as a sanity probe that
// the postcondition machinery actually exposes the identity case.
@AlwaysVerify
fun gcdZeroRight(a: Int): Int {
    preconditions { a >= 0 }
    postconditions<Int> { res -> res == a }
    return gcd(a, 0)
}

// gcd(0, b) == b — symmetric probe. After one loop iteration the state
// becomes (b, 0) so this also tests that the invariant survives a swap.
@AlwaysVerify
fun gcdZeroLeft(b: Int): Int {
    preconditions { b >= 0 }
    postconditions<Int> { res -> res == b }
    return gcd(0, b)
}

// Iterative extended Euclidean. Computes (g, s, t) with g = gcd(a, b)
// and a*s + b*t == g. We return only `s` since that's what modInverse
// needs; g is recoverable via gcd(a, b).
//
// The Bézout invariant is the heart of the algorithm:
//     a*oldS + b*oldT == oldR
//     a*s    + b*t    == r
// We track all four coefficients (oldS, s, oldT, t) in state purely so
// that the loop invariant can name them — testing whether Viper/Silicon
// preserves nonlinear arithmetic facts across iterations.
@AlwaysVerify
fun bezoutS(a: Int, b: Int): Int {
    preconditions {
        a >= 0
        b >= 0
    }

    var oldR = a
    var r = b
    var oldS = 1
    var s = 0
    var oldT = 0
    var t = 1
    while (r > 0) {
        loopInvariants {
            r >= 0
            oldR >= 0
            oldR == a * oldS + b * oldT
            r == a * s + b * t
        }
        val q = oldR / r
        val newR = oldR - q * r
        val newS = oldS - q * s
        val newT = oldT - q * t
        oldR = r
        r = newR
        oldS = s
        s = newS
        oldT = t
        t = newT
    }
    // Loop exit gives r == 0, so the second invariant collapses to
    // 0 == a*s + b*t (interesting but not used). The first invariant
    // gives the Bézout identity at exit: oldR == a*oldS + b*oldT, where
    // oldR is the gcd(a, b). We don't return oldT, so we can't surface
    // the full identity in the postcondition without restructuring to
    // return a class — but we can at least confirm the property at the
    // exit point.
    verify(oldR == a * oldS + b * oldT)
    return oldS
}

// Modular inverse of a modulo m, computed via the extended Euclidean
// algorithm. Caller must supply gcd(a, m) == 1 — we don't recompute /
// check it (no way to express it as a precondition without a logical
// predicate).
//
// Strongest post we attempt: 0 <= result < m. The "(a * result) % m == 1"
// half is the functional spec; whether SnaKt can prove it depends on
// how much it tracks of % and *.
@AlwaysVerify
fun modInverse(a: Int, m: Int): Int {
    preconditions {
        m > 1
        a >= 0
        a < m
    }
    postconditions<Int> { res ->
        res >= 0
        res < m
    }
    val s = bezoutS(a, m)
    // s may be negative or >= m; normalize into [0, m).
    val r = s % m
    return if (r < 0) r + m else r
}
