package com.todaystudio.soha.utils

import java.util.*

object DataUtil {
    private val morningGreetings = listOf("Good Morning", "Hello", "What's up?", "Howdy!", "G'day mate", "Hiya!", "Have a nice day")
    private val afternoonGreetings = listOf("Good afternoon", "How are you doing?", "How's your day going?")
    private val nightGreetings = listOf("Good evening", "How's your day?", "Good to see you", "How's it going?", "How's are things?", "How have you been?")

    fun generateGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> morningGreetings
            in 12..18 -> afternoonGreetings
            in 0..4, in 19..23 -> nightGreetings
            else -> listOf("How's it going?")
        }.random()
        val emoji = when (hour) {
            in 5..8 -> listOf("\uD83C\uDF05", "\uD83C\uDF04", "\uD83C\uDF3B").random()
            in 9..11 -> "\uD83C\uDF24️"
            in 11..13 -> "☀️"
            in 14..16 -> listOf("⛅", "\uD83C\uDF25️").random()
            in 17..19 -> "\uD83C\uDF07"
            in 20..23, in 0..4 -> listOf("\uD83C\uDF11", "\uD83C\uDF19", "\uD83C\uDF18", "\uD83C\uDF18", "☄️").random()
            else -> ""
        }
        return "$greeting $emoji"
    }
}