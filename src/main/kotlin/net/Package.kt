package org.example.net

const val PACKAGE_MAGIC = 20241231
const val PACKAGE_VERSION = 1001

enum class PackageMethod(val value: Short) {
    LINE_BASED(0),
    PROTOBUF(1);

    companion object {
        fun fromValue(value: Short): PackageMethod? {
            return PackageMethod.entries.find { it.value == value }
        }
    }
}

data class PackageHeader(
    val magic: Int,
    val version: Int,

    var method: PackageMethod,

    var id: Int,
    var length: Int
)

data class Package(val header: PackageHeader, val data: ByteArray) {

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
        fun createPackage(id: Int, data: ByteArray): Package {
            val header = PackageHeader(PACKAGE_MAGIC, PACKAGE_VERSION, PackageMethod.PROTOBUF, id, data.size)
            return Package(header, data)
        }
    }
}