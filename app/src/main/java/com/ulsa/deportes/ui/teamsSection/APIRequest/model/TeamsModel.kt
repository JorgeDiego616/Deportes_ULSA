package com.ulsa.deportes.ui.teamsSection.APIRequest.model



data class Team(
    val teamName: String,
    val sport: String,
    val coachName: String,
    val photoUrl: String
)

data class TeamsResponse(
    val teams: List<Team>
)

//val respuesta = StudentsResponse(
//    students = listOf(
//        Student("Jorge Diego Chaparro Núñez", "14446", "Read, Gym and Videogames", "https://pbs.twimg.com/media/HCEaIijWAAAUrwn?format=jpg&name=large"),
//        Student("César Chavira", "12366", "Sports", "https://images.meme-arsenal.com/66c6c9651f0f1fb7ba476cb032cb9e19.jpg"),
//        Student("Justin Contreras", "13634", "Tenis", "https://media.spoferan.com/sport-types/11/images/tennis-jpg/1248x702.webp")
//    )
//)
