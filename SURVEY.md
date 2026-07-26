# What SnaKt can and cannot verify

A survey of the Kotlin language surface supported by the SnaKt formal
verification plugin (`org.jetbrains.kotlin.formver`, `0.1.0-SNAPSHOT` from
`mavenLocal`), run against this project on 2026-07-26.

The probes live in `src/main/kotlin/probes/`, one file per language area. Every
declaration SnaKt cannot handle is marked `@NeverConvert` with the exact
diagnostic it produces, so the module builds green and the annotations
themselves are the inventory.

## The short version

The supported fragment is roughly *first-order imperative Kotlin over `Int`,
`Boolean`, `String`, `Char` and user-defined classes*: `while` loops, `when`,
conditionals, arithmetic, comparisons, nullable types with smart casts, data
classes, inheritance with virtual dispatch, interfaces, generics, and extension
functions. That is enough to write and verify real algorithms — the
`expandAroundCenter` palindrome routine in `src/main/kotlin/Manacher.kt`
verifies with loop invariants.

What is missing clusters into three groups: **function values** (lambdas,
callable references, local functions), **a handful of very ordinary
expressions** (`throw`, `!!`, `do/while`, destructuring, string templates
containing a variable, `Long` and `Float` literals), and **enums and object
declarations**. Two of these do not merely fail — they abort the entire
compilation.

## Two constructs abort compilation outright

Most unsupported constructs are reported as a per-declaration error and
compilation continues. Two are not, because they throw `NotImplementedError`,
which is a `kotlin.Error` and therefore slips past the `catch (e: Exception)`
in `ViperPoweredDeclarationChecker`:

| Construct | Throw site |
| --- | --- |
| Lambda literal (`{ it + 1 }`) | `LinearizationVisitor.kt:590` (`visitLambdaExp`) |
| Anonymous object (`object : Greeter { … }`) | `ProgramConverter.kt:670` (type embedding) |

The failure looks like a compiler crash — `FileAnalysisException: While
analysing …` — and it takes every remaining file with it. Neither can be worked
around with `@NeverConvert`, because both are converted as their own nested
declarations regardless of the annotation on the enclosing function. In this
project they are commented out.

A related trap: a *function type in a signature* is fine. `fun applyTwice(f:
(Int) -> Int, x: Int): Int = f(f(x))` converts without complaint. It is
materialising a lambda *value* that dies.

## Unsupported, reported per declaration

Each of these produces `An internal error has occurred.` with the quoted
detail, and is quarantined behind `@NeverConvert` in the probes.

| Kotlin feature | Diagnostic |
| --- | --- |
| `throw` expressions | `Not yet implemented for FirThrowExpressionImpl` |
| `do { … } while (…)` | `Not yet implemented for FirDoWhileLoopImpl` |
| `!!` not-null assertion | `Not yet implemented for FirCheckNotNullCallImpl` |
| Destructuring (`val (a, b) = p`) | `Not yet implemented for FirComponentCallImpl` |
| Local function declarations | `Not yet implemented for FirSimpleFunctionImpl` |
| Capturing an enclosing local | `Property base not found in scope.` |
| Callable references (`::f`) | `Not yet implemented for FirCallableReferenceAccessImpl` |
| Array indexed assignment (`a[0] = 5`) | `Not yet implemented for FirUnitExpression` |
| Vararg arguments at a call site | ``Vararg arguments are currently supported for `verify` function only`` |
| `Long` literals | `Constant Expression of type Long is not yet implemented.` |
| `Float` literals | `Constant Expression of type Float is not yet implemented.` |
| String template with a variable (`"x is $x"`) | `FirPropertyAccessExpressionImpl is not supported as an element of string concatenation` |
| `enum class` | `Properties dispatch receiver is not a regular class` |
| `object` / companion references | `Unsupported resolved qualifier FirRegularClassSymbol` |

Vararg *parameters* are fine — `fun f(vararg xs: Int) = xs.size` converts. It is
passing varargs at a call site, including `intArrayOf(1, 2, 3)`, that fails.

## Converts, but does not verify

These convert without an error, so they look supported, and then quietly fail
to prove anything. This is the more dangerous category, because the build is
green and the failure is a warning.

**`IntArray.size` is not known to be non-negative.** `verify(a.size >= 0)`
fails. This blocks essentially every array loop: the invariant `i <= a.size`
does not hold on entry with `i == 0`. Adding `preconditions { a.size >= 0 }` by
hand makes both go through, which pins the gap to a missing axiom rather than
to array support in general.

**`loopInvariants` has no effect inside a `for` loop.** After
`for (i in 0 until n) { loopInvariants { acc >= 0 }; acc += 1 }`, even
`acc >= 0` fails. The byte-for-byte equivalent `while` loop verifies the far
stronger `i == n`. Nothing about a variable assigned in a `for` body survives
the loop.

**Range membership carries no semantics.** `x in 1..10` converts, but relating
it to `x >= 1 && x <= 10` does not verify.

**`try`/`catch` loses the result type.** A function returning
`try { x / 1 } catch (e: ArithmeticException) { 0 }` fails its implicit
postcondition `isSubtype(typeOf(result), intType())`.

**Inner-class receivers are untyped.** `o.Inner().get()` fails the generated
precondition `isSubtype(typeOf(this), Outer())`. Nested (non-inner) classes are
fine.

Separately, and by design rather than as a defect: constructors and methods
carry no inferred postconditions, so after `val c = Counter(0); c.bump()`
nothing is known about `c.n`. SnaKt wants explicit contracts.

## Supported

Verified by probe, either by proving a stated property or by converting
cleanly:

- **Control flow** — `while` with `loopInvariants`, `if`/`else`, `when` with a
  subject, with ranges, and without a subject, `break`, `continue`, labelled
  `break`, `try`/`finally`.
- **Primitives** — `Int` arithmetic including `/` and `%`, `Double` arithmetic,
  `Char` comparison and ordering, `Byte`/`Short` via `toInt()`, `toLong()`,
  bitwise `and` and `shl`, boolean connectives.
- **Strings** — `length`, `get(i)`, `+` concatenation, `==`, literals.
- **Nullability** — nullable types, null checks with smart casts, `?.`, `?:`,
  the `null` literal.
- **Functions** — default and named arguments, vararg parameters, extension
  functions and extension receivers, `infix`, `tailrec`, generic functions,
  `Unit` returns.
- **Classes** — data classes with `copy` and `==`, `open`/`override` with
  virtual dispatch, interfaces with default methods, abstract classes, custom
  getters and setters, `lateinit`, `by lazy`, nested classes, `@JvmInline value
  class`, `typealias`, class delegation (`by`), generic classes, `is` and `as`.
- **Sealed hierarchies** — exhaustive `when` over a sealed interface without an
  `else`, and narrowing by exclusion (`if (e !is Lit) return false`).
- **Arrays** — `IntArray` and `Array<T>` construction, `.size`, indexed reads,
  and `while` and `for` loops over them, subject to the non-negativity gap
  above.

## Three things that make the plugin harder to use than it needs to be

**One unsupported construct hides every verification result in the module.**
SnaKt reports unsupported features at `error` severity and verification
findings at `warning` severity. The Kotlin compiler suppresses all warning
output once any error is present. So adding a single `throw` anywhere in a
module silently blanks out every verification result everywhere else — the four
expected failures in `src/main/kotlin/Main.kt` vanish, and the build just says
"compilation error". This was confirmed against a plain Kotlin
`UNUSED_VARIABLE` warning, which is suppressed the same way.

**`unsupported_feature_behaviour=assume_unreachable` destroys its own
messages.** The mode exists precisely to keep going past unsupported
constructs, but under it every message becomes `source must not be null`
instead of the real explanation. Compare, for the same code:

    throw_exception:      Not yet implemented for FirDoWhileLoopImpl (do { … })
    assume_unreachable:   source must not be null

**`@NeverConvert` is honoured for functions but not for class, local-function
or object-literal declarations.** Annotating the enclosing function does not
stop an enum class, a local `fun`, or an `object : I { … }` from being
converted, so the escape hatch does not cover the cases that most need it.

## Filed issues

All against `komiputer/snakt`.

Aborts compilation: lambda literals [#7], anonymous objects [#10].

Unsupported, reported per declaration: callable references [#8], local
functions and closures [#9], `throw` [#11], `do`/`while` [#12], `!!` [#13],
destructuring [#14], indexed assignment [#15], vararg call arguments [#16],
`Long` and `Float` constants [#17], string templates with a variable [#18],
enum classes [#19], `object` and companion references [#20].

Converts but does not verify: array `.size` non-negativity [#21],
`loopInvariants` in `for` loops [#22], range membership [#23], `try`/`catch`
result typing [#24], inner-class receivers [#25].

Plugin behaviour: `NotImplementedError` aborting compilation [#3], errors
hiding all verification warnings [#4], `assume_unreachable` losing its messages
[#5], `@NeverConvert` scope [#6], uniqueness checker unreachable from Gradle
[#26].

[#3]: https://github.com/komiputer/snakt/issues/3
[#4]: https://github.com/komiputer/snakt/issues/4
[#5]: https://github.com/komiputer/snakt/issues/5
[#6]: https://github.com/komiputer/snakt/issues/6
[#7]: https://github.com/komiputer/snakt/issues/7
[#8]: https://github.com/komiputer/snakt/issues/8
[#9]: https://github.com/komiputer/snakt/issues/9
[#10]: https://github.com/komiputer/snakt/issues/10
[#11]: https://github.com/komiputer/snakt/issues/11
[#12]: https://github.com/komiputer/snakt/issues/12
[#13]: https://github.com/komiputer/snakt/issues/13
[#14]: https://github.com/komiputer/snakt/issues/14
[#15]: https://github.com/komiputer/snakt/issues/15
[#16]: https://github.com/komiputer/snakt/issues/16
[#17]: https://github.com/komiputer/snakt/issues/17
[#18]: https://github.com/komiputer/snakt/issues/18
[#19]: https://github.com/komiputer/snakt/issues/19
[#20]: https://github.com/komiputer/snakt/issues/20
[#21]: https://github.com/komiputer/snakt/issues/21
[#22]: https://github.com/komiputer/snakt/issues/22
[#23]: https://github.com/komiputer/snakt/issues/23
[#24]: https://github.com/komiputer/snakt/issues/24
[#25]: https://github.com/komiputer/snakt/issues/25
[#26]: https://github.com/komiputer/snakt/issues/26

## Reproducing

    source ~/.sdkman/bin/sdkman-init.sh
    export JAVA_HOME=~/.sdkman/candidates/java/current
    ./gradlew compileKotlin --no-daemon --max-workers=2

A full run takes about four and a half minutes, most of it Viper. Setting
`verificationTargetsSelection("no_targets")` in the `formver` block keeps
conversion (where the unsupported-feature errors arise) and skips verification,
which is much faster when probing for conversion failures.

Note that `checkUniqueness` is no longer available in the Gradle DSL —
`FormVerExtension` does not expose it, and `FormalVerificationPluginComponentRegistrar`
hardcodes it to `false` — so the uniqueness checker cannot currently be
switched on from a Gradle build. The `@Unique` probes in
`src/main/kotlin/UniqueTest.kt` therefore exercise nothing.
