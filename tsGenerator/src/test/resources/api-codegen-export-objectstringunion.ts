export interface TsExportAnnotationTest {
    foo: string;
}

export interface TsExportInterface {
    foo: string | undefined;
}

export type TsExportOrdinalEnumTest = "FOO" | "BAR";

export interface TsExportManualTest extends TsExportInterface {
    bar: string;
    enumType: TsExportOrdinalEnumTest;
    foo: undefined;
}

export const TsExportOrdinalEnumTestType = {
    FOO: 0,
    BAR: 1
} as const;
