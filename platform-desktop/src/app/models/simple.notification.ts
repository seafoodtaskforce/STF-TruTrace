
import { Document } from '../models/document';

export class SimpleNotification {
    name: string;
    text: string;
    time: string;
    docSyncID : string;
    item: Document;
}