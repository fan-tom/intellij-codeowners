package com.github.fantom.intellijcodeowners.services

import com.intellij.openapi.project.Project
import com.github.fantom.intellijcodeowners.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
