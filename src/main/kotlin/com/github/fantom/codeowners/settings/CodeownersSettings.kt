package com.github.fantom.codeowners.settings

import com.github.fantom.codeowners.util.Listenable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.containers.ContainerUtil
import org.jdom.Element

/**
 * Persistent global settings object for the Codeowners plugin.
 */
@State(name = "CodeownersSettings", storages = [Storage("codeowners.xml")])
class CodeownersSettings : PersistentStateComponent<Element?>, Listenable<CodeownersSettings.Listener> {

    enum class KEY(private val key: String) {
        ROOT("CodeownersSettings"),
        MISSING_CODEOWNERS("missingCodeowners"),
//        LANGUAGES("languages"),
//        LANGUAGES_LANGUAGE("language"),
//        LANGUAGES_ID("id"),
        CODEOWNERS_FILE_STATUS("codeownersFileStatus"),
        INSERT_AT_CURSOR("insertAtCursor")
;

        override fun toString() = key
    }

    /** Notify about missing Gitignore file in the project. */
    var missingCodeowners = true
        set(value) {
            notifyOnChange(KEY.MISSING_CODEOWNERS, missingCodeowners, value)
            field = value
        }

    /** Enable CODEOWNERS file status coloring. */
    var codeownersFileStatus = true
        set(value) {
            notifyOnChange(KEY.CODEOWNERS_FILE_STATUS, codeownersFileStatus, value)
            field = value
        }

    /** Insert new entries at the cursor's position or at the document end. */
    var insertAtCursor = false
        set(value) {
            notifyOnChange(KEY.INSERT_AT_CURSOR, insertAtCursor, value)
            field = value
        }
//
//    /** Settings related to the [CodeownersLanguage]. */
//    var languagesSettings = object : CodeownersLanguagesSettings() {
//        init {
////            CodeownersBundle.LANGUAGES.forEach {
//                put(
//                    CodeownersLanguage.INSTANCE,
//                    object : TreeMap<KEY, Any>() {
//                        init {
//                            put(KEY.NEW_FILE, true)
////                            put(KEY.ENABLE, CodeownersLanguage.INSTANCE.isVCS && !CodeownersBundle.isExcludedFromHighlighting((CodeownersLanguage.INSTANCE)))
//                        }
//                    }
//                )
////            }
//        }
//    }
//        set(value) {
//            notifyOnChange(KEY.LANGUAGES, languagesSettings, value)
//            languagesSettings.clear()
//            languagesSettings.putAll(value)
//        }
//
    private val listeners = ContainerUtil.createConcurrentList<Listener>()

    companion object {
        fun getInstance(): CodeownersSettings = ServiceManager.getService(CodeownersSettings::class.java)
    }
//
    override fun getState() = Element(KEY.ROOT.toString()).apply {
        setAttribute(KEY.MISSING_CODEOWNERS.toString(), missingCodeowners.toString())
        setAttribute(KEY.CODEOWNERS_FILE_STATUS.toString(), codeownersFileStatus.toString())

//        addContent(
//            Element(KEY.LANGUAGES.toString()).apply {
//                languagesSettings.forEach { (key, value) ->
//                    if (key == null) {
//                        return@forEach
//                    }
//                    addContent(
//                        Element(KEY.LANGUAGES_LANGUAGE.toString()).apply {
//                            setAttribute(KEY.LANGUAGES_ID.toString(), key.id)
//                            value.forEach {
//                                setAttribute(it.key.name, it.value.toString())
//                            }
//                        }
//                    )
//                }
//            }
//        )
//        addContent(createTemplatesElement(userTemplates))
    }

    override fun loadState(element: Element) {
        element.apply {
            getAttributeValue(KEY.MISSING_CODEOWNERS.toString())?.let {
                missingCodeowners = it.toBoolean()
            }
            getAttributeValue(KEY.CODEOWNERS_FILE_STATUS.toString())?.let {
                codeownersFileStatus = it.toBoolean()
            }

//            getChild(KEY.LANGUAGES.toString()).children.forEach {
//                val data = TreeMap<CodeownersLanguagesSettings.KEY, Any>()
//                for (key in CodeownersLanguagesSettings.KEY.values()) {
//                    data[key] = it.getAttributeValue(key.name)
//                }
//
//                val id = it.getAttributeValue(KEY.LANGUAGES_ID.toString())
//                val language = CodeownersBundle.LANGUAGES[id]
//                languagesSettings[language] = data
//            }

//            CodeownersBundle.LANGUAGES
//                .filter { !it.isVCS || CodeownersBundle.isExcludedFromHighlighting(it) }
//                .forEach { languagesSettings[it]?.apply { this[CodeownersLanguagesSettings.KEY.ENABLE] = false } }
        }
    }

    override fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyOnChange(key: KEY, oldValue: Any, newValue: Any) {
        if (newValue != oldValue) {
            listeners.forEach { it.onChange(key, newValue) }
        }
    }

    fun interface Listener {

        fun onChange(key: KEY, value: Any?)
    }
//
//    /** Helper class for the [CodeownersLanguage] settings. */
//    open class CodeownersLanguagesSettings : LinkedHashMap<CodeownersLanguage?, TreeMap<CodeownersLanguagesSettings.KEY, Any>>() {
//
//        enum class KEY {
//            NEW_FILE, ENABLE
//        }
//
//        override fun clone(): CodeownersLanguagesSettings {
//            val copy = super.clone() as CodeownersLanguagesSettings
//            for ((key, value) in copy) {
//                copy[key] = value.clone() as TreeMap<KEY, Any>
//            }
//            return copy
//        }
//    }
}
