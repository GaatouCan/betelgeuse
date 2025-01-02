package org.example.net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec

class PackageCodec : ByteToMessageCodec<Package>() {
    override fun encode(ctx: ChannelHandlerContext?, pkg: Package?, buf: ByteBuf?) {
        if (pkg == null || buf == null) return

        // Write Header
        buf.writeInt(pkg.header.magic)
        buf.writeInt(pkg.header.version)

        buf.writeShort(pkg.header.method.value.toInt())
        buf.writeShort(0) // reverse
        buf.writeInt(0) // reverse

        buf.writeInt(pkg.header.id)
        buf.writeInt(pkg.header.length)

        // Write Data Bytes
        buf.writeBytes(pkg.data)
    }

    override fun decode(
        ctx: ChannelHandlerContext?,
        buf: ByteBuf?,
        output: MutableList<Any?>
    ) {
        if (buf == null) return

        if (buf.readableBytes() < 24) return

        // Read Header
        val magic = buf.readInt()
        val version = buf.readInt()

        val method = buf.readShort()
        buf.readShort()
        buf.readInt()

        val id = buf.readInt()
        val length = buf.readInt()

        if (magic != PACKAGE_MAGIC || version != PACKAGE_VERSION) return

        if (length <= 0 || buf.readableBytes() < length) return

        // Read Bytes Data
        val data = ByteArray(length)
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