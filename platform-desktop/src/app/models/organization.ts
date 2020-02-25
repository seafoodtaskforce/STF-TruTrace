import { ProfileEntity } from './profileEntity';
import { User } from './user';
import { Group } from './group';

export class Organization extends ProfileEntity {
    users: User[];
    subGroups: Group[];
}
