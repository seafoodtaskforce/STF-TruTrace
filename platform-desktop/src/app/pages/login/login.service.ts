import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

// Data Model
import { AuthRequest } from '../../models/authRequest';
import { AuthCredentials } from '../../models/AuthCredentials';
import { User } from '../../models/user';


// import global data
import * as AppGlobals from '../../config/globals';

@Injectable()
export class LoginService {

  readonly AUTHENTICATE_URL: string = this.getServerURI().concat('/security/authenticate');
  requestBody: AuthRequest;

  /**
   * 
   * @param http the HTTP request structure
   */
  constructor(private http: Http) {

  }

  getCredentials(user: User, inPassword: string): Observable<Response> {
    this.requestBody = { 
        username: user.name,
        password: inPassword,
        requestOrigin: 'Web Browser',
    };

    let headers = new Headers({ 'Content-Type': 'application/json' });
    // headers.append('user-name', user.name);
    let options = new RequestOptions({ headers: headers });

    return this.http.post(this.AUTHENTICATE_URL, this.requestBody, options)
        .map( (response: Response) => {
            console.log('[Login Service] POST RESTFUL '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  getServerURI() {
    return AppGlobals.SERVER_URI;
  }
}
