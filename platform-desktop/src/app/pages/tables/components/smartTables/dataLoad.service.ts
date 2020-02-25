import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { User } from '../../../../models/user';
import { UserAdmin } from '../../../../models/admin/userAdmin';
import { GroupType } from '../../../../models/groupType';
import { DocumentType } from '../../../../models/documentType';

import { Group } from '../../../../models/group';


import { ScrollToService, ScrollToConfigOptions } from '@nicky-lenaers/ngx-scroll-to';

// import global data
import * as AppGlobals from '../../../../config/globals';

@Injectable()
export class DataLoadService {

  // 
  // user admin
  readonly ALL_USER_URL: string = this.getServerURI().concat('/user/fetchall');
  readonly UPDATE_USER_URL: string = this.getServerURI().concat('/user/update');
  readonly CREATE_USER_URL: string = this.getServerURI().concat('/user/update');

  // 
  // Organization Type admin
  readonly ALL_ORGANIZATION_TYPE_URL: string = this.getServerURI().concat('/organization/fetchallgrouptypes');
  readonly ALL_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/fetchallgrouporganizations');
  readonly ALL_DOC_TYPES_URL: string = this.getServerURI().concat('/document/alltypes');
  readonly UPDATE_ALLOWED_DOCS_GROUP_TYPE: string = this.getServerURI().concat('/organization/allowedDocs');
 

  constructor(private http: Http, private _scrollToService: ScrollToService) {

  }



  getServerURI() {
    return AppGlobals.SERVER_URI;
  }

  /**
   * Get all the document types from the backend
   */
  getAllDocTypes(): Observable<DocumentType[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL DOC TYPES RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.ALL_DOC_TYPES_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Get all the users fron the backend
   */
  getAllUsers(isSuperAdmin: boolean): Observable<User[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    if(isSuperAdmin === true){
      headers.append('user-type', 'super-admin');
    }
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL USERS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.ALL_USER_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Update the given user
   * @param user Update the 
   */
  updateUser(user: User) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_USER_URL;
    let body = JSON.stringify(user);
    console.log('[Data Load Service User Update '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(user)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Update User '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  updateAllowedDocs(allowedDocs:string, groupTypeId: number) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    headers.append('groupId', '' + groupTypeId);
    headers.append('allowedDocs', ''.concat(allowedDocs));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_ALLOWED_DOCS_GROUP_TYPE;
    
    return this.http.post(URL_TO_CALL, '', options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Update Allowed Docs '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

    /**
   * Get all the group types fron the backend
   */
  getAllOrganizationTypes(): Observable<GroupType[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL ORGANIZATION TYPES RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.ALL_ORGANIZATION_TYPE_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Get all the group types fron the backend
   */
  getAllOrganizationGroups(): Observable<Group[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL ORGANIZATION GROUPS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.ALL_GROUP_ORGANIZATION_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }


}

