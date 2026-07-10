package jesyspa

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify

sealed interface Expr
class Lit(val value: Int) : Expr
class Add(val left: Expr, val right: Expr) : Expr
class Mul(val left: Expr, val right: Expr) : Expr

@AlwaysVerify
fun eval(e: Expr): Int = when (e) {   // exhaustive, no `else`
    is Lit -> e.value
    is Add -> eval(e.left) + eval(e.right)
    is Mul -> eval(e.left) * eval(e.right)
}

@AlwaysVerify
fun isLit(e: Expr): Boolean {
    if (e !is Lit) return false       // narrowing by exclusion
    return true
}
