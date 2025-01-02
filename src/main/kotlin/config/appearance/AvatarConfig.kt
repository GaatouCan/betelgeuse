package org.example.config.appearance

import org.example.base.config.ConfigPath
import org.example.base.config.LogicConfig

@ConfigPath("appearance/avatar")
data class AvatarConfig(
    val id: Int = 0,
    val name: String = "",
    val rare: Int = 0
) : LogicConfig
