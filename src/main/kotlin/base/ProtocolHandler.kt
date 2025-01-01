package org.example.base

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtocolHandler(val handler: Int)
