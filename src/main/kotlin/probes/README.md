# SnaKt language-feature probes

Each file probes one area of Kotlin. Declarations that SnaKt converts are left
live; declarations it cannot convert are marked `@NeverConvert` with the exact
diagnostic SnaKt produces, so the module still builds and verifies.

Two constructs cannot be quarantined with `@NeverConvert` at all, because they
abort the entire compilation before any diagnostic is attached to a
declaration; those are commented out instead:

- lambda literals (`{ it + 1 }`)
- anonymous objects (`object : Greeter { ... }`)

Run with:

    ./gradlew compileKotlin --no-daemon --max-workers=2

Verification findings surface as *warnings*, so `BUILD SUCCESSFUL` on its own
does not mean the module verified. Note also that any `@NeverConvert` removed
from this tree turns a warning-only build into an error build, and Kotlin
suppresses all warnings once an error is present — so a single unsupported
construct hides every verification result in the module.
