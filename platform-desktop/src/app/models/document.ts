import { Page } from './page';
import { DocumentTag } from './documentTag';
import { DocumentType } from './documentType';
import { NoteData } from './noteData';
import { User } from './user';

export class Document {
     public static readonly TYPE_DESIGNATION_PASSTHROUGH = "Passthrough";
     public static readonly TYPE_DESIGNATION_PROFILE = "Profile";

     public static readonly STATUS_REJECTED = "REJECTED";
     public static readonly STATUS_RESUBMITTED = "RESUBMITTED";
     public static readonly STATUS_SUBMITTED = "SUBMITTED";
     public static readonly STATUS_PENDING = "PENDING";
     public static readonly STATUS_DRAFT = "DRAFT";
     public static readonly STATUS_ACCEPTED = "ACCEPTED";

     id: number=0;
     status:string;
     documentType: string;
     type:DocumentType;
     owner: string;
     creationTimestamp: string;
     creationFilterTimestamp: string;
     TypeHEXColor: string; 
     syncID: string;
     pages: Page[];
     currentUserRead: boolean;
     tags: DocumentTag[];
     notes: NoteData[] =  new Array<NoteData>();
     lotFound: boolean= true;
     linkedDocuments: Document[];
     attachedDocuments: Document[];
     toRecipients: User[];
     groupId: number;
     organizationId: number; 
     groupName: string;
     groupTypeName: string;
     groupTypeOrderIndex: number;
     currentTraceId: string= '';
     partOfTrace: boolean = true;
     isTemplate: boolean = false;
     isDummy: boolean = false;
}
