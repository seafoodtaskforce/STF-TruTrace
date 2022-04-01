import { IdentifiableEntity } from './identifiableEntity';

export class UserContact extends IdentifiableEntity {
    emailAddress: string='';
    cellNumber: string='';
    firstName: string='';
    lastName: string='';
    activated: boolean=false;
    verified: boolean=false;
}
