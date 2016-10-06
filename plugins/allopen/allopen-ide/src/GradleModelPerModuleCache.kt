/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.allopen

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.io.File
import java.util.*

class GradleModelPerModuleCache(val project: Project) {
    private companion object {
        private val GRADLE_SYSTEM_ID = ProjectSystemId("GRADLE")
    }

    private class CachedData(val externalProject: ExternalProject, val fqNames: MutableMap<String, List<String>> = mutableMapOf())

    private val cache: CachedValue<WeakHashMap<Module, CachedData>> = cachedValue(project) {
        CachedValueProvider.Result.create(WeakHashMap<Module, CachedData>(), ProjectRootModificationTracker.getInstance(project))
    }

    fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner, taskName: String): List<String> {
        val project = modifierListOwner.project
        val projectLocation = project.presentableUrl ?: return emptyList() //TODO

        val virtualFile = modifierListOwner.containingFile?.virtualFile ?: return emptyList()
        val module = ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(virtualFile) ?: return emptyList()
        val linkedProjectPath = module.getOptionValue(ExternalSystemConstants.LINKED_PROJECT_PATH_KEY) ?: return emptyList()

        val cachedData = cache.value.getOrPut(module) {
            val externalProjectCache = ExternalProjectDataCache.getInstance(project) ?: return emptyList()
            val rootExternalProject = externalProjectCache.getRootExternalProject(GRADLE_SYSTEM_ID, File(projectLocation)) ?: return emptyList()
            val moduleExternalProject = findProject(rootExternalProject, linkedProjectPath) ?: return emptyList()
            CachedData(moduleExternalProject)
        } ?: return emptyList()

        return cachedData.fqNames.getOrPut(taskName) {
            val allOpenDataStorageTaskDescription = cachedData.externalProject.tasks[taskName]?.description ?: ""
            allOpenDataStorageTaskDescription.substringAfter(":", missingDelimiterValue = "").trim().split(',')
        }
    }

    private fun findProject(externalProject: ExternalProject, modulePath: String): ExternalProject? {
        if (externalProject.projectDir.absolutePath == modulePath) {
            return externalProject
        }

        for (childProject in externalProject.childProjects.values) {
            findProject(childProject, modulePath)?.let { return it }
        }

        return null
    }

    private fun <T> cachedValue(project: Project, result: () -> CachedValueProvider.Result<T>): CachedValue<T> {
        return CachedValuesManager.getManager(project).createCachedValue(result, false)
    }
}