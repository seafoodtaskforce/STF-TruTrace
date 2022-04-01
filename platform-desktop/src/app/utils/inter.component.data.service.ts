import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Document } from '../models/document';

// import global data
import * as AppGlobals from '../config/globals';

@Injectable()
export class InterComponentDataService {

    showDocumentDetailsMessage = new BehaviorSubject<Document>(AppGlobals.EMITTER_SEED_VALUE);
    //docSessionId = this.showDocumentDetailsMessage.asObservable();
  
  // currentMessage = this.showDocumentDetailsMessage.asObservable();

  constructor() { 
  }

  /** 
  showDocumentDetails(docSessionId: string) {
    console.log('[InterComponentDataService] <inter comm> Triggering Session ID: '
    .concat(docSessionId));
    //this.showDocumentDetailsMessage.next(docSessionId);
  }
  */

  showDocumentNotificationDetails(item: Document) {
    console.log('[InterComponentDataService] <inter comm> Triggering Item Document: '
    .concat(item.syncID));
    this.showDocumentDetailsMessage.next(item);
  }

}