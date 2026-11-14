package fi.uutishetki

enum class Appearance(val label: String) {
    SYSTEM("Järjestelmän mukaan"), LIGHT("Vaalea"), DARK("Tumma");
    fun isDark(systemDark: Boolean) = when (this) { SYSTEM -> systemDark; LIGHT -> false; DARK -> true }
    companion object {
        fun decode(value: String?) = entries.find { it.name == value } ?: SYSTEM
    }
}
