package org.example

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.concurrent.thread

lateinit var client: Client

@Composable
@Preview
fun App() {
    var login by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var messages = remember { mutableStateListOf<String>() }
    var clientsList = remember { mutableStateListOf<String>() }

    Column {
        Row {
            Column(modifier = Modifier.weight(8f)) {
                Box(modifier = Modifier.weight(9f).fillMaxSize().verticalScroll(rememberScrollState())) {
                    Column {
                        for (m in messages) {
                            Text(m)
                        }
                    }
                }
                Row {
                    OutlinedTextField(modifier = Modifier.weight(8f), value = message, onValueChange = { message = it })
                    Button(
                        onClick = {
                            if (isLogin) {
                                if (message.startsWith("/")) {
                                    client.send(message)
                                } else {
                                    text = message
                                    client.send(text)
                                }
                            } else {
                                isLogin = true
                                login = message
                                text = "Добро пожаловать $login"

                                client = Client()
                                client.start(login) // Передача логина на сервер
                                client.send(text)
                                thread {
                                    while (true) {
                                        val t = client.receive()
                                        if (t != null) {
                                            if (t.startsWith("CLIENTS_LIST:")) {
                                                val clients = t.removePrefix("CLIENTS_LIST:").split(",")
                                                clientsList.clear()
                                                clientsList.addAll(clients)
                                            } else {
                                                messages.add(t)
                                            }
                                        }
                                    }
                                }
                            }

                            text = ""
                            message = ""
                        }
                    ) {
                        if (!isLogin) {
                            Text("Ввести логин")
                        } else {
                            Text("Отправить")
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(2f).fillMaxHeight().padding(8.dp)) {
                Text("Подключенные клиенты:", style = MaterialTheme.typography.h6)
                for (client in clientsList) {
                    Text(client)
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
