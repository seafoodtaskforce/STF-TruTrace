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
import { Group } from 'app/models/group';
//import config from "../../../assets/server.json";
declare var require: any;


@Injectable()
export class RegisterService {

  readonly config = require("../../../assets/server.json");
  readonly REGISTER_USER_URL: string = '/user/register';
  readonly REGISTER_ORG_GROUP_URL: string = '/organization/registergrouporganization';
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

  registerUser(user: User, organization: string): Observable<Response> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('group-name', organization);
    // headers.append('user-name', user.name);
    let options = new RequestOptions({ headers: headers });
    let body = JSON.stringify(user);
    return this.http.post(this.getServerURI().concat(this.REGISTER_USER_URL), body, options)
        .map( (response: Response) => {
            console.log('[Registration Service <user> ] POST RESTFUL '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Registration Error' ));
  }

  registerOrgGroup(organization: Group, stageName : string): Observable<Response> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('stage-name', stageName);
    headers.append('user-name', localStorage.getItem('username'));
    // headers.append('user-name', user.name);
    let options = new RequestOptions({ headers: headers });
    let body = JSON.stringify(organization);
    return this.http.post(this.getServerURI().concat(this.REGISTER_ORG_GROUP_URL), body, options)
        .map( (response: Response) => {
            console.log('[Registration Service <organization> ] POST RESTFUL '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Registration Error' ));
  }

  getServerURI() {
    return localStorage.getItem(ServerUtils.BACK_END_SERVER_URL)
    //return AppGlobals.SERVER_URI;
  }
}
