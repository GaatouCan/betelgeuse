﻿package org.example.base.net

/************************************
 |     magic     |      version     |
 |----------------------------------|
 |    4 bytes    |      4 bytes     |
 |==================================|
 |method |reverse|        id        |
 |----------------------------------|
 |2 bytes|2 bytes|      4 bytes     |
 |==================================|
 |             length               |
 |----------------------------------|
 |             8 bytes              |
 |==================================|
 |              data                |
 |----------------------------------|
 |        variable length           |
 ************************************/
data class Package(val header: Header, val data: ByteArray) {

    // 传输中大小为24bytes 8位对齐 大端传输
    data class Header (
        val magic: Int,     // 固定校验魔数 前后端一致
        val version: Int,   // 版本数 前后端一致
        val method: Int,
        var id: Int,        // 协议ID
        var length: Int     // 字节流长度
    )

    init {
        if (header.length == 0) {
            header.length = data.size
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Package

        if (header != other.header) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    companion object {

        const val PACKAGE_MAGIC = 20241231
        const val PACKAGE_VERSION = 1001
        const val PACKAGE_CODEC_METHOD = 0

        fun buildPackage(id: Int, data: ByteArray): Package {
            val header = Header(PACKAGE_MAGIC, PACKAGE_VERSION, PACKAGE_CODEC_METHOD, id, data.size)
            return Package(header, data)
        }
    }
}