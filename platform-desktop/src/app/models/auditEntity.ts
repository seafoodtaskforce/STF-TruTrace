import { User } from './user';

export class AuditEntity {
     id: number;
     actor: User;
     userType: string;
     action: string;
     itemType: string;
     itemId: string; 
     fieldName: string;
     prevValue: string;
     newValue: string;
     timestamp: string;
}