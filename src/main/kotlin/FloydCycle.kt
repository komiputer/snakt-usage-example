package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.postconditions
import org.jetbrains.kotlin.formver.plugin.loopInvariants

// A heterogeneous-aliasing linked list for Floyd's tortoise-and-hare.
// `next` is intentionally NOT @Unique: Floyd's algorithm requires two
// pointers to traverse the SAME chain, and a cycle is, by construction,
// a violation of unique ownership.
class Cell(var data: Int, var next: Cell?)

// Walk a chain to its end (or until `bound` steps), counting nodes seen.
// Pure traversal probe — no aliasing tricks. Establishes that SnaKt can
// reason about a single-pointer walk on a non-unique list without help.
@AlwaysVerify
fun walkLength(head: Cell?, bound: Int): Int {
    preconditions {
        bound >= 0
    }
    postconditions<Int> { res ->
        0 <= res
        res <= bound
    }
    var cur = head
    var n = 0
    while (cur != null && n < bound) {
        loopInvariants {
            0 <= n
            n <= bound
        }
        cur = cur.next
        n += 1
    }
    return n
}

// Floyd's tortoise-and-hare cycle detection.
//
// Functional correctness ("returns true iff a cycle is reachable from
// head") would require heap-reachability reasoning that SnaKt does not
// model. The conditional postcondition below is the strongest spec we
// could discharge: empty list ⇒ no cycle. The early `if (head == null)`
// is load-bearing — without it, SnaKt loses the path through the var
// initializers and can't propagate `head == null ⇒ fast == null` into
// the post-loop return.
@AlwaysVerify
fun hasCycle(head: Cell?): Boolean {
    postconditions<Boolean> { res ->
        // The strongest input-conditioned guarantee we can offer without
        // heap-reachability reasoning: an empty list has no cycle.
        head != null || res == false
    }
    if (head == null) return false
    var slow: Cell? = head
    var fast: Cell? = head
    while (fast != null) {
        val fNext = fast.next
        if (fNext == null) return false
        val fNextNext = fNext.next
        val sNext = slow?.next
        slow = sNext
        fast = fNextNext
        // Reference equality; SnaKt rejects `===` ("IDENTITY not yet
        // implemented") so we use `==`. Cell has no `equals` override,
        // so `==` falls back to Any.equals which is reference equality.
        if (slow != null && slow == fast) return true
    }
    return false
}

// A length-bounded variant: takes a `fuel` budget so termination is
// trivially guaranteed regardless of whether the input list has a cycle.
// Returns true iff a cycle is found within `fuel` half-steps.
@AlwaysVerify
fun hasCycleBounded(head: Cell?, fuel: Int): Boolean {
    preconditions {
        fuel >= 0
    }
    var slow: Cell? = head
    var fast: Cell? = head
    var steps = 0
    while (fast != null && steps < fuel) {
        loopInvariants {
            0 <= steps
            steps <= fuel
        }
        val fNext = fast.next
        if (fNext == null) return false
        val fNextNext = fNext.next
        val sNext = slow?.next
        slow = sNext
        fast = fNextNext
        steps += 1
        if (slow != null && slow == fast) return true
    }
    return false
}

// Probe: an empty list has no cycle. Verifies via the conditional
// postcondition on `hasCycle` — without that postcondition the
// verifier sees the call's result as opaque and rejects this.
@AlwaysVerify
fun emptyHasNoCycle(): Boolean {
    postconditions<Boolean> { res -> res == false }
    return hasCycle(null)
}
