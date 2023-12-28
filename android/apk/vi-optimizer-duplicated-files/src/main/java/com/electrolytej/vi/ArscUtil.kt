package com.electrolytej.vi

import com.didiglobal.booster.kotlinx.get
import pink.madis.apk.arsc.PackageChunk
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.StringPoolChunk
import pink.madis.apk.arsc.TypeChunk
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.jvm.javaField

object ArscUtil {
    fun toUTF16String(buffer: ByteArray?): String {
        val charBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer()
        var index = 0
        while (index < charBuffer.length) {
            if (charBuffer.get().code == 0x00) {
                break
            }
            index++
        }
        charBuffer.limit(index).position(0)
        return charBuffer.toString()
    }

    fun findResPackage(resTable: ResourceTableChunk, packageId: Int): PackageChunk? {
        var resPackage: PackageChunk? = null
        for (pkg in resTable.packages) {
            if (pkg.id == packageId) {
                resPackage = pkg
                break
            }
        }
        return resPackage
    }

    fun findResType(packageChunk: PackageChunk, resourceId: Int): List<TypeChunk> {
        val typeId = resourceId and 0X00FF0000 shr 16
        val entryId = resourceId and 0x0000FFFF
        val resTypeList: MutableList<TypeChunk> = ArrayList()
        val resTypeArray = packageChunk.typeChunks
        if (resTypeArray != null) {
            for (typeChunk in resTypeArray) {
                if (typeChunk.id == typeId) {
                    val entryCount = typeChunk.totalEntryCount
                    if (entryId < entryCount) {
                        resTypeList.add(typeChunk)
                    }
                }
            }
        }
        return resTypeList
    }

    @Throws(IOException::class)
    fun removeResource(resTable: ResourceTableChunk, resourceId: Int, resourceName: String?) {
//        val resPackage = findResPackage(resTable, getPackageId(resourceId))
//        if (resPackage != null) {
//            val resTypeList = findResType(resPackage, resourceId)
//            var resNameStringPoolIndex = -1
//            for (resType in resTypeList) {
//                val entryId = getResourceEntryId(resourceId)
//                resNameStringPoolIndex = resType.entries[entryId]!!.keyIndex()
//                resType.entries.remove(entryId)
//                //                resType.refresh();
//            }
//            if (resNameStringPoolIndex != -1) {
////                resPackage.getTypeStringPool()
////                Log.i(TAG, "try to remove %s (%H), find resource %s", resourceName, resourceId, ResStringBlock.resolveStringPoolEntry(resPackage.getResNamePool().getStrings().get(resNameStringPoolIndex).array(), resPackage.getResNamePool().getCharSet()));
//            }
//            //            resPackage.shrinkResNameStringPool();
////            resPackage.refresh();
////            resTable.refresh();
//        }
    }
}
