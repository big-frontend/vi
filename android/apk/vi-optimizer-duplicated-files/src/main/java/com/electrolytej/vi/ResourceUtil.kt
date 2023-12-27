package com.electrolytej.vi

import java.util.zip.ZipEntry

fun Collection<ZipEntry>.isSameResourceType(): Boolean {
    var resType = ""
    val it = this.iterator()
    while (it.hasNext()) {
        val entry = it.next()
        if (entry.name.isNotEmpty()) {
            if (resType.isEmpty()) {
                resType = entry.name.entryToResType()
                continue
            }
            if (resType != entry.name.entryToResType()) {
                return false
            }
        } else {
            return false
        }
    }
    return resType.isNotEmpty()
}
fun ZipEntry.entryToResource(): Triple<String,String,String> {
    val entryName = this.name
    return entryName.entryToResource()
}
fun String.entryToResource(): Triple<String,String,String> {
    val entry = this
    if (entry.isNotEmpty()) {
        val restype = entry.entryToResType()
        val resName = entry.entryToResName()
        if (restype.isNotEmpty() && resName.isNotEmpty()) {
            return Triple("R.$restype.$resName",restype,resName)
        }
    }
    return Triple("","","")
}
fun String.entryToResName() = substring(lastIndexOf('/') + 1, indexOf('.'))
fun String.entryToResType(): String {
    val entry = this
    if (entry.isNotEmpty() && entry.length > 4) {
        var typeName = entry.substring(4, entry.lastIndexOf('/'))
        if (typeName.isNotEmpty()) {
            val index = typeName.indexOf('-')
            if (index >= 0) {
                typeName = typeName.substring(0, index)
            }
            return typeName
        }
    }
    return ""
}

