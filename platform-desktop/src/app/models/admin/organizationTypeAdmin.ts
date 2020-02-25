import { LookupEntity } from './../lookupEntity';

export class OrganizationTypeAdmin extends LookupEntity {
    organizationType: string;
    hexColorCode: string;
    orderIndex: number;
    organizationsNumber: number;
    allowedDocsNumber: number;
}