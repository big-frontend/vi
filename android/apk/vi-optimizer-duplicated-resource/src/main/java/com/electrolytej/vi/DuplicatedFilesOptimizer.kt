package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
import com.google.auto.service.AutoService
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
@AutoService(BaseOptimizer::class)
class DuplicatedFilesOptimizer : BaseOptimizer {
    companion object {
        private const val DUPLICATED_PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"
    }

    private  var duplicatedIgnores: Set<Wildcard> = mutableSetOf()
    val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
    var s0 = 0L
    var total = 0L
    private  var variant: BaseVariant? = null
    private lateinit var symbols: SymbolList
    private lateinit var logger: L
    override fun start(variant: BaseVariant?, symbols: SymbolList, ap_: File) {
        this.symbols = symbols
        this.logger = L.create(variant,"vi-optimizer-duplicated-files")
        if (variant != null) {
            this.variant = variant
//        this.duplicatedIgnores =
//            variant.project.getProperty(DUPLICATED_PROPERTY_IGNORES, "").trim().split(',')
//                .filter(String::isNotEmpty)
//                .map(Wildcard.Companion::valueOf).toSet()
        }
        logger.println("$DUPLICATED_PROPERTY_IGNORES=$duplicatedIgnores\n")
        if (this.symbols.isEmpty()) {
//            logger_.error("remove duplicated files failed: R.txt doesn't exist or blank")
            return
        }
        //1.find duplicated files from ap file
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

        s0 = ap_.length()
        logger.println("Delete duplicated files:")
    }

    override fun processArsc(resourceFile: ResourceFile):Boolean {
        //2.remove duplicated files  and repack ap file
        val rmDuplicated = mapOfDuplicatesReplacements.isNotEmpty()
        //remove duplicated resources
        if (rmDuplicated) {
            val replaceIterator = mapOfDuplicatesReplacements.keys.iterator()
            while (replaceIterator.hasNext()) {
                val srcEntryName = replaceIterator.next()
                val (srcResId, srcResType, srcResName) = srcEntryName.entryToResource()
                val srcResIdInt  = symbols.getInt(srcResType, srcResName)
                if (srcResIdInt == null){
                    println("symbols not found ${srcResId} in dup")
                    replaceIterator.remove()
                    continue
                }
                val (crc32, size, destEntryName) = mapOfDuplicatesReplacements[srcEntryName] ?: continue
                val (destResId, destResType, destResName) = destEntryName.entryToResource()
                val destResIdInt = symbols.getInt(destResType, destResName)
                if (destResIdInt == null){
                    println("symbols not found  ${destResIdInt} in replace")
                    replaceIterator.remove()
                    continue
                }
                val sourcePkgId = srcResIdInt.getPackageId()
                val targetPkgId = destResIdInt.getPackageId()
                val success = if (sourcePkgId != targetPkgId) {
                    System.out.printf("sourcePkgId %d != targetPkgId %d, quit replace!%n", sourcePkgId, targetPkgId)
                    false
                }else{
                    resourceFile.replaceResource(srcEntryName,destEntryName)
                }
                if (!success) {
                    println("replace ${srcResId}($srcEntryName) with $destResId($destEntryName) failed!")
                    replaceIterator.remove()
                } else {
                    total += size
                    logger.println(" * replace 0x${srcResIdInt.toString(16)}($srcEntryName) with 0x${destResIdInt.toString(16)}($destEntryName)\t${size}bytes crc32/$crc32")
                }
            }
        }
        return false
    }

    override fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean {
        val destFile = File(destDir, zipEntry.name.replace('/', File.separatorChar))
        if (mapOfDuplicatesReplacements.containsKey(zipEntry.name)) {
            return true
        }
        if (zipEntry.isDirectory) {
            destFile.createOrExistsDir()
        } else {
            destFile.createOrExistsFile()
            srcFile.extractEntry(destFile, zipEntry)
        }
        return false
    }

    override fun end(ap_: File) {
        val s1 = ap_.length()
        val maxWidth = mapOfDuplicatesReplacements.map { it.key.length }.maxOrNull()?.plus(10) ?: 10
        logger.println("-".repeat(maxWidth))
        logger.println("Total: $total bytes, ap length: ${s0 - s1} bytes")
        logger.close()
    }
}

fun ZipFile.findDuplicatedFiles(filter: ((ZipEntry) -> Boolean)? = null,each: ((ZipEntry, ZipEntry) -> Unit)? = null) {
    val duplicated = mutableMapOf<Long, MutableList<ZipEntry>>()
    val entries = entries()
    while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        if (entry.isDirectory) continue
        if (entry.size <= 0) continue
        //duplicatedFiles:map crc32 to entry
        if (!duplicated.containsKey(entry.crc)) {
            duplicated[entry.crc] = mutableListOf()
        }
        duplicated[entry.crc]?.add(entry)
    }
    val iterator = duplicated.values.iterator()
    while (iterator.hasNext()) {
        val duplicatedEntries = iterator.next()
        if (duplicatedEntries.size <= 1) continue
        if (!duplicatedEntries.isSameResourceType()) {
//            logger_.error("the type of duplicated resources $duplicatedEntries are not same!")
            continue
        }
        var it = duplicatedEntries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (filter?.invoke(entry) == false) {
                it.remove()
            }
        }
        if (duplicatedEntries.size <= 1) continue
        it = duplicatedEntries.iterator()
        val replaceEntry = it.next()
        while (it.hasNext()) {
            val dupEntry = it.next()
            each?.invoke(dupEntry, replaceEntry)
        }
    }
}