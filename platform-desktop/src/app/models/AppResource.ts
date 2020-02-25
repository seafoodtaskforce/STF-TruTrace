import { IdentifiableEntity } from './identifiableEntity';

export class AppResource extends IdentifiableEntity {
    key: string;
    locale: string;
    type: string;
    subType: string;
    value: string;
    platfrom:string;
}