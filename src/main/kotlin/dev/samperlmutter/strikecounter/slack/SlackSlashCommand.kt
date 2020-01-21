package dev.samperlmutter.strikecounter.slack

class SlackSlashCommand(
    val user_id: String,
    val command: String,
    val text: String
)