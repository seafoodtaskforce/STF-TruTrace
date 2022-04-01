import { LookupEntity } from './../lookupEntity';

export class OrganizationAdmin extends LookupEntity {
    organizationName: string;
    description:string;
    userNumber:  number;
    // Additional Organization Data
    businessIDNumber : string;
    legalBusinessName : string;
    businessAddress : string;
    gpsCoordinates : string;
    emailAddress : string;
    verified : string;
    activated : string;


    organizationType:string;

}