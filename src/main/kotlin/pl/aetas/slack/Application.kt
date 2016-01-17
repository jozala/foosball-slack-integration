package pl.aetas.slack

import org.wasabi.app.AppServer

fun main(args : Array<String>) {

    val server = AppServer()

    server.post("/integration", {

    })

    server.start()
}