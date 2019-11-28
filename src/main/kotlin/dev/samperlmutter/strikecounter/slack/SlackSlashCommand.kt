package dev.samperlmutter.strikecounter.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class SlackSlashCommand(val token: String,
                        val team_id: String?,
                        val team_domain: String?,
                        val enterprise_id: String?,
                        val enterprise_name: String?,
                        val channel_id: String,
                        val channel_name: String,
                        val user_id: String,
                        val user_name: String,
                        val command: String,
                        val text: String?,
                        val response_url: String,
                        val trigger_id: String)