package jesyspa.probes

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.verify

data class Point(val x: Int, val y: Int)

@AlwaysVerify
fun probeDataClassConstruction(): Int {
    val p = Point(1, 2)
    return p.x + p.y
}

@AlwaysVerify
fun probeDataClassCopy(p: Point): Int {
    val q = p.copy(x = 9)
    return q.x
}

@NeverConvert   // Not yet implemented for FirComponentCallImpl (a) -- destructuring declaration
@AlwaysVerify
fun probeDestructuring(p: Point): Int {
    val (a, b) = p
    return a + b
}

@AlwaysVerify
fun probeDataClassEquals(p: Point, q: Point): Boolean = p == q

// Enum classes report "Properties dispatch receiver is not a regular class".
// @NeverConvert does not help: it is honoured for functions but not for class
// declarations, so the enum is converted anyway and the error stands.
//
// enum class Color { RED, GREEN, BLUE }
//
// @AlwaysVerify
// fun probeEnumWhen(c: Color): Int = when (c) {
//     Color.RED -> 0
//     Color.GREEN -> 1
//     Color.BLUE -> 2
// }
//
// @AlwaysVerify
// fun probeEnumEquality(c: Color): Boolean = c == Color.RED

@NeverConvert   // referenced only via unsupported qualifier resolution
object Singleton {
    val constant: Int = 7
}

@NeverConvert   // Unsupported resolved qualifier FirRegularClassSymbol -- object reference
@AlwaysVerify
fun probeObjectAccess(): Int = Singleton.constant

@NeverConvert   // companion object members cannot be resolved as qualifiers
class WithCompanion {
    companion object {
        val answer: Int = 42
        fun make(): WithCompanion = WithCompanion()
    }
}

@NeverConvert   // Unsupported resolved qualifier FirRegularClassSymbol -- companion reference
@AlwaysVerify
fun probeCompanionAccess(): Int = WithCompanion.answer

@NeverConvert   // Unsupported resolved qualifier FirRegularClassSymbol -- companion reference
@AlwaysVerify
fun probeCompanionFactory(): WithCompanion = WithCompanion.make()

open class Base(val n: Int) {
    open fun describe(): Int = n
}

class Derived(n: Int) : Base(n) {
    override fun describe(): Int = n * 2
}

@AlwaysVerify
fun probeVirtualDispatch(b: Base): Int = b.describe()

@AlwaysVerify
fun probeSubclassConstruction(): Int = Derived(3).describe()

interface Greeter {
    fun greet(): Int
    fun greetTwice(): Int = greet() + greet()
}

class SimpleGreeter : Greeter {
    override fun greet(): Int = 1
}

@AlwaysVerify
fun probeInterfaceDefaultMethod(g: Greeter): Int = g.greetTwice()

abstract class AbstractShape {
    abstract fun area(): Int
}

class Square(val side: Int) : AbstractShape() {
    override fun area(): Int = side * side
}

@AlwaysVerify
fun probeAbstractClass(s: AbstractShape): Int = s.area()

class CustomAccessors(private var backing: Int) {
    var doubled: Int
        get() = backing * 2
        set(v) {
            backing = v / 2
        }
}

@AlwaysVerify
fun probeCustomGetter(c: CustomAccessors): Int = c.doubled

@AlwaysVerify
fun probeCustomSetter(c: CustomAccessors) {
    c.doubled = 10
}

class WithLateinit {
    lateinit var name: String
}

@AlwaysVerify
fun probeLateinit(w: WithLateinit): Int = w.name.length

class WithLazy {
    val computed: Int by lazy { 5 }
}

@AlwaysVerify
fun probeLazyDelegate(w: WithLazy): Int = w.computed

class Outer(val v: Int) {
    class Nested(val w: Int)

    inner class Inner {
        fun get(): Int = v
    }
}

@AlwaysVerify
fun probeNestedClass(): Int = Outer.Nested(1).w

// Inner classes convert, but the outer receiver is not typed: the generated
// precondition `isSubtype(typeOf(this), Outer())` fails.
@AlwaysVerify
fun probeInnerClass(o: Outer): Int = o.Inner().get()

@JvmInline
value class Meters(val v: Int)

@AlwaysVerify
fun probeValueClass(m: Meters): Int = m.v

typealias Count = Int

@AlwaysVerify
fun probeTypeAlias(c: Count): Count = c + 1

class Counter(var n: Int) {
    fun bump() {
        n += 1
    }
}

// Constructors and methods carry no inferred postconditions, so nothing is
// known about `c.n` after `Counter(0)` and `c.bump()`. Expected: SnaKt requires
// explicit contracts rather than inferring them.
@AlwaysVerify
fun probeMutableFieldWrite(): Int {
    val c = Counter(0)
    c.bump()
    verify(c.n == 1)
    return c.n
}

class Generic<T>(val item: T)

@AlwaysVerify
fun probeGenericClass(g: Generic<Int>): Int = g.item

@AlwaysVerify
fun probeIsCheckOnClass(a: Any): Boolean = a is Point

@AlwaysVerify
fun probeAsCast(a: Any): Int = (a as Point).x
