package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
class DuplicatedFilesOptimizer(
    val variant: BaseVariant,
    val symbols: SymbolList,
    val logger: L
) : BaseOptimizer() {
    companion object {
        private const val DUPLICATED_PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"
    }

    private  var duplicatedIgnores: Set<Wildcard> = mutableSetOf()
    val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
    var s0 = 0L
    var total = 0L
    override fun start(ap_: File) {
        this.duplicatedIgnores =
            variant.project.getProperty(DUPLICATED_PROPERTY_IGNORES, "").trim().split(',')
                .filter(String::isNotEmpty)
                .map(Wildcard.Companion::valueOf).toSet()
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
//                    logger_.error("replace ${srcResId}($srcEntryName) with $destResId($destEntryName) failed!")
                    replaceIterator.remove()
                } else {
                    total += size
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
        logger.println("Delete duplicated files:")
        val maxWidth = mapOfDuplicatesReplacements.map { it.key.length }.maxOrNull()?.plus(10) ?: 10
        mapOfDuplicatesReplacements.forEach { dup, (crc32, size, replace) ->
            val (srcResId, srcResType, srcResName) = dup.entryToResource()
            val srcResIdInt = symbols.getInt(srcResType, srcResName)
            val (destResId, destResType, destResName) = replace.entryToResource()
            val destResIdInt = symbols.getInt(destResType, destResName)
            logger.println(
                " * replace 0x${srcResIdInt.toString(16)}($dup) with 0x${
                    destResIdInt.toString(
                        16
                    )
                }($replace)\t${size}bytes crc32/$crc32"
            )
        }
        logger.println("-".repeat(maxWidth))
        logger.println("Total: $total bytes, ap length: ${s0 - s1} bytes")
    }
}