import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { NotificationData } from '../../../models/notificationData';
import { SimpleNotification } from '../../../models/simple.notification';

import {BaMsgCenterService} from './baMsgCenter.service';
import { GlobalState } from '../../../global.state';
import {SimpleTimer} from 'ng2-simple-timer';
import { InterComponentDataService } from "../../../utils/inter.component.data.service";

import { Router } from '@angular/router'; 
// import global data
import * as AppGlobals from '../../../config/globals';
import { User } from 'app/models/user';

@Component({
  selector: 'ba-msg-center',
  providers: [BaMsgCenterService],
  styleUrls: ['./baMsgCenter.scss'],
  templateUrl: './baMsgCenter.html'
})
export class BaMsgCenter implements OnInit {

  public notifications: Array<SimpleNotification> = new Array<SimpleNotification>();
  public messages: Array<Object>;
  public backEndNotifications: NotificationData[];
  ticks: number = 0;
  st: SimpleTimer = new SimpleTimer();
  public isScrolled: boolean = false;
  public isMenuCollapsed: boolean = false;
  serverURI: string;
  //@SessionStorage() username: string;
  loggedName: string;
  language:string = 'English';

   timer0Id: string;
     /**
   * Notification Session ID for Document Detail
   */
  notificationbasedDcumentSessionId : string = null;
   
  /**
   * Constructor
   * @param _state  - the global state that sholds all the main data
   * @param _baMsgCenterService - top part of the UI with messaging service for notifications
   * @param _documentDetailTrigger -
   * @param router - routing for the application
   */
  constructor(private _state: GlobalState, private _baMsgCenterService:BaMsgCenterService,
              protected _documentDetailTrigger: InterComponentDataService, private router : Router) {
    //
    // Init data
    let user: User;
    user = JSON.parse(localStorage.getItem('user'));
    this.loggedName = localStorage.getItem('username');
    this.messages = this._baMsgCenterService.getMessages();
  }

  ngOnInit() {
		this.st.newTimer('1sec',30);
    this.subscribeTimer0();
  }

  /**
   * Get all the notifications
   */
  getAllNotifications() {
    this._baMsgCenterService.getAllNotifications().subscribe(
      data => { 
        if(data == null) return;
        this.backEndNotifications = data;
            console.log('[Msg Center Component] GET ALL NOTIFICATIONS RESTFUL '
                .concat(JSON.stringify(this.backEndNotifications)));
            // extract 
            for (const notification of this.backEndNotifications) {
                //
                // additional initialization
                var message;

                // get the message
                if(notification.auditData.action == 'DOCUMENT_SUBMIT'){
                  message = 'New Document Submission';
                }
                if(notification.auditData.action == 'DOCUMENT_REJECT'){
                  message = 'Document Has Been Rejected';
                }
                if(notification.auditData.action == 'DOCUMENT_ACCEPT'){
                  message = 'Document Has Been Accepted';
                }
                if(notification.auditData.action == 'DOCUMENT_RESUBMIT'){
                  message = 'Document Has Been Resubmitted';
                }
                if(notification.auditData.action == 'NOTIFICATION_INDIVIDUAL'){
                  message = 'New Notification: '.concat(notification.notificationText);
                }

                const myNotification: SimpleNotification = {
                  name: notification.auditData.actor.name,
                  text: notification.auditData.actor.name + ' ' + message,
                  time: notification.creationTimestamp,
                  docSyncID : notification.auditData.itemId,
                  item : notification.item
                };
                this.notifications.push(myNotification);

            }
      },
      error => console.log('Server Error'),
    );

  }

  /**
   * Remove the clicked on notification
   * @param clickedNotification  - the clicked notification to remove
   */
  removeNotification(clickedNotification : SimpleNotification){
    let index = this.notifications.indexOf(clickedNotification);
    if(index != -1){
      console.log('[Msg Center Component] Removing Notification '
        .concat(JSON.stringify(this.notifications[index])));
      this.notifications.splice(index, 1);
      
    }
  }

  subscribeTimer0() {
			// Subscribe if timer Id is undefined
			this.timer0Id = this.st.subscribe('1sec', () => this.timer0callback());
			console.log('timer 0 Subscribed.');
   		console.log(this.st.getSubscription());
	}
  	timer0callback() {
      this.ticks++;
      this.getAllNotifications();
  }
    
  getServerURI() {
    return this._baMsgCenterService.getServerURI();
  }

  public getLanguage(){
    return this.language;
  }

  public changeLanguage(language){
    this.language = language;

  }

  public getCurrentFlag(){
    return this.getFlag(this.language);
  }

  public isCurrentFlag(language){
    if(this.language === language){
      return true;
    }else{
      return false;
    }
  }

  public getFlag(language){
    if(language === 'English'){
      return "flag-icon flag-icon-us flag-icon-squared";
    }

    if(language === 'Thai'){
      return "flag-icon flag-icon-th flag-icon-squared";
    }

    
    if(language === 'Vietnamese'){
      return "flag-icon flag-icon-vi flag-icon-squared";
    }

    if(language === 'Spanish'){
      return "flag-icon flag-icon-es flag-icon-squared";
    }

    if(language === 'Bahasa'){
      return "flag-icon flag-icon-id flag-icon-squared";
    }

    if(language === 'Hindi'){
      return "flag-icon flag-icon-in flag-icon-squared";
    }

    if(language === 'Telugu'){
      return "flag-icon flag-icon-in flag-icon-squared";
    }

  }

   /**
   * Get the highlight color for the specific hightlight for the menu
   * @param id - the document id to highlight
   */
  public getBackgroundColorHighlight(language: string) {
    if (this.isCurrentFlag(language)) {
      return '#5dd2ff';
    }else {
      return '#fff';
    }
  }

  /**
   * Notification clicked handler
   * @param msg -  the message for the notification
   */
  notificationClicked(msg: SimpleNotification) {
    console.log('[Msg Center Component] <start> Triggering Session ID: '
                .concat(msg.docSyncID));

        console.log('[Msg Center Component] Current Route '.concat(this.router.url));
    //this._documentDetailTrigger.showDocumentDetails(sessionId) 
    if(this.router.url.includes(AppGlobals.DOCUMENTS_PAGE_ROUTE )) {
      console.log('[Msg Center Component] <actual> Current Route '.concat(this.router.url));
      this.removeNotification(msg);
      this._documentDetailTrigger.showDocumentDetailsMessage.next(msg.item);
      
    } else {
      console.log('[Msg Center Component] <actual> Current Route '.concat(this.router.url));
      this._documentDetailTrigger.showDocumentDetailsMessage.next(AppGlobals.EMITTER_SEED_VALUE);
    }
  }

  /**
   * Clear all notifications
   */
  clearNotifications(){
    this.notifications = [];
  }

}
