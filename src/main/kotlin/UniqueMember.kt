package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.Unique
import org.jetbrains.kotlin.formver.plugin.verify

data class X(var v: Int)
class Pair(@property:Unique val x: X, @property:Unique val y: X)

@AlwaysVerify
fun readMemberUnique(@Unique x: X) {
    val k = x.v
    verify(k == x.v)
}

@AlwaysVerify
fun readMemberShared(x: X) {
    val k = x.v
    verify(k == x.v)            // expected: still fails (shared, possible aliasing)
}

@AlwaysVerify
fun readMemberConstructed() {
    @Unique val x = X(42)
    val k = x.v
    verify(k == x.v)            // expected: passes
    verify(k == 42)             // open question: requires constructor postcondition
}

@AlwaysVerify
fun writeThenRead(@Unique x: X) {
    x.v = 7
    val k = x.v
    verify(k == 7)              // open question: write/read on unique var
}

@AlwaysVerify
fun nestedUnique(@Unique p: Pair) {
    val k = p.x.v
    verify(k == p.x.v)          // out-of-scope per Amadeo (nested receivers)
}

@AlwaysVerify
fun writeTwiceReadOnce(@Unique x: X) {
    x.v = 1
    x.v = 9
    val k = x.v
    verify(k == 9)
}

@AlwaysVerify
fun readWriteRead(@Unique x: X) {
    x.v = 3
    val k1 = x.v
    x.v = 11
    val k2 = x.v
    verify(k1 == 3)
    verify(k2 == 11)
}

@AlwaysVerify
fun writeOneReadAnother(@Unique x: X, @Unique y: X) {
    x.v = 5
    y.v = 8
    verify(x.v == 5)
    verify(y.v == 8)
}

@AlwaysVerify
fun stalePreservedAcrossUnrelatedWrite(@Unique x: X, @Unique y: X) {
    x.v = 4
    val k = x.v
    y.v = 99
    verify(k == 4)
    verify(x.v == 4)
}

