export interface TsExportAnnotationTest {
    foo: string;
}

export interface TsExportInterface {
    foo: string | undefined;
}

export interface TsExportManualTest extends TsExportInterface {
    bar: string;
    foo: undefined;
}