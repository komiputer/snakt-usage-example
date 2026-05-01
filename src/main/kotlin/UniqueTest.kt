package jesyspa

import org.jetbrains.kotlin.formver.plugin.Borrowed
import org.jetbrains.kotlin.formver.plugin.Unique

class Box()

fun consume(@Unique x: Any) {}
fun borrow(@Borrowed x: Any) {}
fun share(x: Any) {}
fun borrowUnique(@Unique @Borrowed x: Any) {}

// Passing cases — exercise that the uniqueness checker accepts what it
// should accept. Build green = these compile under `checkUniqueness(true)`.

fun consumeUnique(@Unique b: Box) {
    consume(b)
}

fun consumeAfterBorrowUnique(@Unique b: Box) {
    borrow(b)
    consume(b)
}

fun shareUnique(@Unique b: Box) {
    share(b)
}

fun transferUnique(@Unique b: Box) {
    @Unique val alias = b
    consume(alias)
}

// Failing cases — kept as documentation. Each block, when uncommented,
// produces a `Argument uniqueness mismatch` compile error from the
// uniqueness checker. The expected mismatch is noted on each line.
// We keep these out of the live source so `main` builds cleanly under
// the project's main-direct workflow.

/*
fun consumeShared(b: Box) {
    consume(b)              // expected: actual 'shared global'
}

fun consumeBorrowed(@Borrowed b: Box) {
    consume(b)              // expected: actual 'shared local'
}

fun consumeUniqueBorrowed(@Unique @Borrowed b: Box) {
    consume(b)              // expected: actual 'unique local' (borrowed even if unique)
}

fun doubleConsume(@Unique b: Box) {
    consume(b)
    consume(b)              // expected: actual 'moved' (already consumed)
}

fun consumeAfterShareUnique(@Unique b: Box) {
    share(b)
    consume(b)              // expected: actual 'shared global' (sharing degrades uniqueness)
}

fun shareBorrowed(@Borrowed b: Box) {
    share(b)                // expected: actual 'shared local'
}

fun shareUniqueBorrowed(@Unique @Borrowed b: Box) {
    share(b)                // expected: actual 'unique local'
}

fun aliasUnique(@Unique b: Box) {
    val alias = b
    consume(alias)          // expected: actual 'shared global' — `val alias = b` shares
}

fun transferUniqueThenUseB(@Unique b: Box) {
    @Unique val alias = b
    consume(b)              // expected: actual 'moved' — alias took ownership
}
*/
