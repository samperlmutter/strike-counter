package dev.samperlmutter.strikecounter.brother

import dev.samperlmutter.strikecounter.slack.SlackService
import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import dev.samperlmutter.strikecounter.strike.StrikeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class BrotherController @Autowired constructor(
    private val slackService: SlackService,
    private val strikeService: StrikeService
) {
    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun cmdHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        return when (body.command) {
            "/strikes" -> strikeService.strikesHandler(body)
            else -> ResponseEntity.ok(slackService.buildResponse("Something's wrong with the slack bot, contact the Slack Master immediately"))
        }
    }
}