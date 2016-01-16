package pl.aetas.slack.pushq

data class SlackResponse(val responseType: SlackResponseType, val text: String, val attachments: List<SlackAttachment> = emptyList())

enum class SlackResponseType {
    in_channel, ephemeral
}

data class SlackAttachment(val text: String)