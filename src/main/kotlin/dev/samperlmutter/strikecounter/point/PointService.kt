package dev.samperlmutter.strikecounter.point

import dev.samperlmutter.strikecounter.brother.Brother
import dev.samperlmutter.strikecounter.brother.BrotherRepository
import dev.samperlmutter.strikecounter.slack.SlackService
import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PointService @Autowired constructor(
    private val slackService: SlackService,
    private val brotherRepository: BrotherRepository
) {
    fun pointsHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(body.user_id)
        val params = body.text.split(' ')

        return when (params[0]) {
            "add" -> addPoints(caller, brotherRepository.findBySlackId(slackService.parseUser(params[1])), params[2].toInt())
            "remove" -> removePoints(caller, brotherRepository.findBySlackId(slackService.parseUser(params[1])), params[2].toInt())
            "list" -> listPoints()
            else -> pointsHelp()
        }
    }

    fun addPoints(caller: Brother, brother: Brother, points: Int): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            brother.points = brother.points.plus(points)
            brotherRepository.save(brother)
            message += "${brother.name.capitalize()} now has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
            ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manipulate points"))
        }
    }

    fun removePoints(caller: Brother, brother: Brother, points: Int): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            brother.points = if (brother.points <= points) 0 else brother.points - points
            brotherRepository.save(brother)
            message += "${brother.name.capitalize()} now has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
            ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manipulate points"))
        }
    }

    fun listPoints(): ResponseEntity<Any> {
        var message = ""
        for (brother in brotherRepository.findAll().sortedWith(compareBy({ -it.points }, { it.name }))) {
            message += "• ${brother.name.capitalize()} has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
        }
        return ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
    }

    fun pointsHelp(): ResponseEntity<Any> {
        val message = """
            *Available commands*:
            >*Add points*
            >Type `/points add @{user} {number}` to add {number} points to {user}

            >*Remove points*
            >Type `/points remove @{user} {number}` to remove {number} points from {user}

            >*List everyone's points*
            >Type `/points list [alpha | num]` to list how many points each user has, sorted alphabetically or numerically
            >Sorts numerically by default

            >*Help*
            >Type `/points help` to display this message
        """.trimIndent()

        return ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
    }
}