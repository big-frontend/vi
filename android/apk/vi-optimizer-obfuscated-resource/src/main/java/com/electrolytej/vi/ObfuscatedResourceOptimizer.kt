package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
import com.google.auto.service.AutoService
import pink.madis.apk.arsc.PackageChunk
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.StringPoolChunk
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
@AutoService(BaseOptimizer::class)
class ObfuscatedResourceOptimizer : BaseOptimizer {
    companion object {
        private const val OBFUSCATED_PROPERTY_IGNORES = "vi.optimizer.obfuscated.resource.ignores"
        private const val RES_DIR_PROGUARD_NAME = "r"
    }

    private val obfuscatedIgnores: Set<Wildcard> = mutableSetOf()
    val mapOfObfuscatedResource = mutableMapOf<String, Pair<SymbolList.IntSymbol, String>>()
    private val mappings = mutableMapOf<String, String>()

    private  var variant: BaseVariant? = null
    private lateinit var symbols: SymbolList
    private lateinit var logger: L
    override fun start(variant: BaseVariant?, symbols: SymbolList, ap_: File) {
        this.symbols = symbols
        this.logger = L.create(variant,"vi-optimizer-obfuscated-resource")
        if (variant != null) {
            this.variant = variant
//            this.obfuscatedIgnores =
//                variant.project.getProperty(OBFUSCATED_PROPERTY_IGNORES, "").trim().split(',')
//                    .filter(String::isNotEmpty).map(Wildcard.Companion::valueOf).toSet()
        }
        logger.println("$OBFUSCATED_PROPERTY_IGNORES=$obfuscatedIgnores\n")
        symbols.obfuscatedResId(
            filter = { sym ->
                val redId = "R.${sym.type}.${sym.name}"
                val ign = false
//                val ign = obfuscatedIgnores.any { it.matches(redId) }
                if (ign) {
                    logger.println("Ignore proguard resource $redId")
                }
                !ign
            },
            each = { sym, newName ->
//                val redId = "R.${sym.type}.${sym.name}"
                val resPath = "res/${sym.type}/${sym.name}"
                mapOfObfuscatedResource[resPath] = Pair(sym, newName)
            })

    }

    override fun processArsc(resourceFile: ResourceFile): Boolean {
//        if (mapOfObfuscatedResource.isEmpty()) return false
        //proguard resource name
//        val resIdProguard = HashMap<Int, String>()
//        val iterator = mapOfObfuscatedResource.keys.iterator()
//        while (iterator.hasNext()) {
//            val resId = iterator.next()
//            val (sym, newName) = mapOfObfuscatedResource[resId] ?: continue
//            resIdProguard[sym.value] = newName
//        }
//        if (resIdProguard.isEmpty()) return false
        resourceFile.chunks.filterIsInstance<ResourceTableChunk>().forEach { chunk ->
            val stringPoolChunk = chunk.stringPool
            // 获取所有的路径
            val strings = stringPoolChunk.getStrings() ?: return@forEach
            for (index in 0 until stringPoolChunk.stringCount) {
                val v = strings[index]
                if (v.startsWith("res")) {
                    val ign = obfuscatedIgnores.any { it.matches(v) }
                    if (ign) {
                        logger.println("Ignore proguard resource $v")
                        continue
                    }
                    var newPath = mappings[v]
                    if (newPath == null) {
                        newPath = createProcessPath(v)
                        mappings[v] = newPath
                    }
                    strings[index] = newPath
                }
            }


            val str2 = mappings.map {
                val startIndex = it.key.lastIndexOf("/") + 1
                var endIndex = it.key.lastIndexOf(".")
                if (endIndex < 0) {
                    endIndex = it.key.length
                }
                if (endIndex < startIndex) {
                    it.key to it.value
                } else {
//                            val vStartIndex = it.value.lastIndexOf("/") + 1
//                            var vEndIndex = it.value.lastIndexOf(".")
//                            if (vEndIndex < 0) {
//                                vEndIndex = it.value.length
//                            }
//                            val result = it.value.substring(vStartIndex, vEndIndex)
                    // 使用相同的字符串，以减小体积
                    it.key.substring(startIndex, endIndex) to "du"
                }
            }.toMap()
//             修改 arsc PackageChunk 字段
            chunk.chunks.values.filterIsInstance<PackageChunk>()
                .flatMap { it.chunks.values }.filterIsInstance<StringPoolChunk>()
                .forEach {
                    for (index in 0 until it.stringCount) {
                        it.getStrings()?.forEachIndexed { index, s ->
                            str2[s]?.let { result ->
                                it.setString(index, result)
                            }
                        }
                    }
                }
            //将mapping映射成指定格式文件，供给反混淆服务使用
            val mMappingWriter: Writer = BufferedWriter(
                FileWriter(
                    variant?.mappingFileProvider?.get()?.singleFile?.parentFile,
                    false
                )
            )
            val packageName = "hi"
            val pathMappings = mutableMapOf<String, String>()
            val idMappings = mutableMapOf<String, String>()
            mappings.filter { (t, u) -> t != u }.forEach { (t, u) ->
//                result?.add(" $t => $u")
//                compress[t]?.let {
//                    compress[u] = it
//                    compress.remove(t)
//                }
                val pathKey = t.substring(0, t.lastIndexOf("/"))
                pathMappings[pathKey] = u.substring(0, u.lastIndexOf("/"))
                val typename = t.split("/")[1].split("-")[0]
                val path1 = t.substring(t.lastIndexOf("/") + 1, t.indexOf("."))
                val path2 = u.substring(u.lastIndexOf("/") + 1, u.indexOf("."))
                val path = "$packageName.R.$typename.$path1"
                val pathV = "$packageName.R.$typename.$path2"
                if (idMappings[path].isNullOrEmpty()) {
                    idMappings[path] = pathV
                }
            }
            generalFileResMapping(mMappingWriter, pathMappings)
            generalResIDMapping(mMappingWriter, idMappings)

        }
        return false
    }

    private fun generalFileResMapping(
        mMappingWriter: Writer,
        pathMappings: MutableMap<String, String>
    ) {

    }

    private fun generalResIDMapping(
        mMappingWriter: Writer,
        idMappings: MutableMap<String, String>
    ) {

    }

    fun createProcessPath(v: String): String {
        val resTypeBuilder = ProguardStringBuilder()
        return "res/${v.entryToResType()}/${resTypeBuilder.generateNextProguard()}"
    }

    override fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean {
        val ign = obfuscatedIgnores.any { it.matches(zipEntry.name) }
        var newPath = ""
        if (ign) {
            newPath = zipEntry.name
//            val newPath = v.replaceFirst("res", whiteTempRes)
//            keeps.add()
//            File("$destDir${File.separator}$v").renameTo(File("$destDir${File.separator}$newPath"))
        } else {
            newPath = mappings[zipEntry.name].toString()
        }
        val destFile = File(destDir, newPath.replace('/', File.separatorChar))
        if (zipEntry.isDirectory) {
            destFile.createOrExistsDir()
        } else {
            destFile.createOrExistsFile()
            srcFile.extractEntry(destFile, zipEntry)
        }
//        if (canObfuscateArsc) {
//            if (mapOfResources.containsKey(resourceName)) {
//                val dir = zipEntry.name.substring(0, zipEntry.name.lastIndexOf("/"))
//                val suffix = zipEntry.name.substring(zipEntry.name.indexOf("."))
//                Log.d(MinifyTask.TAG, "resource %s dir %s", resourceName, dir)
//                if (!resultOfObfuscatedDirs.containsKey(dir)) {
//                    val proguardDir = dirProguard.generateNextProguardFileName()
//                    resultOfObfuscatedDirs[dir] = "${RES_DIR_PROGUARD_NAME}/$proguardDir"
//                    dirFileProguard[dir] = ProguardStringBuilder()
//                    Log.i(MinifyTask.TAG, "dir %s, proguard builder", dir)
//                }
//                resultOfObfuscatedResource[zipEntry.name] =
//                    resultOfObfuscatedDirs[dir] + "/" + dirFileProguard[dir]!!.generateNextProguardFileName() + suffix
//                val success = ArscUtil.replaceResFileName(
//                    resTable,
//                    mapOfResources[resourceName]!!,
//                    zipEntry.name,
//                    resultOfObfuscatedResource[zipEntry.name]
//                )
//                if (success) {
//                    destFile =
//                        unzipDir.canonicalPath + File.separator + resultOfObfuscatedResource[zipEntry.name]!!.replace(
//                            '/', File.separatorChar
//                        )
//                }
//            }
//        }
        return false
    }

    override fun end(ap_: File) {
        val s1 = ap_.length()
        //                logger.println("Delete obfuscated resource:")
//                logger.println("-".repeat(maxWidth))

        // 收尾删除 res2
//    FileOperation.deleteDir(File("$unZipDir${File.separator}$whiteTempRes"))
        logger.close()
    }
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
