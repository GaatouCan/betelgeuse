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
import java.io.File
import kotlin.reflect.KClass

object ConfigManager {
    private val logger = LogManager.getLogger(ConfigManager::class.java)

    private var globalConfig: ServerConfig? = null
    private var configMap: HashMap<String, HashMap<Int, LogicConfig>> = hashMapOf()

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
        val classes = reflections.getSubTypesOf(LogicConfig::class.java)

        classes.forEach { clazz ->
            if (clazz.isAnnotationPresent(ConfigPath::class.java)) {
                try {
                    val annotation = clazz.getAnnotation(ConfigPath::class.java)
                    if (annotation != null) {
                        val url = this::class.java.getResource("/config/${annotation.path}.json")
                        if (url != null) {
                            val file = File(url.toURI())
                            if (file.exists()) {
                                val config = loadJsonFile(jsonMapper, file, clazz.kotlin)
//                                config.forEach { key, value ->
//                                    if (value is AvatarConfig) {
//                                        println("$key -> $value")
//                                    }
//                                }
                                configMap[annotation.path] = config as HashMap<Int, LogicConfig>
                                logger.info("Load Config - ${annotation.path}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getGlobalConfig(): ServerConfig? {
        return globalConfig
    }

    fun find(path: String, id: Int): LogicConfig? {
        val it = configMap[path]?: return null
        return it[id]
    }

    private fun<T : LogicConfig> loadJsonFile(mapper: ObjectMapper, file: File, type : KClass<T>) : HashMap<Int, T> {
        val result = hashMapOf<Int, T>()
        val root = mapper.readTree(file)
        val fields = root.fields()
        while (fields.hasNext()) {
            val field = fields.next()
            val key = field.key as String
            val node = field.value

            val config = mapper.treeToValue(node, type.java)
            result[key.toInt()] = config
        }
        return result
    }
}