import { ProfileEntity } from './profileEntity';
import { UserContact } from './userContact';
import { Group } from './group';
import { Organization } from './organization';
import { PasswordCredentials } from './passwordCredentials';
import { AppResource } from './AppResource';
import { Role } from './role';
import { DynamicFieldDefinition } from './dynamicFieldDefinition';


export class User extends ProfileEntity {
    credentials: PasswordCredentials = new PasswordCredentials();
    id: number;
    name: string = "";
    userGroups: Group[];
    userOrganizations: Organization[];
    contactInfo:UserContact = new UserContact();
    roles: Role[];
    appResources: AppResource[];
    dynamicFieldDefinitions: DynamicFieldDefinition[];
}
