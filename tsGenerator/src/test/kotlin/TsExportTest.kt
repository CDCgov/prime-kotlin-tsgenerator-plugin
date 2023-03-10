package gov.cdc.prime.tsGenerator

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreType

/**
 * Typescript export will not include properties with this type
 */
@JsonIgnoreType
class TsExportJsonIgnoreTypeTest(
    @Suppress("unused")val bar: String
)

/**
 * Nullable type interface
 */
interface TsExportInterface {
    val foo: String?
}

/**
 * Test class load by annotation
 */
@TsExport
@Suppress("unused")
class TsExportAnnotationTest(val foo: String)

@Suppress("unused")
enum class TsExportOrdinalEnumTest {
    FOO,
    BAR
}

/**
 * Test class load by manual fqn.
 * Typescript export will only have "test" and "foo" properties.
 * "foo" will be overriden to be undefined only.
 */
@Suppress("unused")
class TsExportManualTest(
    @JsonIgnore val ignoreField: String,
    @get:JsonIgnore val ignoreGet: String,
    val ignoreType: TsExportJsonIgnoreTypeTest,
    val bar: String,
    val enumType: TsExportOrdinalEnumTest
) : TsExportInterface {
    @get:JsonIgnore
    override val foo: String? = null
}
