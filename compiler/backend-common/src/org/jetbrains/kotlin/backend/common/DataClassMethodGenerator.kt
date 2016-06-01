/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.OverrideResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns

/**
 * A platform-independent logic for generating data class synthetic methods.
 * TODO: data class with zero components gets no toString/equals/hashCode methods. This is inconsistent and should be
 * changed here with the platform backends adopted.
 */
abstract class DataClassMethodGenerator(private val declaration: KtClassOrObject, private val bindingContext: BindingContext) {
    protected val classDescriptor: ClassDescriptor = BindingContextUtils.getNotNull(bindingContext, BindingContext.CLASS, declaration)

    private val builtIns = classDescriptor.builtIns

    fun generate() {
        generateComponentFunctionsForDataClasses()

        generateCopyFunctionForDataClasses(primaryConstructorParameters)

        val properties = dataProperties
        if (properties.isNotEmpty()) {
            generateDataClassToStringIfNeeded(properties)
            generateDataClassHashCodeIfNeeded(properties)
            generateDataClassEqualsIfNeeded(properties)
        }
    }

    protected abstract fun generateComponentFunction(function: FunctionDescriptor, parameter: ValueParameterDescriptor)

    protected abstract fun generateCopyFunction(function: FunctionDescriptor, constructorParameters: List<KtParameter>)

    protected abstract fun generateToStringMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    protected abstract fun generateHashCodeMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    protected abstract fun generateEqualsMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    private fun generateComponentFunctionsForDataClasses() {
        // primary constructor should exist for data classes
        // but when generating light-classes still need to check we have one
        val constructor = classDescriptor.unsubstitutedPrimaryConstructor ?: return

        for (parameter in constructor.valueParameters) {
            val function = bindingContext.get(BindingContext.DATA_CLASS_COMPONENT_FUNCTION, parameter)
            if (function != null) {
                generateComponentFunction(function, parameter)
            }
        }
    }

    private fun generateCopyFunctionForDataClasses(constructorParameters: List<KtParameter>) {
        val copyFunction = bindingContext.get(BindingContext.DATA_CLASS_COPY_FUNCTION, classDescriptor) ?: return
        generateCopyFunction(copyFunction, constructorParameters)
    }

    private fun generateDataClassToStringIfNeeded(properties: List<PropertyDescriptor>) {
        val function = getDeclaredMember("toString", builtIns.string)
        if (function != null && isTrivial(function)) {
            generateToStringMethod(function, properties)
        }
    }

    private fun generateDataClassHashCodeIfNeeded(properties: List<PropertyDescriptor>) {
        val function = getDeclaredMember("hashCode", builtIns.int)
        if (function != null && isTrivial(function)) {
            generateHashCodeMethod(function, properties)
        }
    }

    private fun generateDataClassEqualsIfNeeded(properties: List<PropertyDescriptor>) {
        val function = getDeclaredMember("equals", builtIns.boolean, builtIns.any)
        if (function != null && isTrivial(function)) {
            generateEqualsMethod(function, properties)
        }
    }

    private val dataProperties: List<PropertyDescriptor>
        get() = primaryConstructorParameters
                .filter { it.hasValOrVar() }
                .map { bindingContext.get(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, it)!! }

    private val primaryConstructorParameters: List<KtParameter>
        get() = (declaration as? KtClass)?.getPrimaryConstructorParameters().orEmpty()

    private fun getDeclaredMember(
            name: String,
            returnedClassifier: ClassDescriptor,
            vararg valueParameterClassifiers: ClassDescriptor
    ): FunctionDescriptor? = CodegenUtil.getDeclaredFunctionByRawSignature(
            classDescriptor, Name.identifier(name), returnedClassifier, *valueParameterClassifiers
    )

    /**
     * @return true if the member is an inherited implementation of a method from Any
     */
    private fun isTrivial(function: FunctionDescriptor): Boolean {
        return function.kind != CallableMemberDescriptor.Kind.DECLARATION &&
               OverrideResolver.getOverriddenDeclarations(function).none { overridden ->
                   overridden is CallableMemberDescriptor &&
                   overridden.kind == CallableMemberDescriptor.Kind.DECLARATION &&
                   overridden.containingDeclaration != builtIns.any
               }
    }
}