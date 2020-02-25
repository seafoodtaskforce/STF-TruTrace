import { ProfileEntity } from './profileEntity';
import { GroupType } from './groupType';
import { User } from './user';
import {DocumentType} from './documentType'

export class Group extends ProfileEntity {
    parentId: number;
    childId: number;
    organizationId: number;
    groupType: GroupType = new GroupType();

    subGroups: Group[];
    users: User[];

    allowedDocTypes: DocumentType[] = new Array<DocumentType>();
    
}
