import { LookupEntity } from './../lookupEntity';

export class DocumentTypeAdmin extends LookupEntity {
    docTypeName: string;
    permissions:  string;
    roleName: string;
}