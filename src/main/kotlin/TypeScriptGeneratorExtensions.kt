package gov.cdc.prime.tsGenerator

import me.ntrrgc.tsGenerator.TypeScriptGenerator

enum class TypescriptImportMode {
    NONE,
    EXPORT,
    DECLARE_GLOBAL
}

enum class TypescriptEnumType {
    STRING_UNION,
    OBJECT_AND_STRING_UNION
}

val StringUnionTypeRegex = "^type \\w*? = (\"\\w*?\"(\\s\\|\\s)?)*;$".toRegex()
val TypeNameRegex = "^type (\\w*?) =".toRegex()
val UnionStringRegex = "\"(\\w*?)\"".toRegex()

fun getUnionStrings(definition: String): Set<String> {
    return UnionStringRegex.findAll(definition).map { it.groups[1]!!.value }.toSet()
}

fun TypeScriptGenerator.getIndividualDefinitions(
    importMode: TypescriptImportMode,
    enumType: TypescriptEnumType
): Pair<Set<String>, Set<String>> {
    val defs = mutableListOf<String>()
    val objectEnums = mutableListOf<String>()

    this.individualDefinitions.forEach { def ->
        if (importMode == TypescriptImportMode.DECLARE_GLOBAL) {
            val fDef = def.replace("\n", "\n    ")
            defs.add("    $fDef")
        } else if (importMode == TypescriptImportMode.EXPORT) {
            defs.add("export $def")
        } else {
            defs.add(def)
        }
        if (StringUnionTypeRegex.matches(def) && enumType == TypescriptEnumType.OBJECT_AND_STRING_UNION) {
            val props = getUnionStrings(def)
            val defName = TypeNameRegex.find(def)!!.groups[1]!!.value
            val name = if (defName.endsWith("Type", true)) defName.substring(0, -4) else "${defName}Type"
            val objectEnumDef = "const $name = {\n" +
                props.withIndex().joinToString(",\n") { "    ${it.value}: ${it.index}" } +
                "\n} as const;"
            if (importMode != TypescriptImportMode.NONE) {
                objectEnums.add("export $objectEnumDef")
            } else {
                objectEnums.add(objectEnumDef)
            }
        }
    }

    return Pair(defs.toSet(), objectEnums.toSet())
}

fun TypeScriptGenerator.generateDefinitionsText(
    importMode: TypescriptImportMode = TypescriptImportMode.NONE,
    enumType: TypescriptEnumType = TypescriptEnumType.STRING_UNION
): String {
    val (defs, objectDefs) = this.getIndividualDefinitions(importMode, enumType)
    val defsSection = defs.joinToString("\n\n")
    val objectDefsSection = objectDefs.joinToString("\n\n")

    if (importMode == TypescriptImportMode.DECLARE_GLOBAL) {
        var text = "declare global {\n$defsSection\n}\n"
        if (objectDefsSection == "") {
            text += "\nexport {}\n"
        } else {
            text += "\n$objectDefsSection\n"
        }

        return text
    }

    var text = "$defsSection\n"
    if (objectDefsSection != "") {
        text += "\n$objectDefsSection\n"
    }

    return text
}
