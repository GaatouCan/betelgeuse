package org.example.base.net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import org.example.base.net.Package.Companion.PACKAGE_MAGIC
import org.example.base.net.Package.Companion.PACKAGE_VERSION


//fun ByteBuf.writeUInt(value: UInt) {
//    this.writeInt(value.toInt())
//}
//
//fun ByteBuf.writeUIntLE(value: UInt) {
//    this.writeIntLE(value.toInt())
//}
//
//fun ByteBuf.writeULong(value: ULong) {
//    this.writeLong(value.toLong())
//}
//
//fun ByteBuf.writeULongLE(value: ULong) {
//    this.writeLongLE(value.toLong())
//}
//
//fun ByteBuf.writeUShort(value: UShort) {
//    this.writeShort(value.toInt())
//}
//
//fun ByteBuf.writeUShortLE(value: UShort) {
//    this.writeShortLE(value.toInt())
//}


class PackageCodec : ByteToMessageCodec<Package>() {
    override fun encode(ctx: ChannelHandlerContext?, pkg: Package?, buf: ByteBuf?) {
        if (pkg == null || buf == null) return

        // 写入数据包头部数据
        buf.writeIntLE(pkg.header.magic)
        buf.writeIntLE(pkg.header.version)

        buf.writeShortLE(pkg.header.method)
        buf.writeShort(0)

        buf.writeIntLE(pkg.header.id)
        buf.writeLongLE(pkg.header.length.toLong())

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
        val magic = buf.readUnsignedIntLE().toInt()
        val version = buf.readUnsignedIntLE().toInt()

        val method = buf.readUnsignedShortLE()
        buf.readShort()

        val id = buf.readUnsignedIntLE().toInt()
        val length = buf.readLongLE().toInt()

        // 魔数校验和版本号必须一致
        if (magic != PACKAGE_MAGIC || version != PACKAGE_VERSION) return

        // 剩余长度不足
        if (length < 0 || buf.readableBytes() < length) return

        // 读取数据包字节流
        val data = ByteArray(length)

        // 允许数据包数据部分为空
        if (length > 0)
            buf.readBytes(data)

        val header = Package.Header(magic, version, method, id, length)
        val pkg = Package(header, data)

        output.add(pkg)
    }
}