package com.elfmcys.yesstevemodel.util.obfuscate

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.BINARY)
annotation class Keep