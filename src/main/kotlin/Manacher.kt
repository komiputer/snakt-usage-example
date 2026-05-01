package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.postconditions
import org.jetbrains.kotlin.formver.plugin.loopInvariants

// Expand a candidate palindrome [left..right] (inclusive) outwards as long
// as both ends match. Returns the final length r - l - 1 where (l, r) are
// the positions just outside the maximal palindrome.
@AlwaysVerify
fun expandAroundCenter(s: String, left: Int, right: Int): Int {
    preconditions {
        0 <= left
        left <= right
        right < s.length
    }
    postconditions<Int> { res ->
        0 <= res
        res <= s.length
    }

    var l = left
    var r = right
    while (l >= 0 && r < s.length) {
        loopInvariants {
            -1 <= l
            l <= left
            r >= right
            r <= s.length
            (left - l) == (r - right)
        }
        if (s.get(l) != s.get(r)) break
        l -= 1
        r += 1
    }
    return r - l - 1
}

@AlwaysVerify
fun longestPalindromeBruteForce(s: String): Int {
    postconditions<Int> { res ->
        0 <= res
        res <= s.length
    }

    if (s.length == 0) return 0

    var best = 1
    var i = 0
    while (i < s.length) {
        loopInvariants {
            0 <= i
            i <= s.length
            1 <= best
            best <= s.length
        }
        // Odd-length palindrome centered at i.
        val odd = expandAroundCenter(s, i, i)
        if (odd > best) best = odd
        // Even-length palindrome centered between i and i+1.
        if (i + 1 < s.length) {
            val even = expandAroundCenter(s, i, i + 1)
            if (even > best) best = even
        }
        i += 1
    }
    return best
}

// Manacher's proper version omitted: it needs an array of palindrome
// radii (p[i]) for the mirror trick that gives the linear-time bound.
// SnaKt has no embedding for IntArray / Array<T> / List<T>, so the array
// ops trip an "internal error: not yet implemented" inside the converter
// (specifically on indexed assignment like p[i] = ...).
//
// Without the radii array, the cleanest fallback is the O(n^2)
// expand-around-center version above, which is what callers should use
// until SnaKt grows array support.
