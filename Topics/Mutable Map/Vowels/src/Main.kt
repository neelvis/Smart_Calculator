fun main() {
    val letter = readLine()!!.first().toLowerCase()
    // write your code here
    val map = mutableMapOf('a' to 1, 'e' to 5, 'i' to 9, 'o' to 15, 'u' to 21).withDefault { 0 }
    print(map[letter])
}