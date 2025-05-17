data class AirQualityResponse(
    val status: String,
    val data: AirData
)

data class AirData(
    /*val city: String,
    val state: String,
    val country: String,*/
    val current: CurrentData
)

data class CurrentData(
    val pollution: PollutionData
)

data class PollutionData(
    val aqius: Int,
    val mainus: String
)
