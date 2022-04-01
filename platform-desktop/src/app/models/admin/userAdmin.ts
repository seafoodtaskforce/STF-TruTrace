import { ProfileEntity } from './../profileEntity';

export class UserAdmin extends ProfileEntity {
    firstName: string='';
    lastName:  string='';
    username:  string='';
    email:  string='';
    cellnumber:  string='';
    verified: string='';
    activated: string = '';
    organization:  string='';
    userType:  string='';
    resourceType:  string='';
    resourceName:  string='';
    permissions:  string='';
    role:   string='';
}