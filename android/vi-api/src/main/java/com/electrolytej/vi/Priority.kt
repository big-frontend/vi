package com.electrolytej.vi

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Priority(val value: Int = 0)