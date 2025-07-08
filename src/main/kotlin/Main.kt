package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
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

@NeverConvert
fun main() {
    testContract()
}