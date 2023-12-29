package com.electrolytej.vi

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.getReport
import com.didiglobal.booster.gradle.packageTaskProvider
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import pink.madis.apk.arsc.PackageChunk
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.StringPoolChunk
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


private val logger_ = Logging.getLogger(MinifyResourceVariantProcessor::class.java)

@AutoService(VariantProcessor::class)
class MinifyResourceVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {

        val removeDuplicatedFiles = variant.project.tasks.register(
            "remove${variant.name.capitalize()}DuplicatedFiles", RemoveDuplicatedFiles::class.java
        ) {
            it.group = "booster"
            it.description = "remove duplicated files for ${variant.name}"
            it.variant = variant
        }
        variant.processResTaskProvider?.let { processRes ->
            removeDuplicatedFiles.dependsOn(processRes)
            processRes.configure {
                it.finalizedBy(removeDuplicatedFiles)
            }
        }
        val removeUnusedFiles = variant.project.tasks.register(
            "remove${variant.name.capitalize()}UnusedFiles", RemoveUnusedFiles::class.java
        ) {
            it.group = "booster"
            it.description = "remove unused files for ${variant.name}"
            it.variant = variant
        }
        variant.packageTaskProvider?.let { packageApk ->
            packageApk.configure {
                it.doFirst {
                }
            }
        }
    }
}

internal abstract class RemoveUnusedFiles : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant
    private val UNUSED_PROPERTY_IGNORES = "vi.optimizer.unused.files.ignores"
}
internal abstract class RemoveDuplicatedFiles : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant
    private lateinit var symbols: SymbolList
    private lateinit var logger: PrintWriter
    private val DUPLICATED_PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"

    private val OBFUSCATED_PROPERTY_IGNORES = "vi.optimizer.obfuscated.files.ignores"
    private lateinit var duplicatedIgnores: Set<Wildcard>
    private lateinit var obfuscatedIgnores: Set<Wildcard>

    @TaskAction
    fun remove() {
        this.symbols = SymbolList.from(variant.symbolList.single())
        if (this.symbols.isEmpty()) {
            logger_.error("remove duplicated files failed: R.txt doesn't exist or blank")
            return
        }

        this.duplicatedIgnores = project.getProperty(DUPLICATED_PROPERTY_IGNORES, "").trim().split(',')
            .filter(String::isNotEmpty)
            .map(Wildcard.Companion::valueOf).toSet()
        this.obfuscatedIgnores = project.getProperty(OBFUSCATED_PROPERTY_IGNORES, "").trim().split(',')
            .filter(String::isNotEmpty)
            .map(Wildcard.Companion::valueOf).toSet()
        this.logger = variant.getReport("vi-optimizer-minify-resource", "report.txt").touch().printWriter()
        logger.use {

            logger.println("$DUPLICATED_PROPERTY_IGNORES=$duplicatedIgnores\n")
            logger.println("$OBFUSCATED_PROPERTY_IGNORES=$duplicatedIgnores\n")
            val files = variant.processedRes.search {
                it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
            }
            files.parallelStream().forEach { ap_ ->
                //    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)
                //1.find duplicated files from ap file
                val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()

                ZipFile(ap_).use {
                    it.findDuplicatedFiles(
                        filter = { entry ->
                            val ign = duplicatedIgnores.any { it.matches(entry.name) }
                            if (ign) {
                                logger.println("Ignore `${entry.name}`")
                            }
                            entry.name.startsWith("res/") && !ign
                        },
                        each = { dup, replace ->
                            mapOfDuplicatesReplacements[dup.name] =
                                Triple(dup.crc, dup.size, replace.name)
                        })
                }
                val mapOfObfuscatedResource = mutableMapOf<String, Pair<SymbolList.IntSymbol, String>>()
                symbols.obfuscatedResId(filter = { sym ->
                    val redId = "R.${sym.type}.${sym.name}"
                    val ign = obfuscatedIgnores.any { it.matches(redId) }
                    if (ign) {
                        logger.println("Ignore proguard resource $redId")
                    }
                    !ign
                }, each = { sym, newName ->
                    val redId = "R.${sym.type}.${sym.name}"
                    mapOfObfuscatedResource[redId] = Pair(sym,newName)
                })

                val maxWidth = mapOfDuplicatesReplacements.map { it.key.length }.maxOrNull()?.plus(10) ?: 10
                val s0 = ap_.length()
                //2.remove duplicated files  and repack ap file
                val total = ap_.minify(symbols, mapOfDuplicatesReplacements,mapOfObfuscatedResource)
                val s1 = ap_.length()


                logger.println("Delete duplicated files:")
                mapOfDuplicatesReplacements.forEach { dup, (crc32, size, replace) ->
                    val (srcResId, srcResType, srcResName) = dup.entryToResource()
                    val srcResIdInt = symbols.getInt(srcResType, srcResName)
                    val (destResId, destResType, destResName) = replace.entryToResource()
                    val destResIdInt = symbols.getInt(destResType, destResName)
                    logger.println(" * replace 0x${srcResIdInt.toString(16)}($dup) with 0x${destResIdInt.toString(16)}($replace)\t${size}bytes crc32/$crc32")
                }
                logger.println("-".repeat(maxWidth))
                logger.println("Total: $total bytes, ap length: ${s0 - s1} bytes")
//                logger.println("Delete obfuscated files:")
//                logger.println("-".repeat(maxWidth))
            }
        }
    }
}

const val ARSC_FILE_NAME = "resources.arsc"
const val RES_DIR_PROGUARD_NAME = "r"
fun File.minify(
    symbols: SymbolList, mapOfDuplicatesReplacements: MutableMap<String, Triple<Long, Long, String>>,
    mapOfObfuscatedResource: MutableMap<String, Pair<SymbolList.IntSymbol, String>>
): Long {
    val shrunkApFile = File(parent, "${name}_shrunk")
    shrunkApFile.deleteRecursivelyIfExists()
    shrunkApFile.mkdir()
    var total = 0L
    val compressedEntry = HashSet<String>()
    ZipFile(this).use { zipInputFile ->
        val arscFile = File(shrunkApFile, ARSC_FILE_NAME)
        zipInputFile.extractEntry(arscFile, ARSC_FILE_NAME)
        val destArscFile = File(shrunkApFile, "shrinked_${ARSC_FILE_NAME}")
        FileInputStream(arscFile).use { arscStream ->
            val resourceFile = ResourceFile.fromInputStream(arscStream)
            val rmDuplicated = mapOfDuplicatesReplacements.isNotEmpty()
            val canObfuscateArsc = mapOfObfuscatedResource.isNotEmpty()
            //remove duplicated resources
            if (rmDuplicated) {
                val replaceIterator = mapOfDuplicatesReplacements.keys.iterator()
                while (replaceIterator.hasNext()) {
                    val srcEntryName = replaceIterator.next()
                    val (srcResId, srcResType, srcResName) = srcEntryName.entryToResource()
                    val srcResIdInt = symbols.getInt(srcResType, srcResName)
                    val (crc32, size, destEntryName) = mapOfDuplicatesReplacements[srcEntryName] ?: continue
                    val (destResId, destResType, destResName) = destEntryName.entryToResource()
                    val destResIdInt = symbols.getInt(destResType, destResName)
                    val sourcePkgId = srcResIdInt.getPackageId()
                    val targetPkgId = destResIdInt.getPackageId()
                    val success = if (sourcePkgId != targetPkgId) {
                        System.out.printf("sourcePkgId %d != targetPkgId %d, quit replace!%n", sourcePkgId, targetPkgId)
                        false
                    }else{
                        resourceFile.replaceResource(srcEntryName,destEntryName)
                    }
                    if (!success) {
                        logger_.error("replace ${srcResId}($srcEntryName) with $destResId($destEntryName) failed!")
                        replaceIterator.remove()
                    } else {
                        total += size
                    }
                }
            }

            //proguard resource name
            if (canObfuscateArsc) {
                val resIdProguard = HashMap<Int, String>()
                val iterator = mapOfObfuscatedResource.keys.iterator()
                while (iterator.hasNext()) {
                    val resId = iterator.next()
                    val (sym, newName) = mapOfObfuscatedResource[resId] ?: continue
                    resIdProguard.put(sym.value,newName)
                }
                if (resIdProguard.isNotEmpty()) {
                    resourceFile.chunks
                        .filterIsInstance<ResourceTableChunk>()
                        .forEach { chunk ->
                            val stringPoolChunk = chunk.stringPool
                            // 获取所有的路径
                            val strings = stringPoolChunk.getStrings() ?: return@forEach
                            for (index in 0 until stringPoolChunk.stringCount) {
                                val v = strings[index]

                                if (v.startsWith("res")) {
                                    if (ignore(v, context.proguardResourcesExtension.whiteList)) {
                                        println("resProguard  ignore  $v ")
                                        // 把文件移到新的目录
                                        val newPath = v.replaceFirst("res", whiteTempRes)
                                        val parent = File("$unZipDir${File.separator}$newPath").parentFile
                                        if (!parent.exists()) {
                                            parent.mkdirs()
                                        }
                                        keeps.add(newPath)
                                        // 移动文件
                                        File("$unZipDir${File.separator}$v").renameTo(File("$unZipDir${File.separator}$newPath"))
                                        continue
                                    }
                                    // 判断是否有相同的
                                    val newPath = if (mappings[v] == null) {
                                        val newPath = createProcessPath(v, builder)
                                        // 创建路径
                                        val parent = File("$unZipDir${File.separator}$newPath").parentFile
                                        if (!parent.exists()) {
                                            parent.mkdirs()
                                        }
                                        // 移动文件
                                        val isOk =
                                            File("$unZipDir${File.separator}$v").renameTo(File("$unZipDir${File.separator}$newPath"))
                                        if (isOk) {
                                            mappings[v] = newPath
                                            newPath
                                        } else {
                                            mappings[v] = v
                                            v
                                        }
                                    } else {
                                        mappings[v]
                                    }
                                    strings[index] = newPath!!
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

                            // 修改 arsc PackageChunk 字段
                            chunk.chunks.values.filterIsInstance<PackageChunk>()
                                .flatMap { it.chunks.values }
                                .filterIsInstance<StringPoolChunk>()
                                .forEach {
                                    for (index in 0 until it.stringCount) {
                                        it.getStrings()?.forEachIndexed { index, s ->
                                            str2[s]?.let { result ->
                                                it.setString(index, result)
                                            }
                                        }
                                    }
                                }

                            // 将 mapping 映射成 指定格式文件，供给反混淆服务使用
                            val mMappingWriter: Writer = BufferedWriter(FileWriter(file, false))
                            val packageName = context.proguardResourcesExtension.packageName
                            val pathMappings = mutableMapOf<String, String>()
                            val idMappings = mutableMapOf<String, String>()
                            mappings.filter { (t, u) -> t != u }.forEach { (t, u) ->
                                result?.add(" $t => $u")
                                compress[t]?.let {
                                    compress[u] = it
                                    compress.remove(t)
                                }
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
                    // 删除res下的文件
                    FileOperation.deleteDir(File("$unZipDir${File.separator}res"))
                    // 将白名单的文件移回res
                    keeps.forEach {
                        val newPath = it.replaceFirst(whiteTempRes, "res")
                        val parent = File("$unZipDir${File.separator}$newPath").parentFile
                        if (!parent.exists()) {
                            parent.mkdirs()
                        }
                        File("$unZipDir${File.separator}$it").renameTo(File("$unZipDir${File.separator}$newPath"))
                    }
                    // 收尾删除 res2
                    FileOperation.deleteDir(File("$unZipDir${File.separator}$whiteTempRes"))
                }
            }


            destArscFile.outputStream().use {
                it.write(resourceFile.toByteArray())
                it.flush()
            }
        }
        if (arscFile.delete()) {
            if (!destArscFile.renameTo(arscFile)) {
                destArscFile.copyTo(arscFile, overwrite = true)
                destArscFile.delete()
            }
        }
        zipInputFile.extractEntries(
            destDir = shrunkApFile,
            filter = { zipEntry ->
                !mapOfDuplicatesReplacements.containsKey(zipEntry.name) && zipEntry.name != ARSC_FILE_NAME
            },
            each = { zipEntry, destFile ->
                if (zipEntry.name.startsWith("res/")) {
                    val (s, s1, s2) = zipEntry.entryToResource()
                    if (s.isNotEmpty()) {
                        if (canObfuscateArsc) {
                            if (mapOfResources.containsKey(resourceName)) {
                                val dir = zipEntry.name.substring(0, zipEntry.name.lastIndexOf("/"))
                                val suffix = zipEntry.name.substring(zipEntry.name.indexOf("."))
                                Log.d(MinifyTask.TAG, "resource %s dir %s", resourceName, dir)
                                if (!resultOfObfuscatedDirs.containsKey(dir)) {
                                    val proguardDir = dirProguard.generateNextProguardFileName()
                                    resultOfObfuscatedDirs[dir] = "${RES_DIR_PROGUARD_NAME}/$proguardDir"
                                    dirFileProguard[dir] = ProguardStringBuilder()
                                    Log.i(MinifyTask.TAG, "dir %s, proguard builder", dir)
                                }
                                resultOfObfuscatedFiles[zipEntry.name] = resultOfObfuscatedDirs[dir] + "/" + dirFileProguard[dir]!!.generateNextProguardFileName() + suffix
                                val success = ArscUtil.replaceResFileName(resTable, mapOfResources[resourceName]!!, zipEntry.name, resultOfObfuscatedFiles[zipEntry.name])
                                if (success) {
                                    destFile = unzipDir.canonicalPath + File.separator + resultOfObfuscatedFiles[zipEntry.name]!!.replace('/', File.separatorChar)
                                }
                            }
                        }
                        if (zipEntry.method == ZipEntry.DEFLATED) {
                            compressedEntry.add(zipEntry.name)
                        }
//                    logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                    } else {
                        logger_.error("parse entry ${zipEntry.name} resource name failed!")
                    }
                } else {
                    if (zipEntry.method == ZipEntry.DEFLATED) {
                        compressedEntry.add(zipEntry.name)
                    }
//                logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                }
            })
    }
    val destFile = File(parentFile, "tmp")
    shrunkApFile.zipFile(destFile) { zipEntry ->
        compressedEntry.contains(zipEntry.name)
    }
    if (delete()) {
        if (!destFile.renameTo(this)) {
            destFile.copyTo(this, overwrite = true)
        }
    }
    return total
}

fun File.deleteRecursivelyIfExists() {
    if (!exists()) return
    deleteRecursively()
}

//private fun BaseVariant.generateReport(results: CompressionResults) {
//    val base = project.buildDir.toURI()
//    val table = results.map {
//        val delta = it.second - it.third
//        CompressionReport(
//            base.relativize(it.first.toURI()).path,
//            it.second,
//            it.third,
//            delta,
//            if (delta == 0L) "0" else decimal(delta),
//            if (delta == 0L) "0%" else percentage((delta).toDouble() * 100 / it.second),
//            decimal(it.second),
//            it.fourth
//        )
//    }
//    val maxWith1 = table.maxOfOrNull { it.first.length } ?: 0
//    val maxWith5 = table.maxOfOrNull { it.fifth.length } ?: 0
//    val maxWith6 = table.maxOfOrNull { it.sixth.length } ?: 0
//    val maxWith7 = table.maxOfOrNull { it.seventh.length } ?: 0
//    val fullWith = maxWith1 + maxWith5 + maxWith6 + 8
//
//    project.buildDir.file("reports", Build.ARTIFACT, name, "report.txt").touch().printWriter()
//        .use { logger ->
//            // sort by reduced size and original size
//            table.sortedWith(compareByDescending<CompressionReport> {
//                it.fourth
//            }.thenByDescending {
//                it.second
//            }).forEach {
//                logger.println(
//                    "${it.sixth.padStart(maxWith6)} ${it.first.padEnd(maxWith1)} ${
//                        it.fifth.padStart(
//                            maxWith5
//                        )
//                    } ${it.seventh.padStart(maxWith7)} ${it.eighth}"
//                )
//            }
//            logger.println("-".repeat(maxWith1 + maxWith5 + maxWith6 + 2))
//            logger.println(" TOTAL ${decimal(table.sumOf { it.fourth.toDouble() }).padStart(fullWith - 13)}")
//        }
//
//}

internal val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

internal val decimal: (Number) -> String = DecimalFormat("#,##0")::format