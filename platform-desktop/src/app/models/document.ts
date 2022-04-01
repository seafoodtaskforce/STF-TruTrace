import { Page } from './page';
import { DocumentTag } from './documentTag';
import { DocumentType } from './documentType';
import { NoteData } from './noteData';
import { User } from './user';
import { DynamicFieldData } from './dynamicFieldData';

export class Document {
     public static readonly TYPE_DESIGNATION_PASSTHROUGH = "Passthrough";
     public static readonly TYPE_DESIGNATION_PROFILE = "Profile";

     public static readonly STATUS_REJECTED = "REJECTED";
     public static readonly STATUS_RESUBMITTED = "RESUBMITTED";
     public static readonly STATUS_SUBMITTED = "SUBMITTED";
     public static readonly STATUS_PENDING = "PENDING";
     public static readonly STATUS_DRAFT = "DRAFT";
     public static readonly STATUS_ACCEPTED = "ACCEPTED";

     public static readonly DOCUMENT_DESIGNATION_ATTACHED = "Backup Docs";
     public static readonly DOCUMENT_DESIGNATION_LINKED = "Linked Docs";

     id: number=0;
     status:string;
     documentType: string;
     type:DocumentType;
     owner: string;
     creationTimestamp: string;
     updationTimestamp: string;
     updationServerTimestamp: string;
     creationFilterTimestamp: string;
     TypeHEXColor: string; 
     syncID: string='0';
     pages: Page[];
     currentUserRead: boolean;
     tags: DocumentTag[];
     notes: NoteData[] =  new Array<NoteData>();
     
     linkedDocuments: Document[];
     attachedDocuments: Document[];
     toRecipients: User[];
     groupId: number;
     organizationId: number; 
     groupName: string;
     groupTypeName: string;
     groupTypeValue: string;
     groupTypeOrderIndex: number;
     
     isTemplate: boolean = false;
     isDummy: boolean = false;
     dynamicFieldData: DynamicFieldData[] =  new Array<DynamicFieldData>();
     gpsLocation: string='';
     orgGPSLocation: string = '';
     isLocked: boolean=false;

     //
     // Used exclusevly by desktop code
     currentTraceId: string= '';
     partOfTrace: boolean = true;
     lotFound: boolean= true;
     partOfFeed: boolean = true;
}
