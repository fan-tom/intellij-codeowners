package com.github.fantom.codeowners.search

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.search.ui.CodeownersSearchFilterDialog
import com.intellij.ide.util.scopeChooser.ScopeDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

class CodeownersSearchScopeDescriptor(private val project: Project) : ScopeDescriptor(null) {
    private val manager = project.service<CodeownersManager>()

    /**
     * It is a very dirty hack to avoid showing [CodeownersSearchFilterDialog] when user opens `Search Structurally` dialog.
     * The issue is that when it happens, IDEA uses [com.intellij.structuralsearch.plugin.ui.ScopePanel.SCOPE_FILTER]
     * to allow `com.intellij.ide.util.scopeChooser.ClassHierarchyScopeDescriptor` in selection list, but
     * filter out [com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope].
     * It does so by comparing [com.intellij.ide.util.scopeChooser.ScopeDescriptor.getDisplayName] value with
     * the message for key `scope.class.hierarchy`
     * and checking type of the scope returned by [com.intellij.ide.util.scopeChooser.ScopeDescriptor.getScope]
     * for all other descriptors:
     * ```java
     *   private static final Condition<ScopeDescriptor> SCOPE_FILTER =
     *   (ScopeDescriptor descriptor) -> IdeBundle.message("scope.class.hierarchy").equals(descriptor.getDisplayName()) ||
     *   !(descriptor.getScope() instanceof ModuleWithDependenciesScope); // don't show module scope
     * ```
     * So, since upon first call to [getScope] [cachedScope] is `null`, we will show the dialog.
     * The same would happen for `ClassHierarchyScopeDescriptor`, but it is handled by this `getDisplayName` comparison
     *
     * The solution is to have a [unsafeToShowDialog] flag, that will protect us from showing the dialog
     * on first invocation in this case, since we set it to `true` only after [getDisplayName] s invoked,
     * which is the first method invoked in this `SCOPE_FILTER`. All other methods' invocations will reset it to `false`.
     *
     * In the regular case, e.g., when selecting this scope in `Find in Files` dialog,
     * we will show it on the first invocation, because in this case it turns out [getDisplayName]
     * is not the last method invoked before [getScope], so when [getScope] is invoked in these cases,
     * [unsafeToShowDialog] is reset to `false` by some other method (including the first invocation of [getScope]).
     */
    private var unsafeToShowDialog = false

    private var cachedScope: SearchScope? = null

    override fun getDisplayName(): String {
        unsafeToShowDialog = true
        return CodeownersBundle.message("search.scope.name")
    }

    override fun getIcon() = CodeownersIcons.FILE.also { unsafeToShowDialog = false }

    override fun scopeEquals(scope: SearchScope?): Boolean {
        unsafeToShowDialog = false
        return cachedScope == scope
    }

    override fun getScope(): SearchScope? {
        if (unsafeToShowDialog) {
            unsafeToShowDialog = false
            return null
        }
        if (cachedScope == null) {
            val codeownersFiles = manager.getCodeownersFiles().ifEmpty { return null }

            val dialog = CodeownersSearchFilterDialog(project, codeownersFiles)

            if (!dialog.showAndGet()) {
                cachedScope = GlobalSearchScope.EMPTY_SCOPE
                return null
            }

            val result = dialog.result!! // it cannot be null

            val (codeownersFile, dnf) = result

            cachedScope = CodeownersSearchScope(project, CodeownersSearchFilter(codeownersFile, DNF(dnf)))
        }

        return cachedScope
    }
}