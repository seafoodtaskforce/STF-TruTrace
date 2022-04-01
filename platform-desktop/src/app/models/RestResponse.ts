import { IdentifiableEntity } from './identifiableEntity';

/**
 * The main reponse object that the backend send to the UI
 */
export class RESTResponse extends IdentifiableEntity {
    data: any;                          // response data as an object
    errorData: ResponseErrorData[];     // errors
    messageData: ReponseMessageData[];  // non-error messages
}

/**
 * Data specific to error messages
 */
export class ResponseErrorData extends IdentifiableEntity {
    header: string;                     // the header for the error message
    mediaType : string;                 // media type for the response
    issues: ResponseIssue[];         // the actual textual issues itemized for the request
}

/**
 * A singular issue for the data/functionality request
 */
export class ResponseIssue extends IdentifiableEntity {

    /**
     * Static global variables
     */
    public static readonly ISSUE_SEVERITY_WARNING:string = "WARNING";
    public static readonly ISSUE_SEVERITY_ERROR:string = "ERROR";
    public static readonly ISSUE_SEVERITY_FATAL_ERROR:string = "FATAL";

    lineNumber :string;                 // line number if the input was a file
    columnNumber : string;              // column numberof the input had columns 1-based
    columnName : string;                // column name if names have been designated
    atCharLocation : string;            //  specific locatio of the 1st character of the issue 1-based
    issue : string;                     // the actual issue representation as a string
    severity : string;                  // severity if the issue as noted in the static variables above
    rawMessage : string;                // the raw message which is the original data for the issue

}

/**
 * Simpel reponse message for requests that encountered no issues
 */
export class ReponseMessageData extends IdentifiableEntity {
    messages: string[];                 // set of specific messages
}