package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.implies
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.postconditions
import org.jetbrains.kotlin.formver.plugin.loopInvariants

// KMP failure function: f[i] = length of the longest proper prefix of
// s[0..i] that is also a suffix of s[0..i].
//
// Probe 1 (canonical, blocked): IntArray-backed table. The SnaKt
// converter trips on indexed *assignment*, the same way Manacher's
// p[i] = ... did. Verbatim error from `./gradlew clean compileKotlin`:
//
//   e: KmpFailure.kt:17:5 An internal error has occurred.
//   Details: Not yet implemented for
//     org.jetbrains.kotlin.fir.expressions.impl.FirUnitExpression@...
//     (f[0] = 0)
//
// The 17:5 position points at the very first `f[0] = 0` line; the rest of
// the loop body is never reached by the converter because the first
// indexed assignment is what trips it.
//
// @AlwaysVerify
// fun kmpFailureIntArray(s: String): IntArray {
//     preconditions { s.length > 0 }
//     val n = s.length
//     val f = IntArray(n)
//     f[0] = 0
//     var i = 1
//     while (i < n) {
//         var j = f[i - 1]
//         while (j > 0 && s.get(i) != s.get(j)) {
//             j = f[j - 1]
//         }
//         if (s.get(i) == s.get(j)) j += 1
//         f[i] = j
//         i += 1
//     }
//     return f
// }

// Probe 2 (storage-free reformulation): compute the failure value at a
// single index by scanning candidate lengths from longest to shortest and
// checking the prefix-vs-suffix character equality directly. This is
// O(i^2) per call (so O(n^3) for the whole table) but avoids the array
// store that blocks Probe 1.
@AlwaysVerify
fun kmpFailureAt(s: String, i: Int): Int {
    preconditions {
        0 <= i
        i < s.length
    }
    postconditions<Int> { res ->
        0 <= res
        res <= i
        forAll<Int> { j ->
            (0 <= j && j < res).implies(s.get(j) == s.get(i - res + j + 1))
        }
    }

    var k = i
    while (k > 0) {
        loopInvariants {
            0 <= k
            k <= i
            i < s.length
        }
        var j = 0
        var ok = true
        while (j < k) {
            loopInvariants {
                0 <= j
                j <= k
                k <= i
                i < s.length
                ok.implies(forAll<Int> { jj ->
                    (0 <= jj && jj < j).implies(s.get(jj) == s.get(i - k + jj + 1))
                })
            }
            if (s.get(j) != s.get(i - k + j + 1)) {
                ok = false
                break
            }
            j += 1
        }
        if (ok) return k
        k -= 1
    }
    return 0
}
