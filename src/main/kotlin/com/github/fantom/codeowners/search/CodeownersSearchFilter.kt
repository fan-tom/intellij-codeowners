package com.github.fantom.codeowners.search

import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.OwnersList
import com.github.fantom.codeowners.indexing.CodeownersEntryOccurrence
import com.github.fantom.codeowners.indexing.OwnerString
import com.intellij.openapi.vfs.VirtualFile

interface Predicate {
    // TODO change to OwnersSet, duplicates make no sense
    fun satisfies(fileOwners: OwnersList?): Boolean
}

sealed interface Filter: Predicate {
    fun displayName(): CharSequence

    // TODO rewrite using service + context
    /**
     * @return false, if this filter definitely cannot be satisfied by given codeowners file, true otherwise
     */
    fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean

    sealed interface Condition: Filter {
        // files without assigned owners
        data object Unowned: Condition {
            override fun displayName() = "unowned"

            override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
                return true // cannot easily calculate it precise: need to proof there is at least one unowned file
            }

            override fun satisfies(fileOwners: OwnersList?) = fileOwners == null
        }

        // explicitly unowned files
        data object OwnershipReset: Condition {
            override fun displayName() = "explicitly unowned"

            override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
                return codeownersFile.items.any { it.second.owners.isEmpty() }
            }

            override fun satisfies(fileOwners: OwnersList?) = fileOwners?.isEmpty() ?: false
        }

        // files, owned by any (at least one) of the given owners
        data class OwnedByAnyOf(val owners: Set<OwnerString>): Condition {
            override fun displayName() = "owned by any of ${owners.joinToString(", ")}"

            override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
                return true // because "owners" can be only from this file, so they own some files
            }

            override fun satisfies(fileOwners: OwnersList?) = fileOwners?.any { it in owners } ?: false
        }

        // files, owned by all the given owners, and maybe by some extra owners
        data class OwnedByAllOf(val owners: Set<OwnerString>): Condition {
            override fun displayName() = "owned by all of ${owners.joinToString(", ")}"

            override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
                return codeownersFile.items.map { it.second }.any { satisfies(it.owners) }
            }

            override fun satisfies(fileOwners: OwnersList?) = fileOwners?.containsAll(owners) ?: false
        }

        // files, owned by only given owners, no extra owners allowed
        data class OwnedByExactly(val owners: Set<OwnerString>): Condition {
            override fun displayName() = "owned by exactly ${owners.joinToString(", ")}"

            override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
                return codeownersFile.items.map { it.second }.any { satisfies(it.owners) }
            }

            override fun satisfies(fileOwners: OwnersList?) = fileOwners?.let { owners == it.toSet() } ?: false
        }
    }

    // negation of some Condition
    data class Not(val condition: Condition): Filter {
        override fun displayName() = "not ${condition.displayName()}"

        override fun satisfiable(codeownersFile: CodeownersEntryOccurrence): Boolean {
            // cannot calculate it in abstract: it depends on the nature of the "condition"
            return true
        }

        override fun satisfies(fileOwners: OwnersList?): Boolean {
            return !condition.satisfies(fileOwners)
        }
    }
}

// disjunction of conjunctions
data class DNF(val filters: List<List<Filter>>): Predicate {
    override fun satisfies(fileOwners: OwnersList?) = filters.any { conj -> conj.all { n -> n.satisfies(fileOwners) } }

    fun displayName() =
        filters.joinToString(" or ") { conj -> "(${conj.joinToString(" and ") { n -> n.displayName() }})" }
}

data class CodeownersSearchFilter(
    val codeownersFile: CodeownersEntryOccurrence,
    // DNF: disjunction of conjunctions
    private val dnf: DNF
): Predicate by dnf {
    fun displayName() = "${codeownersFile.url}: ${dnf.displayName()}"

    context(CodeownersManager)
    fun satisfies(file: VirtualFile): Boolean {
        val ownersRef = getFileOwners(file, codeownersFile).getOrNull() ?: return false
        val ownersList = ownersRef.ref?.owners

        return satisfies(ownersList)
    }
}