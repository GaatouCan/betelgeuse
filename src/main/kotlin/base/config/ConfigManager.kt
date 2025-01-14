package org.example.base.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import java.io.File
import kotlin.reflect.full.findAnnotation
import kotlin.system.exitProcess

object ConfigManager {
    private val logger = LogManager.getLogger(ConfigManager::class.java)

    private var globalConfig: ServerConfig? = null
    private val configMap = hashMapOf<String, HashMap<Int, NormalConfig>>()

    init {
        val yamlMapper = ObjectMapper(YAMLFactory()).apply {
            registerModule(
                KotlinModule.Builder().build()
            )
        }
        val url = this::class.java.getResource("/config.yaml")
        if (url != null) {
            val file = File(url.toURI())
            if (file.exists()) {
                globalConfig = yamlMapper.readValue(file)
                logger.info("Server Config Loaded.")
            }
        }

        val jsonMapper = ObjectMapper().apply {
            KotlinModule.Builder()
                .configure(KotlinFeature.NullToEmptyCollection, true)
                .configure(KotlinFeature.NullToEmptyMap, true)
                .build()
        }

        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example.config").addScanners(SubTypesScanner(false)))
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
                            val config = loadJsonFile(jsonMapper, file, clazz)
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

    private fun<T : NormalConfig> loadJsonFile(mapper: ObjectMapper, file: File, type : KClass<T>) : HashMap<Int, T> {
        val result = hashMapOf<Int, T>()
        val root = mapper.readTree(file)
        val fields = root.fields()
        while (fields.hasNext()) {
            val field = fields.next()
            val key = field.key as String
            val node = field.value

            val config = mapper.treeToValue(node, type.java)
            if (result.containsKey(key.toInt()))
                throw IndexOutOfBoundsException("Key $key is out of define. file ${file.absolutePath}")

            result[key.toInt()] = config
        }
        return result
    }
}