package org.example.base.config

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogicConfig(val path: String)
