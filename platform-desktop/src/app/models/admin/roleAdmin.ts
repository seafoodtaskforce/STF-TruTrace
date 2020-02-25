import { LookupEntity } from './../lookupEntity';
import { DocumentTypeAdmin } from './documentTypeAdmin';


export class RoleAdmin extends LookupEntity {
    roleName: string;
    docTypes:  DocumentTypeAdmin[];
    isActive:boolean =false;
}