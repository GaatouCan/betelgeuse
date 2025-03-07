package org.example.base.config

import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.system.exitProcess

object ConfigManager {
    private val logger = LogManager.getLogger(ConfigManager::class.java)

    private var globalConfig: ServerConfig? = null
    private val configMap = hashMapOf<String, HashMap<Int, NormalConfig>>()

    init {
        val yaml = Yaml()
        val input = this::class.java.getResourceAsStream("/config.yaml")
        if (input != null) {
            globalConfig = yaml.load(input)
        }

        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example.config").addScanners(Scanners.SubTypes))
        val classes = reflections.getSubTypesOf(NormalConfig::class.java).map { it.kotlin }

        classes.forEach { clazz ->
            val annotation = clazz.findAnnotation<ConfigPath>()
            if (annotation != null) {
                try {
                    val path = annotation.path.replace(".", "/")
                    val configUrl = this::class.java.getResource("/config/json/$path.json")
                    if (configUrl != null) {
                        val file = File(configUrl.toURI())
                        if (file.exists()) {
                            val config = loadJsonFile(file, clazz)
//                                config.forEach { key, value ->
//                                    if (value is AvatarConfig) {
//                                        println("$key -> $value")
//                                    }
//                                }
                            configMap[annotation.path] = config as HashMap<Int, NormalConfig>
                            logger.info("Load Config - ${annotation.path}")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(2)
                }
            }
        }
    }

    fun getGlobalConfig(): ServerConfig? {
        return globalConfig
    }

    fun find(path: String, id: Int): NormalConfig? {
        val it = configMap[path]?: return null
        return it[id]
    }

    private fun<T : NormalConfig> loadJsonFile(file: File, type : KClass<T>) : HashMap<Int, T> {
        val str = file.readText(Charsets.UTF_8);
        val preResult = Json.decodeFromString<HashMap<String, T>>(str)
        val result = preResult.mapKeys { it.key.toInt() } as HashMap<Int, T>
        return result
    }
}