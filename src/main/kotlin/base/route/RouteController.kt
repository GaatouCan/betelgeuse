package org.example.base.route

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteController(val route: String)
