/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.matrix.plugin.task

import com.android.build.gradle.AppExtension
import com.tencent.matrix.javalib.util.Log
import com.tencent.matrix.javalib.util.Util
import com.tencent.matrix.plugin.compat.CreationConfig
import com.tencent.matrix.plugin.compat.MatrixTraceCompat
import com.tencent.matrix.trace.extension.MatrixTraceExtension
import org.gradle.api.Project

class MatrixTasksManager {

    companion object {
        const val TAG = "Matrix.TasksManager"
    }

    fun createMatrixTasks(android: AppExtension,
                          project: Project,
                          traceExtension: MatrixTraceExtension) {

//        createMatrixTraceTask(android, project, traceExtension)
    }

    private fun createMatrixTraceTask(
            android: AppExtension,
            project: Project,
            traceExtension: MatrixTraceExtension) {
        MatrixTraceCompat().inject(android, project, traceExtension)
    }
}
