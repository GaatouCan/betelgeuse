package org.example.player

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlayerComponent(val name: String = "")
