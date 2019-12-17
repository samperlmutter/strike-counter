package dev.samperlmutter.strikecounter.brother

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class BrotherController @Autowired constructor(
    private var brotherRepository: BrotherRepository
) {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/strikes"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun strikesHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(body.user_id)
        val params = body.text.split(' ')

        return when (params[0]) {
            "add" -> addStrike(caller, params.subList(1, params.size))
            "remove" -> removeStrike(caller, params.subList(1, params.size))
            "list" -> listStrikes()
            else -> strikesHelp()
        }
    }

    @RequestMapping(
        "/points",
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun pointsHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(body.user_id)
        val params = body.text.split(' ')

        return when (params[0]) {
            "add" -> addPoints(caller, brotherRepository.findBySlackId(parseUser(params[1])), params[2].toInt())
            "remove" -> removePoints(caller, brotherRepository.findBySlackId(parseUser(params[1])), params[2].toInt())
            "list" -> listPoints()
            else -> pointsHelp()
        }
    }

    private fun addStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(parseUser(param))
                brother.strikes++
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} now has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
            }
            ResponseEntity.ok(buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(buildResponse("Sorry, you're not allowed to strike other brothers"))
        }
    }

    private fun addPoints(caller: Brother, brother: Brother, points: Int): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            brother.points += points
            brotherRepository.save(brother)
            message += "${brother.name.capitalize()} now has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
            ResponseEntity.ok(buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(buildResponse("Sorry, you're not allowed to manipulate points"))
        }
    }

    private fun removeStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(parseUser(param))
                brother.strikes -= if (brother.strikes < 1) 0 else 1
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} now has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
            }
            ResponseEntity.ok(buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(buildResponse("Sorry, you're not allowed to strike other brothers"))
        }
    }

    private fun removePoints(caller: Brother, brother: Brother, points: Int): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            brother.points = if (brother.points <= points) 0 else brother.points - points
            brotherRepository.save(brother)
            message += "${brother.name.capitalize()} now has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
            ResponseEntity.ok(buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(buildResponse("Sorry, you're not allowed to manipulate points"))
        }
    }

    private fun listStrikes(): ResponseEntity<Any> {
        var message = ""
        for (brother in brotherRepository.findAll().sortedWith(compareBy({ -it.strikes }, { it.name }))) {
            message += "• ${brother.name.capitalize()} has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
        }
        return ResponseEntity.ok(buildResponse(message.trimMargin()))
    }

    private fun listPoints(): ResponseEntity<Any> {
        var message = ""
        for (brother in brotherRepository.findAll().sortedWith(compareBy({ -it.points }, { it.name }))) {
            message += "• ${brother.name.capitalize()} has ${brother.points} point${if (brother.points == 1) "" else "s"}\n"
        }
        return ResponseEntity.ok(buildResponse(message.trimMargin()))
    }

    private fun strikesHelp(): ResponseEntity<Any> {
        val message = """
            *Available commands*:
            >*Add a strike*
            >type `/strike add @{name1} @{name2} ...` to add a strike to each user listed
            
            >*Remove a strike*
            >type `/strike remove @{name1} @{name2} ...` to remove a strike from each user listed
            
            >*List everyone's strikes*
            >type `/strike list [alpha | num]` to list how many strikes each user has, sorted alphabetically or numerically.
            >Sorts numerically by default
            
            >*Help*
            >type `/strike help` to display this message
        """.trimIndent()

        return ResponseEntity.ok(buildResponse(message.trimMargin()))
    }

    private fun pointsHelp(): ResponseEntity<Any> {
        val message = """
            *Available commands*:
            >*Add points*
            >type `/points add @{user} {number}` to add {number} points to {user}
            
            >*Remove points*
            >type `/points remove @{user} {number}` to remove {number} points from {user}
            
            >*List everyone's points*
            >type `/points list [alpha | num]` to list how many points each user has, sorted alphabetically or numerically.
            >Sorts numerically by default
            
            >*Help*
            >type `/points help` to display this message
        """.trimIndent()

        return ResponseEntity.ok(buildResponse(message.trimMargin()))
    }

    private fun buildResponse(message: String): ObjectNode {
        val mapper = ObjectMapper()
        val json = mapper.createObjectNode()
        json.put("text", message)

        return json
    }

    private fun parseUser(user: String): String {
        val regex = "(?<=@)([A-Z0-9])\\w+(?=\\||>)".toRegex()
        return regex.find(user)!!.value
    }
}