import { ProfileEntity } from './profileEntity';
import { GroupType } from './groupType';
import { User } from './user';
import {DocumentType} from './documentType'

export class Group extends ProfileEntity {
    parentId: number;
    childId: number;
    organizationId: number;
    groupType: GroupType = new GroupType();
    // Additional Organization Data
    businessIDNumber : string;
	legalBusinessName : string;
	businessAddress : string;
    gpsCoordinates : string;
    emailAddress : string;
    verified : boolean;

    subGroups: Group[];
    users: User[]= new Array<User>();;

    allowedDocTypes: DocumentType[] = new Array<DocumentType>();
    
}
