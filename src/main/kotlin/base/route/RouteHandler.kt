package org.example.base.route

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteHandler(val handler: Int)
