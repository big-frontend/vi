package com.electrolytej.vi

import pink.madis.apk.arsc.PackageChunk
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.StringPoolChunk
import java.io.FileInputStream
import java.util.zip.ZipEntry

fun Int.getResourceTypeId(): Int {
    val resourceId = this
    return resourceId and 0x00FF0000 shr 16
}

fun Int.getResourceEntryId(): Int {
    val resourceId = this
    return resourceId and 0x0000FFFF
}

fun Int.getPackageId(): Int {
    val resourceId = this
    return resourceId and -0x1000000 shr 24
}

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

fun ZipEntry.entryToResource(): Triple<String, String, String> {
    val entryName = this.name
    return entryName.entryToResource()
}

fun String.entryToResource(): Triple<String, String, String> {
    val entry = this
    if (entry.isNotEmpty()) {
        val restype = entry.entryToResType()
        val resName = entry.entryToResName()
        if (restype.isNotEmpty() && resName.isNotEmpty()) {
            return Triple("R.$restype.$resName", restype, resName)
        }
    }
    return Triple("", "", "")
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
fun SymbolList.obfuscatedResId(
    filter: (SymbolList.IntSymbol) -> Boolean,
    each: (SymbolList.IntSymbol, String) -> Unit
) {
    // Prepare proguard resource name
    val mapOfResTypeName = HashMap<String, HashSet<SymbolList.IntSymbol>>()
    filterIsInstance<SymbolList.IntSymbol>().forEach {
        if (!mapOfResTypeName.containsKey(it.type)) {
            mapOfResTypeName[it.type] = HashSet()
        }
        mapOfResTypeName[it.type]?.add(it)
    }
    for (resType in mapOfResTypeName.keys) {
        val resTypeBuilder = ProguardStringBuilder()
        mapOfResTypeName[resType]?.forEach { sym ->
            if (filter(sym)) {
                each(sym, resTypeBuilder.generateNextProguard())
            }
        }
    }
}
