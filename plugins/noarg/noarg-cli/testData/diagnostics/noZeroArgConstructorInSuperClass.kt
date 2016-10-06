// !DIAGNOSTICS: -UNUSED_PARAMETER

annotation class NoArg

@NoArg
annotation class MyAnno

abstract class BaseClass(a: String)

@NoArg
interface BaseIntf

@MyAnno
open class BaseDefault(a: String = "", b: Int = 5)

abstract class BaseDefault2(a: String = "") {
    constructor() : this("")
}

open class BaseNoDefault {
    constructor(a: Int) {}
    constructor(a: String) {}
}

@NoArg
class <!NO_NOARG_CONSTRUCTOR_IN_SUPERCLASS!>Test1<!>(a: Int) : BaseClass("")

class Test2(a: Int) : BaseIntf

class Test3(a: Int) : BaseDefault("", 4)

@NoArg
class Test4(a: Int) : BaseDefault2("")

@MyAnno
class <!NO_NOARG_CONSTRUCTOR_IN_SUPERCLASS!>Test5<!>(a: Int) : BaseNoDefault("")

@NoArg
class <!NO_NOARG_CONSTRUCTOR_IN_SUPERCLASS!>Test6<!>() : BaseNoDefault("")

//

object TestObj : BaseClass("")

interface TestIntf : BaseIntf