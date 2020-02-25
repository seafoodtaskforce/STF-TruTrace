import { IdentifiableEntity } from "./identifiableEntity";

export class NoteData extends IdentifiableEntity {

    note: string;
    creationTimestamp: string;
    owner: string;
}