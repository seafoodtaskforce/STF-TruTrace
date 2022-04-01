import { User } from './user';
import { AuditEntity } from './auditEntity';
import { Document } from './document';

export class NotificationData {
     id: number;
     user: User;

     notificationType: string;
     creationTimestamp: string;
     notificationTimestamp: string;
     creationFilterTimestamp: string;
     auditData: AuditEntity; 
     notificationText: string;
     notificationDescription: string;
     item: Document;
}