package gov.cdc.prime.tsGenerator

import io.github.classgraph.ClassGraph
import me.ntrrgc.tsGenerator.TypeScriptGenerator
import me.ntrrgc.tsGenerator.VoidType
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.reflect.KClass

data class TypeScriptGeneratorParameters(
    val rootClasses: List<KClass<out Any>>,
    val mappings: Map<KClass<out Any>, String>,
    val intTypeName: String,
    val voidType: VoidType,
    val classTransformers: List<TypescriptClassTransformer>
)

var Annotation = TsExport::class.qualifiedName!!

fun createTypeScriptGeneratorParameters(
    classPath: FileCollection,
    manualClasses: List<String> = listOf<String>(),
    typeMappings: Map<String, String> = mapOf("java.lang.Void" to "undefined"),
    intTypeName: String = "number",
    voidType: VoidType = VoidType.UNDEFINED
): TypeScriptGeneratorParameters {
    val urls = classPath.files.map { it.toURI().toURL() }
    val classLoader = URLClassLoader(urls.toTypedArray())
    val classGraph = ClassGraph().addClassLoader(classLoader).enableAllInfo().scan()

    val klasses = classGraph.allClasses.filter { klass ->
        !klass.hasAnnotation(JsonAnnotations.JSONIGNORETYPE.fullName) &&
            klass.hasAnnotation(Annotation) ||
            (manualClasses.any { it == klass.name })
    }.map { it.loadClass().kotlin }

    val mappings =
        typeMappings.entries.associate { (className, typescriptName) ->
            Class.forName(
                className,
                true,
                classLoader
            ).kotlin to typescriptName
        }

    val classTransformers = listOf(TypescriptClassTransformer())

    return TypeScriptGeneratorParameters(
        rootClasses = klasses,
        mappings = mappings,
        intTypeName = intTypeName,
        voidType = voidType,
        classTransformers = classTransformers
    )
}

@Suppress("UnstableApiUsage")
abstract class TypescriptGeneratorTask : DefaultTask() {
    @get:Input
    abstract val manualClasses: ListProperty<String>

    @get:CompileClasspath
    abstract val classPath: Property<FileCollection>

    @get:Input
    abstract val typeMappings: MapProperty<String, String>

    @get:Input
    abstract val imports: ListProperty<String>

    @get:Input
    abstract val intTypeName: Property<String>

    @get:Input
    abstract val voidType: Property<VoidType>

    @get:OutputFile
    abstract val outputPath: Property<Path>

    @get:Input
    abstract val typescriptImportMode: Property<TypescriptImportMode>

    @get:Input
    abstract val typescriptEnumType: Property<TypescriptEnumType>

    init {
        description = "Generates Typescript definitions from Kotlin classes."
    }

    fun useExtension(ext: TypescriptGeneratorConfigExtension) {
        manualClasses.set(ext.manualClasses)
        outputPath.set(ext.outputPath)
        classPath.set(ext.classPath)
        typeMappings.set(ext.typeMappings)
        imports.set(ext.imports)
        intTypeName.set(ext.intTypeName)
        voidType.set(ext.voidType)
        typescriptImportMode.set(ext.typescriptImportMode)
        typescriptEnumType.set(ext.typescriptEnumType)
    }

    @TaskAction
    fun generateTypescriptDefinitions() {
        if (!outputPath.isPresent) {
            throw IncompletePluginConfigurationException("outputPath")
        }
        if (!classPath.isPresent) {
            throw IncompletePluginConfigurationException("classPath")
        }

        val tsParams = createTypeScriptGeneratorParameters(
            classPath.get(),
            manualClasses.get(),
            typeMappings.get(),
            intTypeName.get(),
            voidType.get()
        )

        logger.lifecycle("Found ${tsParams.rootClasses.size} exportable class(es)")

        val generator = TypeScriptGenerator(
            rootClasses = tsParams.rootClasses,
            mappings = tsParams.mappings,
            intTypeName = tsParams.intTypeName,
            voidType = tsParams.voidType,
            classTransformers = tsParams.classTransformers
        )

        val result = generator.generateDefinitionsText(typescriptImportMode.get(), typescriptEnumType.get())

        outputPath.get().toFile().writeText(result)
        logger.lifecycle(outputPath.get().toString())
    }
}

class IncompletePluginConfigurationException(missing: String) : IllegalArgumentException(
    "Incomplete TypescriptGenerator plugin configuration: $missing is missing"
)

@Suppress("unused")
class InvalidPluginConfigurationException(input: String, expected: String) : IllegalArgumentException(
    "Incomplete TypescriptGenerator plugin configuration: $input is invalid. Expected: $expected."
)