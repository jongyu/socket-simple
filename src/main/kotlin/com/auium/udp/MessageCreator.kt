package com.auium.udp

object MessageCreator {

    private const val SN_HEADER = "收到暗号，我是（SN）:"
    private const val PORT_HEADER = "这是暗号，请回电端口（Port）:"

    fun buildWithPort(port: Int): String {
        return PORT_HEADER + port
    }

    fun parsePort(data: String): Int {
        return if (data.startsWith(PORT_HEADER)) {
            data.substring(PORT_HEADER.length).toInt()
        } else -1
    }

    fun buildWithSn(sn: String): String {
        return SN_HEADER + sn
    }

    fun parseSn(data: String): String? {
        return if (data.startsWith(SN_HEADER)) {
            data.substring(SN_HEADER.length)
        } else null
    }
}