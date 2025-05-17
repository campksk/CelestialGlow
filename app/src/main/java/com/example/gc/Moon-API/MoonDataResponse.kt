data class MoonDataResponse(
    val timestamp: Int,
    //val datestamp: String,
    val moon: Moon
)

data class Moon(
    val phase: String,
    val phase_name: String,
    val emoji: String,
    val illumination: String,
    val detailed: Detailed
)

data class Detailed(
    val position: Position
)

data class Position(
    val distance: Double
)