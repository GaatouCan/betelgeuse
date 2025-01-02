package org.example.base.net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec

class PackageCodec : ByteToMessageCodec<Package>() {
    override fun encode(ctx: ChannelHandlerContext?, pkg: Package?, buf: ByteBuf?) {
        if (pkg == null || buf == null) return

        // 写入数据包头部数据
        buf.writeInt(pkg.header.magic)
        buf.writeInt(pkg.header.version)

        buf.writeShort(pkg.header.method.value.toInt())
        buf.writeShort(0) // reverse

        buf.writeInt(pkg.header.id)
        buf.writeInt(pkg.header.length)

        buf.writeInt(0) // reverse

        // 写入数据包字节流数据
        buf.writeBytes(pkg.data)
    }

    override fun decode(
        ctx: ChannelHandlerContext?,
        buf: ByteBuf?,
        output: MutableList<Any?>
    ) {
        if (buf == null) return

        // 头部大小24字节
        if (buf.readableBytes() < 24) return

        // 读取头部数据
        val magic = buf.readInt()
        val version = buf.readInt()

        val method = buf.readShort()
        buf.readShort() // reverse

        val id = buf.readInt()
        val length = buf.readInt()
        buf.readInt() // reverse

        // 魔数校验和版本号必须一致
        if (magic != PACKAGE_MAGIC || version != PACKAGE_VERSION) return

        // 剩余长度不足
        if (length < 0 || buf.readableBytes() < length) return

        // 读取数据包字节流
        val data = ByteArray(length)

        // 允许数据包数据部分为空
        if (length > 0)
            buf.readBytes(data)

        val header = Package.CodecMethod.fromValue(method)?.let {
            Package.Header(
                magic, version, it, id, length
            )
        } ?: return

        val pkg = Package(header, data)
        output.add(pkg)
    }
}