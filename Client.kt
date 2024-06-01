package org.example

import java.net.Socket

class Client(val host: String = "localhost", val port: Int = 8080) {
    private var socket: Socket? = null
    lateinit var connection: Connection

    fun start(login: String) {
        try {
            socket = Socket(host, port)
            connection = Connection(socket!!)
            connection.send(login) // Отправка логина на сервер
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun send(text: String) = connection.send(text)
    fun receive() = connection.receive()
}
