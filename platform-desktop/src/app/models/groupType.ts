import { LookupEntity } from './lookupEntity';
import { User } from './user';
import { DocumentType } from './documentType';

export class GroupType extends LookupEntity {
    hexColorCode: string;
    orderIndex: number;
    allowedDocTypes: DocumentType[];
}
