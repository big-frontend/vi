package com.electrolyte.vi

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.Build
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode

@AutoService(ClassTransformer::class)
class StartupTransformer : ClassTransformer {
    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {

    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        return klass

    }

    override fun onPostTransform(context: TransformContext) {

    }

}