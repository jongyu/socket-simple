package com.auium.udp

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*


object UDPProvider {

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // 生成一份唯一标示
        val sn = UUID.randomUUID().toString()
        val provider = Provider(sn)
        provider.start()

        // 读取任意键盘信息后可以退出
        System.`in`.read()
        provider.exit()
    }

    private class Provider(private val sn: String) : Thread() {
        private var done = false
        private var ds: DatagramSocket? = null
        override fun run() {
            super.run()
            println("UDPProvider Started.")
            try {
                // 监听20000 端口
                ds = DatagramSocket(20000)
                while (!done) {

                    // 构建接收实体
                    val buf = ByteArray(512)
                    val receivePack = DatagramPacket(buf, buf.size)

                    // 接收
                    ds!!.receive(receivePack)

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    val ip = receivePack.address.hostAddress
                    val port = receivePack.port
                    val dataLen = receivePack.length
                    val data = String(receivePack.data, 0, dataLen)
                    println("UDPProvider receive form ip:$ip\tport:$port\tdata:$data")
                    // 解析端口号
                    val responsePort: Int = MessageCreator.parsePort(data)
                    if (responsePort != -1) {
                        // 构建一份回送数据
                        val responseData: String = MessageCreator.buildWithSn(sn)
                        val responseDataBytes = responseData.toByteArray()
                        // 直接根据发送者构建一份回送信息
                        val responsePacket = DatagramPacket(
                            responseDataBytes,
                            responseDataBytes.size,
                            receivePack.address,
                            responsePort
                        )
                        ds?.send(responsePacket)
                    }
                }
            } catch (ignored: Exception) {
            } finally {
                close()
            }
            // 完成
            println("UDPProvider Finished.")
        }

        private fun close() {
            if (ds != null) {
                ds!!.close()
                ds = null
            }
        }

        /**
         * 提供结束
         */
        fun exit() {
            done = true
            close()
        }
    }

}