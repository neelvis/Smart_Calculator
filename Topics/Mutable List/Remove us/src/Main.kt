fun solution(elements: MutableList<String>, index: Int): MutableList<String> { 
    val result = elements
    result.removeAt(index)
    return result
}
