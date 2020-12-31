package com.github.fantom.codeowners.services

import com.intellij.openapi.project.Project
import com.github.fantom.codeowners.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
