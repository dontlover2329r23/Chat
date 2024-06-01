package org.example

import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConnectedClient(val socket: Socket, val login: String) {
    companion object {
        var clients = mutableListOf<ConnectedClient>()
        var channels = mutableMapOf<String, MutableList<ConnectedClient>>()
    }

    var connection: Connection = Connection(socket)
    var currentChannel: String? = null

    init {
        clients.add(this)
        sendAllClientsList()
    }

    fun removeClient() {
        clients.remove(this)
        println("now " + clients.size + " clients")
        sendAllClientsList()
    }

    fun send(text: String) = connection.send(text)
    fun sendAll(text: String) = clients.forEach {
        it.send(text)
    }

    fun sendToChannel(channel: String, text: String) {
        channels[channel]?.forEach {
            it.send(text)
        }
    }

    fun sendPrivateMessage(toLogin: String, message: String) {
        val recipient = clients.find { it.login == toLogin }
        recipient?.send("Private message from $login: $message")
        if (recipient != null) {
            this.send("Private message to $toLogin: $message")
        } else {
            this.send("User $toLogin not found")
        }
    }

    fun sendAllClientsList() {
        val clientsList = clients.joinToString(separator = ",") {
            "${it.login} (${it.connection.retrieveSocket()?.inetAddress?.hostAddress})"
        }
        sendAll("CLIENTS_LIST:$clientsList")
    }

    fun handleCommand(command: String) {
        when {
            command.startsWith("/create_channel ") -> {
                val channelName = command.removePrefix("/create_channel ").trim()
                if (channels.containsKey(channelName)) {
                    send("Channel $channelName already exists")
                } else {
                    channels[channelName] = mutableListOf(this)
                    currentChannel = channelName
                    send("Channel $channelName created and you joined")
                }
            }
            command.startsWith("/join ") -> {
                val channelName = command.removePrefix("/join ").trim()
                if (channels.containsKey(channelName)) {
                    channels[channelName]?.add(this)
                    currentChannel = channelName
                    send("Joined channel $channelName")
                } else {
                    send("Channel $channelName does not exist")
                }
            }
            command.startsWith("/exit") -> {
                currentChannel = null
                send("Exited current channel")
            }
            command.startsWith("/all ") -> {
                val message = command.removePrefix("/all ").trim()
                val formattedMessage = formatMessage("All", login, message)
                sendAll(formattedMessage)
            }
            else -> {
                send("Unknown command")
            }
        }
    }

    fun formatMessage(channel: String, login: String, message: String): String {
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        return "[$currentTime] [$channel] $login: $message"
    }
}
