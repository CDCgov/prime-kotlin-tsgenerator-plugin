package gov.cdc.prime.tsGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class TypescriptGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("typescriptGenerator", TypescriptGeneratorExtension::class.java)
        project.tasks.register("generateTypescriptDefinitions", TypescriptGeneratorTask::class.java) {
            it.useExtension(ext)
        }
    }
}
