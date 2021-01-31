package com.eilas.newcourseschedule.data.model

data class LoggedInUser(
    val id: String,
    val pwd: String,
    val name: String = id,
    val sex: Sex = Sex.MALE
) {
    enum class Sex(i: Int) {
        MALE(0), FEMALE(1)
    }
}