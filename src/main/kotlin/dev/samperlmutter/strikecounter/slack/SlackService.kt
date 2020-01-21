package dev.samperlmutter.strikecounter.slack

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service

@Service
class SlackService {
    fun buildResponse(message: String): ObjectNode {
        val mapper = ObjectMapper()
        val json = mapper.createObjectNode()
        json.put("text", message)

        return json
    }

    fun parseUser(user: String): String {
        val regex = "(?<=@)([A-Z0-9])\\w+(?=\\||>)".toRegex()
        return regex.find(user)!!.value
    }
}