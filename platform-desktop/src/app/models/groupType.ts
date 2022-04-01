import { LookupEntity } from './lookupEntity';
import { DocumentType } from './documentType';

export class GroupType extends LookupEntity {
    hexColorCode: string;
    orderIndex: number;
    allowedDocTypes: DocumentType[];
}
