package com.auium

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket


object Server {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val server = ServerSocket(80)
        println("服务器准备就绪～")
        println("服务器信息：" + server.inetAddress + " P:" + server.localPort)
        // 等待客户端连接
        while (true) {

            // 得到客户端
            val client = server.accept()
            // 客户端构建异步线程
            val clientHandler = ClientHandler(client)
            // 启动线程
            clientHandler.start()
        }
    }

    /**
     * 客户端消息处理
     */
    private class ClientHandler(private val socket: Socket) : Thread() {
        private var flag = true
        override fun run() {
            super.run()
            println("新客户端连接：" + socket.inetAddress + " P:" + socket.port)
            try {
                // 得到打印流，用于数据输出；服务器回送数据使用
                val socketOutput = PrintStream(socket.getOutputStream())
                // 得到输入流，用于接收数据
                val socketInput = BufferedReader(InputStreamReader(socket.getInputStream()))
                do {
                    // 客户端拿到一条数据
                    val str = socketInput.readLine()
                    if ("bye".equals(str, ignoreCase = true)) {
                        flag = false
                        // 回送
                        socketOutput.println("bye")
                    } else {
                        // 打印到屏幕。并回送数据长度
                        println(str)
                        socketOutput.println("回送：" + str.length)
                    }
                } while (flag)
                socketInput.close()
                socketOutput.close()
            } catch (e: Exception) {
                println("连接异常断开")
            } finally {
                // 连接关闭
                try {
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            println("客户端已退出：" + socket.inetAddress + " P:" + socket.port)
        }
    }
}
