package za.varsitycollege.kronosapp

data class Category(
    val name: String = "",
    var totalHours: Float = 0f
) {
    constructor() : this("", 0f)
}
