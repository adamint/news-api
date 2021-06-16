package com.adamratzman.utils

fun String.clean(): String = this.replace("\n ", "")
    .replace(" \n", "").replace("\n", "").trim()
