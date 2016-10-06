// !DIAGNOSTICS: -UNUSED_PARAMETER

annotation class NoArg

@NoArg
open class BaseClass(a: Int)

class ChildClass(a: String) : BaseClass(2)