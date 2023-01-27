package gov.cdc.prime.tsGenerator

import me.ntrrgc.tsGenerator.TypeScriptGenerator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

enum class CompareFile(val filename: String, val importMode: TypescriptImportMode, val enumType: TypescriptEnumType) {
    DEFAULT("api-codegen.ts", TypescriptImportMode.NONE, TypescriptEnumType.STRING_UNION),
    EXPORT("api-codegen-export.ts", TypescriptImportMode.EXPORT, TypescriptEnumType.STRING_UNION),
    GLOBAL("api-codegen-global.ts", TypescriptImportMode.DECLARE_GLOBAL, TypescriptEnumType.STRING_UNION),
    EXPORT_OBJECT_AND_STRING_UNION("api-codegen-export-objectstringunion.ts", TypescriptImportMode.EXPORT, TypescriptEnumType.OBJECT_AND_STRING_UNION),
    GLOBAL_OBJECT_AND_STRING_UNION("api-codegen-global-objectstringunion.ts", TypescriptImportMode.DECLARE_GLOBAL, TypescriptEnumType.OBJECT_AND_STRING_UNION);

    fun getText(): String {
        var text = ""
        val file = TypescriptGeneratorPluginTest::class.java.classLoader.getResourceAsStream(this.filename)
        if(file != null) {
            val compareReader = file.reader()
            text = compareReader.readText()
            compareReader.close()
        }
        return text
    }
}

class TypescriptGeneratorPluginTest {
    @field:TempDir
    lateinit var tmpDir: File
    private var project: Project = ProjectBuilder.builder().build()
    private val generateTask: TypescriptGeneratorTask
    private val pluginExt: TypescriptGeneratorConfigExtension

    init {
        project.pluginManager.apply("gov.cdc.prime.tsGenerator")
        generateTask = project.tasks.withType(TypescriptGeneratorTask::class.java).first()
        pluginExt = project.extensions.getByType(TypescriptGeneratorConfigExtension::class.java)
    }

    @Test
    fun typescriptGeneratorOutputIsCorrect() {
        val classPath = (project.layout.projectDirectory.files("test/kotlin/TsExportTest.kt"))
        val manualClasses = listOf(
            "gov.cdc.prime.tsGenerator.TsExportManualTest"
        )

        enumValues<CompareFile>().forEach {
            val params = createTypeScriptGeneratorParameters(classPath, manualClasses)
            val generator = TypeScriptGenerator(
                rootClasses = params.rootClasses,
                mappings = params.mappings,
                intTypeName = params.intTypeName,
                voidType = params.voidType,
                classTransformers = params.classTransformers
            )
            val result = generator.generateDefinitionsText(it.importMode, it.enumType)
            assertEquals(it.getText(), result)
        }
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
        val tmpExportFile = File(tmpDir, "api-codegen.ts")
        pluginExt.apply {
            outputPath.set(tmpExportFile.toPath())
            classPath.set(project.layout.projectDirectory.files("test/kotlin/TsExportTest.kt"))
            manualClasses.set(
                listOf(
                    "gov.cdc.prime.tsGenerator.TsExportManualTest"
                )
            )
        }
        generateTask.generateTypescriptDefinitions()
        assertTrue(tmpExportFile.exists())
        val result = tmpExportFile.readText()
        assertEquals(CompareFile.DEFAULT.getText(), result)
    }
}
