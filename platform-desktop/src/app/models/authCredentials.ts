
import { UserCredentials } from './userCredentials';
import { IdentifiableEntity } from './identifiableEntity';
import { PasswordCredentials } from './passwordCredentials';
import { UserContact } from './UserContact';

export class AuthCredentials extends IdentifiableEntity {
    credentials: PasswordCredentials;
    roles: any[];
    contactInfo: UserContact;
    name: string;
}
