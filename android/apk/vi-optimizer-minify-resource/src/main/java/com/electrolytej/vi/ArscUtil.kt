package com.electrolytej.vi

import pink.madis.apk.arsc.PackageChunk
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.ResourceValue
import pink.madis.apk.arsc.StringPoolChunk
import pink.madis.apk.arsc.TypeChunk
import pink.madis.apk.arsc.TypeSpecChunk
import java.io.File
import java.io.PrintWriter

fun StringPoolChunk.setString(index: Int, s: String) {
    val stringsField = StringPoolChunk::class.java.getDeclaredField("strings")
    stringsField.isAccessible = true
    val stringsList = stringsField.get(this) as? java.util.ArrayList<String>
    stringsList?.set(index, s)
}
fun StringPoolChunk.getStrings():ArrayList<String>?{
    try{
        val field = javaClass.getDeclaredField("strings")
        field.setAccessible(true)
        return field.get(this) as ArrayList<String>
    }catch (e:Exception){
        e.printStackTrace()
    }

    return null

}
fun StringPoolChunk.getStyles():MutableList<Any>?{
    try{
        val field = javaClass.getDeclaredField("styles")
        field.setAccessible(true)
        return field.get(this) as MutableList<Any>
    }catch (e:Exception){
        e.printStackTrace()
    }

    return null

}

fun TypeSpecChunk.getResources():IntArray?{
    try{
        val field = javaClass.getDeclaredField("resources")
        field.setAccessible(true)
        return field.get(this) as IntArray
    }catch (e:Exception){
        e.printStackTrace()
    }

    return null
}


fun TypeChunk.getMutableEntities():MutableMap<Int, TypeChunk.Entry>?{
    try{
        val field = javaClass.getDeclaredField("entries")
        field.setAccessible(true)
        return field.get(this) as MutableMap<Int, TypeChunk.Entry>
    }catch (e:Exception){
        e.printStackTrace()
    }

    return null
}

fun ResourceValue.setData(data: Int){
    try{
        val field = javaClass.getDeclaredField("data")
        field.setAccessible(true)
        field.setInt(this,data)
    }catch (e:Exception){
        e.printStackTrace()
    }
}

fun ResourceTableChunk.removeString(globalString:String,fileLogger : PrintWriter? = null){

    val stringPoolChunk = stringPool
    /**
     * These styles have a 1:1 relationship with the strings. For example, styles.get(3) refers to
     * the string at location strings.get(3). There are never more styles than strings (though there
     * may be less). Inside of that are all of the styles referenced by that string.
     */
    val strings = stringPoolChunk.getStrings()
    val styles = stringPoolChunk.getStyles()

    var deleteIndex = 0
    strings!!.apply {
        val iterator = this.listIterator() as MutableListIterator

        loop0@ while(iterator.hasNext()){
            val value = iterator.next()
            if(value == globalString){
                iterator.remove()
                break@loop0
            }

            if(styles != null && deleteIndex < styles.size){
                styles.removeAt(deleteIndex)
            }

            deleteIndex++

        }

        //删除全局 StringChunk的元素，后续的元素索引减一，需要改变对应的TypeChunk.Entry.ResourceValue string类型 data（对应全局StringChunk的索引）
        packages.forEach {packageChunk ->
            packageChunk.typeChunks.forEach { typeChunk ->
                typeChunk.getMutableEntities()!!.forEach{ mapEntry ->
                    var entry = mapEntry.value
                    if(entry.typeName() == "string"){
                        entry.value()?.apply {
                            if(data() > deleteIndex){
                                setData(data() - 1 )
                                println("reset TypeChunk entry resourceValue data ${this}")
                                fileLogger?.println("reset TypeChunk entry resourceValue data ${this}")
                            }

                        }

                        entry.values()?.forEach{ resourceValueEntry ->
                            resourceValueEntry.value?.apply {
                                if(type() == ResourceValue.Type.STRING){
                                    if(data() > deleteIndex){
                                        setData(data() - 1 )
                                        println("reset TypeChunk entry resourceValues data ${this}")
                                        fileLogger?.println("reset TypeChunk entry resourceValues data ${this}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
fun ResourceFile.replaceResource(
    srcName: String,
    destName: String
): Boolean {
    chunks
        .filterIsInstance<ResourceTableChunk>()
        .forEach { chunk ->
            val stringPoolChunk = chunk.stringPool
            val index = stringPoolChunk.indexOf(srcName)
            if (index != -1) {
                stringPoolChunk.setString(index, destName)
                return@replaceResource true
            }
        }
    return false
}
