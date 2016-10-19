// FILE: A.kt
package test

class Cell<T>(val x: T)

fun foo() = {
    typealias A = String
    typealias CA = Cell<A>
    typealias CCA = Cell<CA>
    CCA(CA("OK"))
}

// FILE: B.kt
import test.*

fun box() = foo()().x.x

