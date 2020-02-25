import { Component, OnInit } from '@angular/core';
import { NotificationData } from '../../../models/notificationData';

import {BaMsgCenterService} from './baMsgCenter.service';
import { GlobalState } from '../../../global.state';
import {SimpleTimer} from 'ng2-simple-timer';

@Component({
  selector: 'ba-msg-center',
  providers: [BaMsgCenterService],
  styleUrls: ['./baMsgCenter.scss'],
  templateUrl: './baMsgCenter.html'
})
export class BaMsgCenter implements OnInit {

  public notifications: Array<Object> = new Array<Object>();
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

  constructor(private _state: GlobalState, private _baMsgCenterService:BaMsgCenterService) {
    // this.notifications = this._baMsgCenterService.getNotifications();
    this.messages = this._baMsgCenterService.getMessages();
    this.loggedName = localStorage.getItem('username');
  }




  ngOnInit() {
		this.st.newTimer('1sec',30);
		this.subscribeTimer0();

  }

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

                const myNotification: any = {
                  name: notification.auditData.actor.name,
                  text: notification.auditData.actor.name + ' ' + message,
                  time: notification.creationTimestamp,
                };
                this.notifications.push(myNotification);

            }
      },
      error => console.log('Server Error'),
    );

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
    // return this.SERVER_URI_LOCAL;
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

    if(language === 'Bahasa'){
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

}
