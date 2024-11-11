@file:JvmName("DependencyUtil")
package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.artifacts.ResolvedArtifact
import java.lang.reflect.Field



//fun getResolvedArtifacts(variant: BaseVariant,consumedConfigType: AndroidArtifacts.ConsumedConfigType):Set<ResolvedArtifact>?{
//        return getResolvedArtifacts2(variant,consumedConfigType)
//}

///**
// * AGP 4+实现
// */
//private fun getResolvedArtifacts2(variant: BaseVariant,consumedConfigType: AndroidArtifacts.ConsumedConfigType):Set<ResolvedArtifact>?{
//    if (variant !is BaseVariantImpl) return null
//    getField(BaseVariantImpl::class.java,"componentProperties")?.apply{
//        isAccessible = true
//        val componentProperties = get(variant) as ComponentPropertiesImpl
//        val buildMapping = componentProperties.globalScope.project.gradle.computeBuildMapping()
//        return getAllArtifacts(componentProperties,consumedConfigType,null,buildMapping)
//    }
//
//}
//
//
//fun getField():Field{
//
//}