// !DIAGNOSTICS: -DEPRECATION

<!UNDERSCORE_IS_RESERVED!>import kotlin.Deprecated as ___<!>

@___("") data class Pair(val x: Int, val y: Int)

class <!UNDERSCORE_IS_RESERVED!>_<!><<!UNDERSCORE_IS_RESERVED!>________<!>>
val <!UNDERSCORE_IS_RESERVED!>______<!> = _<Int>()

fun <!UNDERSCORE_IS_RESERVED!>__<!>(<!UNDERSCORE_IS_RESERVED!>___<!>: Int, y: _<Int>?): Int {
    val (_, <!UNUSED_VARIABLE!>z<!>) = Pair(___ - 1, 42)
    val (x, <!UNDERSCORE_IS_RESERVED!>__________<!>) = Pair(___ - 1, 42)
    val <!UNDERSCORE_IS_RESERVED!>____<!> = x
    // in backquotes: allowed
    val `_` = __________
    <!UNDERSCORE_IS_RESERVED!>__<!>@ return if (y != null) __(____, y) else __(`_`, ______)
}

// one underscore parameters for named function is still prohibited
fun oneUnderscore(<!UNDERSCORE_IS_RESERVED!>_<!>: Int) {}

fun doIt(f: (Any?) -> Any?) = f(null)

val something = doIt { <!UNDERSCORE_IS_RESERVED!>__<!> -> __ }
val something2 = doIt { _ -> 1 }
