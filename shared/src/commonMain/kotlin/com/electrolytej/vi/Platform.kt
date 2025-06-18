package com.electrolytej.vi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform