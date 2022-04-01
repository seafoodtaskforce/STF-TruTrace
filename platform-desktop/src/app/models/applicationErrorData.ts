export class ApplicationErrorData {
    public static readonly REQUIRED_FIELD_MESSAGE = "This field is required";
    public static readonly INCORRECT_DATA_FIELD_MESSAGE = "Please enter correct data";
    public static readonly INCOMPLETE_DATA_FIELD_MESSAGE = "Data entered is incomplete";
    isError: boolean = false;
    message: string;
}