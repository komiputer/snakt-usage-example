package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.preconditions
import org.jetbrains.kotlin.formver.plugin.postconditions
import org.jetbrains.kotlin.formver.plugin.verify
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
@AlwaysVerify
fun testContract(): Boolean {
    contract {
        returns(true)
    }
    println("Goodbye World!")
    return false
}

@AlwaysVerify
fun verifyFalse() {
    verify(false)
}

@AlwaysVerify
fun alwaysFails(): Int {
    postconditions<Int> { result -> result > 1000 }
    return 0
}

@AlwaysVerify
fun needsPositive(x: Int): Int {
    preconditions { x > 0 }
    postconditions<Int> { it > 1 }
    return x + 1
}

@AlwaysVerify
fun callsBadly(): Int = needsPositive(-5)

@NeverConvert
fun main() {
    testContract()
}