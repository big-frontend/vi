package com.electrolytej.vi

import com.android.build.api.variant.LibraryVariant
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.kotlin.dsl.provideDelegate

@AutoService(VariantProcessor::class)
class StartupVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val GROUP_ID:String by variant.project
        if (variant !is LibraryVariant && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "${GROUP_ID}.vi-optimizer-tasklist-composer:1.0.0")
        }
    }
}