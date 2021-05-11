fun main() {
    val words = mutableMapOf<String, Int>()
    do {
        val input = readLine()!!
        if (input == "stop") break
        words[input] = (words[input] ?: 0) + 1
    } while (true)

    print(words.maxByOrNull { it.value }?.key ?: "null")
}