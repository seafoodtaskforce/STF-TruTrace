import { IdentifiableEntity } from './identifiableEntity';
import { User } from './user';

export class LookupEntity extends IdentifiableEntity {
    name: string;
    value: string;
}
