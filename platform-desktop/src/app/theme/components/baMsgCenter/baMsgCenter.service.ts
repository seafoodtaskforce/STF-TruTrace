import {Injectable} from '@angular/core'
import {Observable} from 'rxjs/Rx';

import { Http, Response, Headers, RequestOptions } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { NotificationData } from '../../../models/notificationData';

// import global data
import * as AppGlobals from '../../../config/globals';

@Injectable()
export class BaMsgCenterService {

  readonly ALL_NOTIFICATIONS_URL: string = this.getServerURI().concat('/notification/fetchall');

   constructor(private http: Http) {

  }

  private _notifications = [
    {
      name: 'ppaweska',
      text: 'ppaweska posted new MCPD Document.',
      time: '1 min ago'
    },
    {
      name: 'ppaweska',
      text: 'ppaweska posted new MCPD Document.',
      time: '2 hrs ago'
    },
    {
      name: 'rhughes',
      text: 'rhughes attached a Document',
      time: '1 day ago'
    },
    {
      name: 'rhughes',
      text: 'rhughes posted new Fishmeal Lot Treacability Document.',
      time: '2 days ago'
    },
  ];

  private _messages = [
    {
      name: 'Nasta',
      text: 'Message 1...',
      time: '1 min ago'
    },
    {
      name: 'Vlad',
      text: 'Message 2...',
      time: '2 hrs ago'
    },
    {
      name: 'Kostya',
      text: 'Message 3...',
      time: '10 hrs ago'
    },
    {
      name: 'Andrey',
      text: 'Message 4...',
      time: '1 day ago'
    },
    {
      name: 'Nasta',
      text: 'Message 5...',
      time: '1 day ago'
    },
    {
      name: 'Kostya',
      text: 'Message 6...',
      time: '2 days ago'
    },
    {
      name: 'Vlad',
      text: 'Message 7...',
      time: '1 week ago'
    }
  ];


      /**
   * Get all the docuemnts from the backend
   * Add header option for username infromation to specify which user is getting the data
   */
  public getAllNotifications(): Observable<NotificationData[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Msg Center Service] GET ALL NOTIFICATIONS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.ALL_NOTIFICATIONS_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }
  public getMessages():Array<Object> {
    return this._messages;
  }

  public getNotifications():Array<Object> {
    return this._notifications;
  }

  getServerURI() {
    return AppGlobals.SERVER_URI;
  }
}
