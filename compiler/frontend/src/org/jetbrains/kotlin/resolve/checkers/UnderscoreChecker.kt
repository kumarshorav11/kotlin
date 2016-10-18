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

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.descriptors.impl.FunctionExpressionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext

object UnderscoreChecker : SimpleDeclarationChecker {

    @JvmOverloads
    fun checkIdentifier(identifier: PsiElement?, diagnosticHolder: DiagnosticSink, allowOneUnderscore: Boolean = false) {
        if (identifier == null || identifier.text.isEmpty()) return
        if (identifier.text.all { it == '_' } && (!allowOneUnderscore || identifier.text.length != 1)) {
            diagnosticHolder.report(Errors.UNDERSCORE_IS_RESERVED.on(identifier))
        }
    }

    @JvmOverloads
    fun checkNamed(declaration: KtNamedDeclaration, diagnosticHolder: DiagnosticSink, allowOneUnderscore: Boolean = false) {
        checkIdentifier(declaration.nameIdentifier, diagnosticHolder, allowOneUnderscore)
    }

    override fun check(
            declaration: KtDeclaration,
            descriptor: DeclarationDescriptor,
            diagnosticHolder: DiagnosticSink,
            bindingContext: BindingContext
    ) {
        if (declaration is KtProperty && descriptor !is VariableDescriptor) return
        if (declaration is KtCallableDeclaration && descriptor !is FunctionExpressionDescriptor) {
            for (parameter in declaration.valueParameters) {
                checkNamed(parameter, diagnosticHolder)
            }
        }
        if (declaration is KtTypeParameterListOwner) {
            for (typeParameter in declaration.typeParameters) {
                checkNamed(typeParameter, diagnosticHolder)
            }
        }
        if (declaration !is KtNamedDeclaration) return
        checkNamed(declaration, diagnosticHolder)
    }
}
