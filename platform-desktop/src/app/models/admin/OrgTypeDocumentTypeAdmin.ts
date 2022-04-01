import { LookupEntity } from './../lookupEntity';
import { GroupType } from './../GroupType';

export class OrgTypeDocumentTypeAdmin extends LookupEntity {
    docTypeName: string;
    docName:string;
    permissions:  string="";
    roleName: string;
    isActiveinOrg:boolean;
    org:GroupType;
}