import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

// Data Model
import { AuthRequest } from '../../models/authRequest';
import { User } from '../../models/user';


// import global data
import * as AppGlobals from '../../config/globals';
import { ServerUtils } from '../../utils/server.utils';
//import config from "../../../assets/server.json";
declare var require: any;


@Injectable()
export class LoginService {

  readonly config = require("../../../assets/server.json");
  readonly AUTHENTICATE_URL: string = '/security/authenticate';
  requestBody: AuthRequest;

  /**
   * 
   * @param http the HTTP request structure
   */
  constructor(private http: Http) {
    // add the server URL to the session data
    localStorage.setItem(ServerUtils.BACK_END_SERVER_URL, this.config.server.accessUrl);
    console.log('[Login Service] SERVER URL <constructor> '.concat(JSON.stringify(this.config.server.accessUrl)));

  }

  getCredentials(user: User, inPassword: string): Observable<Response> {
    this.requestBody = { 
        username: user.name.toLowerCase(),
        password: inPassword,
        requestOrigin: 'Web Browser',
    };

    let headers = new Headers({ 'Content-Type': 'application/json' });
    // headers.append('user-name', user.name);
    let options = new RequestOptions({ headers: headers });

    return this.http.post(this.getServerURI().concat(this.AUTHENTICATE_URL), this.requestBody, options)
        .map( (response: Response) => {
            console.log('[Login Service] POST RESTFUL '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  getServerURI() {
    return localStorage.getItem(ServerUtils.BACK_END_SERVER_URL)
    //return AppGlobals.SERVER_URI;
  }
}
