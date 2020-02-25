import { ProfileEntity } from './../profileEntity';
import { UserContact } from './../userContact';
import { Group } from './../group';
import { Organization } from './../organization';
import { PasswordCredentials } from './../passwordCredentials';

export class UserAdmin extends ProfileEntity {
    firstName: string='';
    lastName:  string='';
    username:  string='';
    email:  string='';
    cellnumber:  string='';
    organization:  string='';
    userType:  string='';
    resourceType:  string='';
    resourceName:  string='';
    permissions:  string='';
    role:   string='';
}