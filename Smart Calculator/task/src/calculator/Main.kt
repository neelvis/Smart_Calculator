import java.lang.NumberFormatException
import java.math.BigInteger
import java.util.*

data class ValidationObject(val message: String = "Default", val valid: Boolean, val service: Calculator.CalculationType = Calculator.CalculationType.DEFAULT)

class Calculator {
    enum class CalculationType {
        INIT_VAR_WITH_NUM,
        INIT_VAR_WITH_VAR,
        SHOW_VAR_VALUE,
        CALCULATE,
        SHOW_ENTERED_VALUE,
        DEFAULT;
    }
    private val priority = mapOf<String, Int>("^" to 3, "*" to 2, "/" to 2, "+" to 1, "-" to 1)
    private val variables = mutableMapOf<String, BigInteger>()
    private var rpn = ""
    private var stack = Stack<String>()

    fun showHelp() {
        println("""
        Enter expression and I will calc it for you!
        EXAMPLE: a = 5
        EXAMPLE: -3 + a * ((4 + 3) * 2 + 1) - 6 / (2 + 1) ^ 2
    """.trimIndent())
    }
    /*
     * function goes through the input string and checks whether input is correct
     * returns ValidationObject: message, expression type and flag "valid" (true/false)
    * Invalid input examples:
    * /hello (or other word) -> Unknown command
    * 22 5 -> Invalid expression
    * 11 - 5 + -> Invalid expression
    * 11 ** 5 -> Invalid expression
    * a = b = 6 -> Invalid assignment
    * a1 = 5 (or a = a1) -> Invalid identifier
    *
    * Valid input examples:
    * 58 -> Number entered, print it
    * a = 5 -> Assign 5 to var 'a'
    * a -> Print var 'a' value
    * -3 +++ 8 * ((4 + 3) * 2 + 1) - 6 / (2 + 1) ^ 2 -> Good, parse it*/
    fun validateInput(s: String): ValidationObject {
        if (s[0] == '/') return ValidationObject("Unknown command", valid = false)
        if (s.toBigIntegerOrNull() != null) return ValidationObject(valid = true, service = CalculationType.SHOW_ENTERED_VALUE)
        if (s.contains('=')) return validateAssignment(s.replace(Regex("\\s+"), ""))
        if (s.contains(Regex("[^-+*/^()a-zA-Z0-9\\s] | \\d\\s\\d | $[^0-9)] | [*/][*/]"))) return ValidationObject("Invalid expression #1", valid = false)
        if (s.matches(Regex("[a-zA-Z]+"))) {
            return if (!variables.containsKey(s)) ValidationObject("Unknown variable #1", valid = false)
            else ValidationObject(s, valid = true, service = CalculationType.SHOW_VAR_VALUE)
        }
        if (s.count { it == '(' } != s.count { it == ')' }) return ValidationObject("Invalid expression #2", valid = false)

        return parseInput(s)
    }
    private fun validateAssignment(s: String): ValidationObject {
        val variable = s.split(Regex("="))
        if (variable.size > 2) return ValidationObject(message = "Invalid assignment #1", valid = false)
        val varName = variable[0]
        val varValue = variable[1]
        if (!varName.matches(Regex("[a-zA-Z]+")))
            return ValidationObject(message = "Invalid identifier #1", valid = false)
        return try {
            varValue.toBigInteger()
            ValidationObject("$varName=$varValue", valid = true, service = CalculationType.INIT_VAR_WITH_NUM)
        } catch (e: NumberFormatException) {
            if (!varValue.matches(Regex("[a-zA-Z]+"))) ValidationObject(message = "Invalid identifier #2", valid = false)
            else {
                if (variables.containsKey(varValue)) ValidationObject("$varName=$varValue", valid = true, service = CalculationType.INIT_VAR_WITH_VAR)
                else ValidationObject("Unknown variable #2", valid = false)
            }
        }
    }
    /*
    * after input is validated, it goes through parsing function
    * Parsing rules
    * remove all spaces
    * treat -- as +
    * treat +++++ as +
    * split all symbols with spaces
    * valid parsed string: - 3 + 8 * ( ( 4 + 3 ) * 2 + 1 ) - 6 / ( 2 + 1 ) ^ 2 */
    private fun parseInput(input_: String): ValidationObject {
        rpn = ""
        val elements = mutableListOf<String>()
        var tempNumber = BigInteger.ZERO
        var flagNumber = false
        var input = input_
        // remove spaces, fold - and + signs, add extra space to the end for parsing
        for (v in variables) {
            input = input.replace(v.key, v.value.toString())
        }
        for (c in input.replace(Regex("\\s+"), "")
            .replace("--", "+")
            .replace(Regex("[+]+"), "+") + " ") {
            if (c in '0'..'9') {
                flagNumber = true
                tempNumber *= BigInteger.TEN
                tempNumber += (c - '0').toBigInteger()
            } else {
                if (flagNumber) {
                    elements.add(tempNumber.toString())
                    flagNumber = false
                    tempNumber = BigInteger.ZERO
                    elements.add(c.toString())
                } else {
                    elements.add(c.toString())
                }
            }
        }
        elements.removeLast() // removing the last space symbol
//    Dijkstra parsing
        elements.forEachIndexed { i, item ->
            if (item.toBigIntegerOrNull() != null) {
                rpn += "$item "
            } else if (item == "(") {
                stack.push(item)
            } else if (item == ")") {
                bracket@do {
                    if (stack.isEmpty()) return ValidationObject("Invalid expression #5", valid = false)
                    val s = stack.pop() ?: return ValidationObject("Invalid expression #3", valid = false)
                    if (s == "(") break@bracket
                    else rpn += "$s "
                } while (true)
            } else if (item in "-+/*^") {
                //            checks unary -
                if (item == "-" && i == 0 ||
                    item == "-" && elements[i - 1] in "+-*/^" && elements[i + 1].toBigIntegerOrNull() != null) {
                    stack.push("~")
                } else {
                    while (stack.isNotEmpty() && stack.peek() != "(" &&
                        (stack.peek() == "~" || priority[stack.peek()]!! >= priority[item[0].toString()]!!)) {
                        rpn += "${stack.pop()} "
                    }
                    stack.push(item[0].toString())
                }
            } else if (item.contains(Regex("[a-zA-Z]"))) {
                if (!variables.containsKey(item)) return ValidationObject("Unknown variable #3", valid = false)
                stack.push(variables[item]!!.toString())
            }
        }
        while (stack.isNotEmpty()) {
            if (stack.peek() in "()") return ValidationObject("Invalid expression #4", valid = false)
            rpn += "${stack.pop()} "
        }
        rpn = rpn.dropLast(1)
        // println(rpn)

        return ValidationObject(valid = true, service = CalculationType.CALCULATE)
    }

    fun showVariable(varName: String) = println(variables[varName])
    fun initVariableWithNum(varParams: String) {
        val (varName, varValue) = varParams.split('=')
        variables[varName] = varValue.toBigInteger()
    }
    fun initVariableWithValue(varParams: String) {
        val (varName, varValue) = varParams.split('=')
        variables[varName] = variables[varValue]!!
    }

    fun calculate() {
        for (elem in rpn.split(" ")) {
            when {
                elem.toBigIntegerOrNull() != null -> stack.push(elem)
                variables.containsKey(elem) -> stack.push(variables[elem].toString())
                elem == "~" -> {
                    val n = stack.pop().toBigInteger()
                    stack.push((-n).toString())
                }
                else -> {
                    val n2 = stack.pop().toBigInteger()
                    val n1 = stack.pop().toBigInteger()
                    when (elem) {
                        "+" -> stack.push((n1 + n2).toString())
                        "-" -> stack.push((n1 - n2).toString())
                        "*" -> stack.push((n1 * n2).toString())
                        "/" -> stack.push((n1 / n2).toString())
                    }
                }
            }
        }
        println(stack.pop())

    }
    fun showNumber(s: String) = println(s)
}

fun main(args: Array<String>){
    val calc = Calculator()
    do {
        val input = readLine()!!
        if (input == "/exit") {
            print("Bye!")
            break
        }
        if (input == "/help") {
            calc.showHelp()
            continue
        }
        if (input.isEmpty() || input.isBlank()) continue
        val validationResult = calc.validateInput(input)
        if (!validationResult.valid) {
            println(validationResult.message)
            continue
        }
        when (validationResult.service) {
            Calculator.CalculationType.SHOW_VAR_VALUE -> calc.showVariable(validationResult.message)
            Calculator.CalculationType.INIT_VAR_WITH_VAR -> calc.initVariableWithValue(validationResult.message)
            Calculator.CalculationType.INIT_VAR_WITH_NUM -> calc.initVariableWithNum(validationResult.message)
            Calculator.CalculationType.CALCULATE -> calc.calculate()
            Calculator.CalculationType.SHOW_ENTERED_VALUE -> calc.showNumber(input)
            Calculator.CalculationType.DEFAULT -> println("Calculation error, please contact developers ($input)")
        }
    } while(true)
}