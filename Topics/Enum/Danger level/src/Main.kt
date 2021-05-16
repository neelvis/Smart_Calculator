enum class DangerLevel(val lvl: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3);
    
    fun getLevel(): Int = lvl
}
