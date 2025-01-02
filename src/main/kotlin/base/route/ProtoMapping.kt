package org.example.base.route

import org.example.controller.ProtocolType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtoMapping(val type: ProtocolType)
