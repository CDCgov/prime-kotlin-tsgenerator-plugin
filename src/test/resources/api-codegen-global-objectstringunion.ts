declare global {
    interface TsExportAnnotationTest {
        foo: string;
    }

    interface TsExportInterface {
        foo: string | undefined;
    }

    type TsExportOrdinalEnumTest = "FOO" | "BAR";

    interface TsExportManualTest extends TsExportInterface {
        bar: string;
        enumType: TsExportOrdinalEnumTest;
        foo: undefined;
    }
}

export const TsExportOrdinalEnumTestType = {
    FOO: 0,
    BAR: 1
} as const;
