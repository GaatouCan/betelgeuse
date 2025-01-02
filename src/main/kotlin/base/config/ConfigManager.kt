package org.example.base.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import java.io.File

object ConfigManager {
    private var globalConfig: ServerConfig? = null
    private val logger = LogManager.getLogger(ConfigManager::class.java)

    init {
        val mapper = ObjectMapper(YAMLFactory()).apply {
            registerModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.NullToEmptyCollection, true) // 允许将 null 转为空集合
                    .configure(KotlinFeature.NullToEmptyMap, true)       // 允许将 null 转为空 Map
                    .build()
            )
        }
        val url = this::class.java.getResource("/config.yaml")
        if (url != null) {
            val file = File(url.toURI())
            if (file.exists()) {
                globalConfig = mapper.readValue(file)
                logger.info("Server Config Loaded.")
            }
        }
    }

    fun getGlobalConfig(): ServerConfig? {
        return globalConfig
    }
}