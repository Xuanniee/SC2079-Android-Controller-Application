package com.sc2079.androidcontroller.features.map.domain.model

// Available Messages that can be Parsed
sealed class ParsedInbound {
    data class Protocol(val msg: ProtocolMessage) : ParsedInbound()
    data class Event(val event: RobotInboundEvent) : ParsedInbound()
}