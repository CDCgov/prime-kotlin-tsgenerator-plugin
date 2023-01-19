package gov.cdc.prime.tsGenerator.plugin

import gov.cdc.prime.tsGenerator.library.TsExportAnnotationConfig
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypescriptGeneratorPluginTest {
    @field:TempDir
    lateinit var tmpDir: File
    private var project: Project = ProjectBuilder.builder().build()
    private var compareExportFile = TypescriptGeneratorPluginTest::class.java.classLoader.getResourceAsStream("api-codegen.d.ts")
    private val generateTask: TypescriptGeneratorTask
    private val pluginExt: TypescriptGeneratorExtension

    init {
        project.pluginManager.apply("gov.cdc.prime.tsGenerator.plugin")
        generateTask = project.tasks.withType(TypescriptGeneratorTask::class.java).first()
        pluginExt = project.extensions.getByType(TypescriptGeneratorExtension::class.java)
    }

    @Test
    fun pluginAddsGenerateTaskToProject() {
        assertEquals(generateTask, project.tasks.getByName("generateTypescriptDefinitions"))
    }

    @Test
    fun pluginAddsExtensionToProject() {
        assertEquals(pluginExt, project.extensions.getByName("typescriptGenerator"))
    }

    @Test
    fun pluginGenerateTaskTestIsSuccessful() {
        val tmpExportFile = File(tmpDir, "api-codegen.d.ts")
        pluginExt.apply {
            outputPath.set(tmpExportFile.toPath())
            classPath.set(project.layout.projectDirectory.files("test/kotlin/TsExportTest.kt"))
            annotation.set(TsExportAnnotationConfig("gov.cdc.prime.tsGenerator.plugin"))
            manualClasses.set(
                listOf(
                    "gov.cdc.prime.tsGenerator.plugin.TsExportManualTest"
                )
            )
        }
        generateTask.generateTypescriptDefinitions()
        assertTrue(tmpExportFile.exists())
        val reader = compareExportFile?.reader()!!
        val orig = reader.readText()
        reader.close()
        val compare = tmpExportFile.readText()
        assertEquals(orig, compare)
    }
}