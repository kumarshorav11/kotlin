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

package org.jetbrains.kotlin.resolve.jvm.extensions

import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor

interface IsAndroidProjectExtension {
    companion object : ProjectExtensionDescriptor<IsAndroidProjectExtension>(
            "org.jetbrains.kotlin.isAndroidProjectExtension",
            IsAndroidProjectExtension::class.java
    )

    /**
     * @return true if the project has modules with Android facet.
     *
     * IsAndroidProjectExtension should be a project extension, so it should already know about the project we are checking now.
     */
    fun isAndroidProject(): Boolean
}