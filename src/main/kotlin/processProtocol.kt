package org.example

import java.io.File

data class PackageInfo(
    val name: String? = null,
    val list: MutableList<String> = mutableListOf(),
)

fun main(args: Array<String>) {
    val fileDir = "proto"

    val data = mutableListOf<PackageInfo>()
    val nameSet = mutableSetOf<String>()

    val fileList = listOf("player", "friend")

    for (fileName in fileList) {
        val file = File("$fileDir/$fileName.proto")
        val info = PackageInfo(list = mutableListOf())
    }
}