package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.verify

class Holder(val value: Int)

@AlwaysVerify
fun probeNullableParam(x: Int?): Int {
    if (x == null) return 0
    return x
}

@AlwaysVerify
fun probeSmartCastFromNullCheck(h: Holder?): Int {
    if (h == null) return -1
    return h.value
}

@AlwaysVerify
fun probeSafeCall(h: Holder?): Int? = h?.value

@AlwaysVerify
fun probeElvis(x: Int?): Int {
    val r = x ?: 0
    verify(r >= 0 || x != null)
    return r
}

@NeverConvert   // Not yet implemented for FirCheckNotNullCallImpl (x!!)
@AlwaysVerify
fun probeNotNullAssertion(x: Int?): Int {
    return x!!
}

@AlwaysVerify
fun probeNullLiteral(): Holder? = null

@AlwaysVerify
fun probeSafeCallChainWithElvis(h: Holder?): Int = h?.value ?: 0
