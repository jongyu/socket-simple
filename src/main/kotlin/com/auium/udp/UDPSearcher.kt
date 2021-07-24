package com.auium.udp

import com.auium.udp.MessageCreator.buildWithPort
import com.auium.udp.MessageCreator.parseSn
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch


object UDPSearcher {

    private const val LISTEN_PORT = 30000

    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("UDPSearcher Started.")
        val listener = listen()
        sendBroadcast()
        // 读取任意键盘信息后可以退出
        System.`in`.read()
        val devices = listener.devicesAndClose
        for (device: Device in devices) {
            println("Device:$device")
        }
        // 完成
        println("UDPSearcher Finished.")
    }

    @Throws(InterruptedException::class)
    private fun listen(): Listener {
        println("UDPSearcher start listen.")
        val countDownLatch = CountDownLatch(1)
        val listener = Listener(LISTEN_PORT, countDownLatch)
        listener.start()
        countDownLatch.await()
        return listener
    }

    @Throws(IOException::class)
    private fun sendBroadcast() {
        println("UDPSearcher sendBroadcast started.")
        // 作为搜索方，让系统自动分配端口
        val ds = DatagramSocket()
        // 构建一份请求数据
        val requestData = buildWithPort(LISTEN_PORT)
        val requestDataBytes = requestData.toByteArray()
        // 直接构建packet
        val requestPacket = DatagramPacket(
            requestDataBytes,
            requestDataBytes.size
        )
        // 20000端口, 广播地址
        requestPacket.address = InetAddress.getByName("255.255.255.255")
        requestPacket.port = 20000
        // 发送
        ds.send(requestPacket)
        ds.close()
        // 完成
        println("UDPSearcher sendBroadcast finished.")
    }

    private class Device(val port: Int, val ip: String, val sn: String) {
        override fun toString(): String {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}'
        }
    }

    private class Listener(private val listenPort: Int, private val countDownLatch: CountDownLatch) : Thread() {
        private val devices: MutableList<Device> = ArrayList()
        private var done = false
        private var ds: DatagramSocket? = null
        override fun run() {
            super.run()

            // 通知已启动
            countDownLatch.countDown()
            try {
                // 监听回送端口
                ds = DatagramSocket(listenPort)
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
                    println("UDPSearcher receive form ip:$ip\tport:$port\tdata:$data")
                    val sn = parseSn(data)
                    if (sn != null) {
                        val device = Device(port, ip, sn)
                        devices.add(device)
                    }
                }
            } catch (ignored: Exception) {
            } finally {
                close()
            }
            println("UDPSearcher listener finished.")
        }

        private fun close() {
            if (ds != null) {
                ds!!.close()
                ds = null
            }
        }

        val devicesAndClose: List<Device>
            get() {
                done = true
                close()
                return devices
            }

    }

}