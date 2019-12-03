package dev.samperlmutter.strikecounter.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class SlackSlashCommand(
    val user_id: String,
    val text: String
)