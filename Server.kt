package org.example

import java.net.ServerSocket
import kotlin.concurrent.thread

class Server(port: Int = 8080) {
    private val serverSocket: ServerSocket = ServerSocket(port)

    fun start() {
        while (true) {
            val clientSocket = serverSocket.accept()
            thread {
                var isClientConnected = true
                val connection = Connection(clientSocket)

                // Ожидаем получения логина от клиента
                val login = connection.receive()
                if (login != null) {
                    val connectedClient = ConnectedClient(clientSocket, login)

                    while (isClientConnected) {
                        try {
                            val text = connection.receive()
                            if (text != null) {
                                when {
                                    text.startsWith("/w ") -> {
                                        val parts = text.split(" ", limit = 3)
                                        if (parts.size == 3) {
                                            val toLogin = parts[1]
                                            val privateMessage = parts[2]
                                            connectedClient.sendPrivateMessage(toLogin, privateMessage)
                                        }
                                    }
                                    text.startsWith("/") -> {
                                        connectedClient.handleCommand(text)
                                    }
                                    else -> {
                                        val formattedMessage = connectedClient.formatMessage(
                                            connectedClient.currentChannel ?: "All",
                                            connectedClient.login,
                                            text
                                        )
                                        if (connectedClient.currentChannel != null) {
                                            connectedClient.sendToChannel(connectedClient.currentChannel!!, formattedMessage)
                                        } else {
                                            connectedClient.sendAll(formattedMessage)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            connectedClient.removeClient()
                            connection.finish()
                            isClientConnected = false
                        }
                    }
                } else {
                    connection.finish()
                }
            }
        }
    }
}

fun main() {
    val server = Server()
    server.start()
}
