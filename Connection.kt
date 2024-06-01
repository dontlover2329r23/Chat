package org.example

import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket

class Connection(val socket: Socket) {

    private var pw: PrintWriter? = null
    private var br: BufferedReader? = null

    fun send(text: String) {
        if (pw == null) {
            pw = socket.getOutputStream()?.let { PrintWriter(it, true) }
        }
        pw?.println(text)
    }

    fun receive(): String? {
        if (br == null) {
            br = socket.getInputStream()?.bufferedReader()
        }
        return br?.readLine()
    }

    fun finish() {
        pw?.close()
        br?.close()
    }

    fun retrieveSocket(): Socket? {
        return socket
    }
}
