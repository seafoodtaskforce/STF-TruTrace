import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { User } from '../../models/user';
import { GroupType } from '../../models/groupType';
import { DocumentType } from '../../models/documentType';
import {DocumentTag} from "../../models/documentTag"
import {NotificationData} from "../../models/notificationData"
import {AppResource} from "../../models/AppResource"



import { Group } from '../../models/group';


import { ScrollToService, ScrollToConfigOptions } from '@nicky-lenaers/ngx-scroll-to';

// import global data
import * as AppGlobals from '../../config/globals';
import { OrganizationStage } from 'app/models/OrganizationStage';

@Injectable()
export class DataLoadService {

  // 
  // user admin
  readonly ALL_USER_URL: string = this.getServerURI().concat('/user/fetchall');
  readonly UPDATE_USER_URL: string = this.getServerURI().concat('/user/update');
  readonly CREATE_USER_URL: string = this.getServerURI().concat('/user/create');

  // 
  // Organization Type admin
  readonly ALL_ORGANIZATION_TYPE_URL: string = this.getServerURI().concat('/organization/fetchallgrouptypes');
  
  readonly ALL_DOC_TYPES_URL: string = this.getServerURI().concat('/document/alltypes');
  readonly UPDATE_ALLOWED_DOCS_GROUP_TYPE: string = this.getServerURI().concat('/organization/allowedDocs');

  //
  // Organization Group Admin
  readonly ALL_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/fetchallgrouporganizations');
  readonly CREATE_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/creategrouporganization');

  //
  // Stages Admin
  readonly ALL_ORGANIZATION_STAGES_URL: string = this.getServerURI().concat('/organization/fetchstages?org_id=[:1]');
  readonly SAVE_ORGANIZATION_STAGES_URL: string = this.getServerURI().concat('/organization/createstages');

  //
  // Tag Admin
  readonly GET_ALL_TAGS_URL: string = this.getServerURI().concat('/tag/fetchall');
  readonly CREATE_TAG_URL: string = this.getServerURI().concat('/tag/create');

  //
  // Notifications Admin
  readonly GET_ALL_NOTIFICATIONS_URL: string = this.getServerURI().concat('/notification/fetchallfiltered');
  readonly CREATE_NOTIFICATION_URL: string = this.getServerURI().concat('/notification/create');

  //
  // Document Type Admin
  readonly CREATE_DOC_TYPE_URL: string = this.getServerURI().concat('/document/createdoctype');

  //
  // REsource Admin
  readonly CREATE_APP_RESOURCE_URL: string = this.getServerURI().concat('/server/resource');
  readonly DELETE_APP_RESOURCE_URL: string = this.getServerURI().concat('/server/resource/[:1]');

  /**
   * 
   * @param http  - RESTFul connectivity
   * @param _scrollToService - Service to captrure scrolling gestures
   */
  constructor(private http: Http, private _scrollToService: ScrollToService) {

  }



  /**
   * Get the REST Server URI
   */
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

   /**
   * Update the given user
   * @param user Update the 
   */
  createUser(user: User) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_USER_URL;
    let body = JSON.stringify(user);
    console.log('[Data Load Service User Update '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(user)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create User '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

   /**
   * Create the given Group Organization
   * @param organizationGroup the organization group being created
   */
  createGroupOrganization(organizationGroup: Group) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_GROUP_ORGANIZATION_URL;
    let body = JSON.stringify(organizationGroup);
    console.log('[Data Load Service Organization CREATE '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(organizationGroup)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create Organization Group '.concat(JSON.stringify(response.json())));
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

  /**
   * Get all the tags from the backend
   */
  getAllTags(): Observable<DocumentTag[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL TAGS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.GET_ALL_TAGS_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Create the given Tag
   * @param newTag the tag being created
   */
  createNewTag(newTag: DocumentTag) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_TAG_URL;
    let body = JSON.stringify(newTag);
    console.log('[Data Load Service Tag Create '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(newTag)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create New Tag '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Get all the notifications from the backend
   */
  getAllNotifications(): Observable<NotificationData[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL NOTIFICATIONS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.GET_ALL_NOTIFICATIONS_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  createNewNotification(newNotification: NotificationData, scope:string, target:string, orgName:string){
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    headers.append('notification-scope', scope);
    headers.append('user-target', target);
    headers.append('group-target', orgName);
    headers.append('notification-text', newNotification.notificationText);
    headers.append('notification-description', newNotification.notificationDescription);
    
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_NOTIFICATION_URL;
    let body = JSON.stringify(newNotification);
    console.log('[Data Load Service Notification Create '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(newNotification)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create New Notification '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Get all the stages from the backend
   */
  getAllStages(): Observable<OrganizationStage[]> {
    let user: User;
    user = JSON.parse(localStorage.getItem('user'));

    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    const URL_TO_CALL = this.ALL_ORGANIZATION_STAGES_URL
    .replace('[:1]', '' + user.userGroups[0].organizationId );

    console.log('[dataLoad Service (Admin)] GET STAGES '.concat(URL_TO_CALL));

    console.log('[dataLoad Service (Admin)] GET ALL STAGES RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(URL_TO_CALL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Create/Update the provided Stages/Organization Types
   * @param newStages the stages being created
   */
  createNewStage(newStages: OrganizationStage[]) {
    //
    // get necessary global data
    let user: User;
    user = JSON.parse(localStorage.getItem('user'));

    //
    // Get REST header data
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    headers.append('org-id', ''+ user.userGroups[0].organizationId);
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.SAVE_ORGANIZATION_STAGES_URL;
    let body = JSON.stringify(newStages);
    console.log('[Data Load Organization Stage Create/Save '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(newStages)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Stage Create/Save '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

    /**
   * Create/Update the provided Stages/Organization Types
   * @param newStages the stages being created
   */
  createNewDocumentType(newDocType: DocumentType) {
    //
    // get necessary global data
    let user: User;
    user = JSON.parse(localStorage.getItem('user'));

    //
    // Get REST header data
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_DOC_TYPE_URL;
    let body = JSON.stringify(newDocType);
    console.log('[Data Load Document Type Create/Save '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(newDocType)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Doc Type Create/Save '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Create new reources for the app
   * @param resource 
   */
  createNewResource(resource:AppResource){
//
    // get necessary global data
    let user: User;
    user = JSON.parse(localStorage.getItem('user')); 

    //
    // Get REST header data
    let headers = new Headers({ 'Content-Type': 'application/json;charset=UTF-8' });
    headers.append('user-name', localStorage.getItem('username'));

    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.CREATE_APP_RESOURCE_URL;
    let body = JSON.stringify(resource);
    console.log('[Data Load APP Resource Create/Save '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(resource)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Doc Type Create/Save '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Create new reources for the app
   * @param resource 
   */
  deleteResource(resource:AppResource){
        //
        // get necessary global data
        let headers = new Headers({ 'Content-Type': 'application/json' });
        headers.append('user-name', localStorage.getItem('username'));
        let options = new RequestOptions({ headers: headers });
        // create the URL
        const URL_TO_CALL = this.DELETE_APP_RESOURCE_URL
            .replace('[:1]', '' + resource.id );
    
        console.log('[Data Load Service] DELETE - App Resource '.concat(URL_TO_CALL));
    
        return this.http.delete(URL_TO_CALL, options)
            .map( (response: Response) => {
                console.log('[Data Load Service] DELETE - App Resource  '.concat(JSON.stringify(response.json())));
                return response;
            } )
            .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
      }

  



  


}

