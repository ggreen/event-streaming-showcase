package showcase.streaming.event.rabbitmq.streaming.account.domain

data class Location(
    var id: String = "",
    var address: String= "",
    var cityTown: String = "",
    var stateProvince: String = "",
    var zipPostalCode: String = "",
    var countryCode : String = ""
)
