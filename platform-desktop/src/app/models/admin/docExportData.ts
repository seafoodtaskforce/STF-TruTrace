import { result } from 'lodash';
import { IdentifiableEntity } from '../identifiableEntity';

export class DocExportData extends IdentifiableEntity {
    stage: string='';
    organization:  string='';
    owner:  string='';
    recipient:  string='';
    docType:  string='';
    docInfoDefinition: string='';
    docInfoValue: string = '';
    creationDate: string = '';
    orgGPS: string = '';
    docGPS: string = '';

    constructor(){

        super();
        this.stage='';
        this.organization='';
        this.owner='';
        this.recipient='';
        this.docType='';
        this.docInfoDefinition='';
        this.docInfoValue = '';
        this.creationDate = '';
        this.orgGPS = '';
        this.docGPS = '';
   }

   public static getCSVRowHeader() {
       var result: string  = "";
       result = 'Doc ID, Org Type, Organization, Owner, Recipient, '
                + 'Doc Type, Doc Info Definition, Doc Info Value, Creation Date, Org GPS, Doc GPS'; 

       return result;
   }
}