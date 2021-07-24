package com.auium

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.Socket


object Client {

    @JvmStatic
    fun main(args: Array<String>) {
        val socket = Socket()
        // 超时时间
        socket.soTimeout = 3000
        // 连接本地，端口2000；超时时间3000ms
        socket.connect(InetSocketAddress("120.79.41.195", 80), 3000)
        println("已发起服务器连接，并进入后续流程～")
        println("客户端信息：" + socket.localAddress + " 端口:" + socket.localPort)
        println("服务器信息：" + socket.inetAddress + " 端口:" + socket.port)
        try {
            // 发送接收数据
            todo(socket)
        } catch (e: Exception) {
            println("异常关闭")
        }
        // 释放资源
        socket.close()
        println("客户端已退出～")
    }

    private fun todo(client: Socket) {
        // 构建键盘输入流
        val input = BufferedReader(InputStreamReader(System.`in`))
        // 得到Socket输出流，并转换为打印流
        val socketPrintStream = PrintStream(client.getOutputStream())
        // 得到Socket输入流，并转换为BufferedReader
        val socketBufferedReader = BufferedReader(InputStreamReader(client.getInputStream()))
        var flag = true
        do {
            // 键盘读取一行
            val str = input.readLine()
            // 发送到服务器
            socketPrintStream.println(str)
            // 从服务器读取一行
            val echo = socketBufferedReader.readLine()
            if ("bye".equals(echo, ignoreCase = true)) {
                flag = false
            } else {
                println(echo)
            }
        } while (flag)

        // 资源释放
        socketPrintStream.close()
        socketBufferedReader.close()
    }

}