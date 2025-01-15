package org.example.base.net

/************************************
 |     magic     |      version     |
 |----------------------------------|
 |    4 bytes    |      4 bytes     |
 |==================================|
 |       id      |      length      |
 |----------------------------------|
 |    4 bytes    |      4 bytes     |
 |==================================|
 |              data                |
 |----------------------------------|
 |        variable length           |
 ************************************/
data class Package(val header: Header, val data: ByteArray) {

    // 传输中大小为16bytes 8位对齐 小端传输
    data class Header(
        val magic: Int,     // 固定校验魔数 前后端一致
        val version: Int,   // 版本数 前后端一致

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

        fun createPackage(id: Int, data: ByteArray): Package {
            val header = Header(PACKAGE_MAGIC, PACKAGE_VERSION, id, data.size)
            return Package(header, data)
        }
    }
}