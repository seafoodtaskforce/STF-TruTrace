import { LookupEntity } from './lookupEntity';
import { User } from './user';
import { DocumentType } from './documentType';

export class Role extends LookupEntity {
    // addtional data

    public static readonly ROLE_NAME_SUPER_ADMIN = "Super Admin";
    public static readonly ROLE_NAME_MATRIX_ADMIN = "Matrix Admin";
    public static readonly ROLE_NAME_ORG_ADMIN = "Org Admin";
    public static readonly ROLE_NAME_USER = "User";

}