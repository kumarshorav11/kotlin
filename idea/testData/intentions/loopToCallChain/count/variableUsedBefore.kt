// WITH_RUNTIME
// INTENTION_TEXT: "Replace with 'filter{}.forEach{}'"
// INTENTION_TEXT_2: "Replace with 'asSequence().filter{}.forEach{}'"
fun foo(list: List<String>): Int {
    var count = 0
    <caret>for (s in list) {
        if (s.length > count) {
            count++
        }
    }
    return count
}