enum class Currency (val currency: String) {
    Germany("Euro"),
    Mali("CFA franc"),
    Dominica("Eastern Caribbean dollar"),
    Canada("Canadian dollar"),
    Spain("Euro"),
    Australia("Australian dollar"),
    Brazil("Brazilian real"),
    Senegal("CFA franc"),
    France("Euro"),
    Grenada("Eastern Caribbean dollar"),
    Kiribati("Australian dollar");

    companion object {
        fun haveSameCurrency(country1: String, country2: String) = try {
            valueOf(country1).currency == valueOf(country2).currency
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

fun main() = readLine()!!
    .split(" ")
    .let { Currency.haveSameCurrency(it[0], it[1]) }
    .let(::println)