package org.example.base.net

import io.netty.util.Recycler
import io.netty.util.concurrent.FastThreadLocal

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
class Package private constructor(
    private val handle: Recycler.Handle<Package>
) {

    var header: Header = Header(PACKAGE_MAGIC, PACKAGE_VERSION, PACKAGE_CODEC_METHOD, 0, 0)
    var data: ByteArray = byteArrayOf()

    // 传输中大小为24bytes 8位对齐 大端传输
    data class Header (
        val magic: Int,     // 固定校验魔数 前后端一致
        val version: Int,   // 版本数 前后端一致
        val method: Int,
        var id: Int,        // 协议ID
        var length: Int     // 字节流长度
    )

    fun setId(id: Int): Package {
        header.id = id
        return this
    }

    fun setData(data: ByteArray): Package {
        this.data = data
        this.header.length = data.size
        return this
    }

    fun recycle() {
        header.id = 0
        header.length = 0

        data = byteArrayOf()

        handle.recycle(this)
    }

    companion object {

        const val PACKAGE_MAGIC = 20241231
        const val PACKAGE_VERSION = 1001
        const val PACKAGE_CODEC_METHOD = 0

        private val RECYCLER = object : Recycler<Package>() {
            override fun newObject(handle: Handle<Package>): Package {
                return Package(handle)
            }
        }

        private val THREAD_LOCAL_PACKAGE = object : FastThreadLocal<Package>() {
            override fun initialValue(): Package {
                return RECYCLER.get()
            }
        }

        fun newInstance(id: Int, data: ByteArray): Package {
            val pkg = THREAD_LOCAL_PACKAGE.get()

            pkg.header.id = id
            pkg.header.length = data.size

            pkg.data = data

            return pkg
        }
    }
}