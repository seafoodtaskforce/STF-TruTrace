import { LookupEntity } from './../lookupEntity';

export class NotificationAdmin extends LookupEntity{
    notificationScope: string;
    notificationType: string;
    notificationTarget: string;
    notificationText: string;
    notificationDescription: string;
    creationTimestamp: string;
    notificationTimestamp: string;
}