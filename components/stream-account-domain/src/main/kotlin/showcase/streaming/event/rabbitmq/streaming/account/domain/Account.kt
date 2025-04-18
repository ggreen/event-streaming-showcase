package showcase.streaming.event.rabbitmq.streaming.account.domain

    data class Account(
        var id : String = "",
        var name: String = "",
        var accountType : String = "",
        var status : String  = "",
        var notes : String = "",
        var location: Location = Location()
    )
