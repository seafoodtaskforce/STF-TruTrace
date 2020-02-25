import { LookupEntity } from './../lookupEntity';
import { GroupType } from './../GroupType';

export class DocumentTypeOrgTypeAdmin extends LookupEntity {
    docTypeName: string;
    docName:string;
    permissions:  string="";
    roleName: string;
    isActiveinOrg:boolean;
    org:GroupType;
}