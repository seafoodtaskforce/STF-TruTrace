import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { User } from '../../models/user';
import { GroupType } from '../../models/groupType';
import { DocumentType } from '../../models/documentType';
import { DynamicFieldType } from '../../models/dynamicFieldType';

import {DocumentTag} from "../../models/documentTag"
import {NotificationData} from "../../models/notificationData"
import {AppResource} from "../../models/AppResource"



import { Group } from '../../models/group';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';


import { ScrollToService, ScrollToConfigOptions } from '@nicky-lenaers/ngx-scroll-to';

// import global data
import * as AppGlobals from '../../config/globals';
import { OrganizationStage } from 'app/models/OrganizationStage';
import { DynamicFieldDefinition } from 'app/models/dynamicFieldDefinition';
import { ServerUtils } from 'app/utils/server.utils';
import { UserAdminTable } from './userAdmin/userAdmin.component';
import { RESTResponse, ResponseErrorData, ResponseIssue } from 'app/models/RestResponse';
import { OrganizationAdminTable } from './organizationAdmin';

@Injectable()
export class DataLoadService {

  // 
  // user admin
  readonly ALL_USER_URL: string = this.getServerURI().concat('/user/fetchall');
  readonly UPDATE_USER_URL: string = this.getServerURI().concat('/user/update');
  readonly CREATE_USER_URL: string = this.getServerURI().concat('/user/create');
  readonly UPDATE_USER_PROFILE_DATA_URL: string = this.getServerURI().concat('/user/updateprofile');
  readonly UPLOAD_USER_BATCH_AS_CSV_URL: string = this.getServerURI().concat('/user/batchupload');

  // 
  // Organization Type admin
  readonly ALL_ORGANIZATION_TYPE_URL: string = this.getServerURI().concat('/organization/fetchallgrouptypes');
  
  readonly ALL_DOC_TYPES_URL: string = this.getServerURI().concat('/document/alltypes');
  readonly UPDATE_ALLOWED_DOCS_GROUP_TYPE: string = this.getServerURI().concat('/organization/allowedDocs');

  //
  // Organization Group Admin
  readonly ALL_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/fetchallgrouporganizations');
  readonly CREATE_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/creategrouporganization');
  readonly UPDATE_GROUP_ORGANIZATION_URL: string = this.getServerURI().concat('/organization/updategrouporganization');
  readonly UPLOAD_GROUP_BATCH_AS_CSV_URL: string = this.getServerURI().concat('/organization/batchorgupload');

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
  readonly UPDATE_DOC_TYPE_URL: string = this.getServerURI().concat('/document/updatedoctype');

  //
  // REsource Admin
  readonly CREATE_APP_RESOURCE_URL: string = this.getServerURI().concat('/server/resource');
  readonly DELETE_APP_RESOURCE_URL: string = this.getServerURI().concat('/server/resource/[:1]');
  readonly GET_ALL_APP_RESOURCES_URL: string = this.getServerURI().concat('/server/resources');

  //
  // Dynamic field Definition Admin
  readonly CREATE_DYNAMIC_FIELD_DEFINITION_URL: string = this.getServerURI().concat('/document/createfielddefinition');
  readonly UPDATE_DYNAMIC_FIELD_DEFINITION_URL: string = this.getServerURI().concat('/document/updatefielddefinition');
  readonly GET_ALL_DYNAMIC_FIELD_DEFINITIONS_URL: string = this.getServerURI().concat('/document/fetchallfielddefinitions');
  readonly DELETE_DYNAMIC_FIELD_DEFINITION_URL: string = this.getServerURI().concat('/document/delete/dynamicfielddefinition/[:1]');
  readonly GET_ALL_DYNAMIC_FIELD_TYPES_URL: string = this.getServerURI().concat('/server/dynamicfieldtypes');

  //
  // File Uploads
  public csvUserBatchUploadFlag: boolean = false;
  public csvOrgBatchUploadFlag: boolean = false;

  /**
   * 
   * @param http  - RESTFul connectivity
   * @param _scrollToService - Service to captrure scrolling gestures
   */
  constructor(private http: Http, private _scrollToService: ScrollToService) {

  }

  //
  // CSV FIle Uploads 
  startCSVUserBatchUploadProcess(){
    this.csvUserBatchUploadFlag = true;
  }

  stopCSVUserBatchUploadProcess(){
    this.csvUserBatchUploadFlag = false;
  }

  startCSVOrgBatchUploadProcess(){
    this.csvOrgBatchUploadFlag = true;
  }

  stopCSVOrgBatchUploadProcess(){
    this.csvOrgBatchUploadFlag = false;
  }

  isUploadingCSVUserBatch(){
    return this.csvUserBatchUploadFlag;
  }

  isUploadingCSVOrgBatch(){
    return this.csvOrgBatchUploadFlag;
  }

   /**
    * Event handler for CSV Batch User File creation upload and coverstion in the server
    * @param file 
    * @param formData 
    * @param slimLoader 
    * @param thisService 
    * @param component 
    */
  onCSVOrgBatchFileUpload(file: string, formData: FormData, slimLoader: SlimLoadingBarService, thisService: DataLoadService, component: OrganizationAdminTable){
    //
    // init
    component.uploadOperationErrorsFlag =false;

    // headers
    let headers = new Headers({ 'Content-Type': 'multipart/form-data' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // usrl
    const URL_TO_CALL = this.UPLOAD_GROUP_BATCH_AS_CSV_URL;
    console.log('[Data Load Service] POST - Upload a CSV Org Batch File Upload URL'.concat(JSON.stringify(URL_TO_CALL)));
    console.log('[Data Load Service] POST - Upload a CSV Org Batch File Upload Options'.concat(JSON.stringify(options)));
    console.log('[Data Load Service] POST - Upload a CSV Org Batch File Upload FORM DATA'.concat(JSON.stringify(formData)));

    var oReq = new XMLHttpRequest();
    oReq.open("POST", URL_TO_CALL, true);
    oReq.onload = function(oEvent) {
      if (oReq.status == 200) {
        console.log('[Data Load Service] POST - Upload Org Batch CSV SUCCESS');
        //
        // fetch response data

        var restResponse: RESTResponse = JSON.parse(oReq.response);
        var newUsers: User[] = restResponse.data;
        var responseErrorDataItems: ResponseErrorData;;
        var errorMessages: ResponseIssue[];
        if (restResponse.errorData.length > 0) {
          //
          // We have errors

          // Data Items
          responseErrorDataItems = restResponse.errorData[0];
          // extract issues
          if(responseErrorDataItems.issues.length > 0){
            errorMessages = responseErrorDataItems.issues;
            // send it upstream
            component.uploadOperationErrorsFlag = true;
            component.errorMessages = errorMessages;
            component.showErrorToasterBackendCSV();
          } else {
            component.uploadOperationErrorsFlag = false;
          }
        }

        console.log('[Data Load Service] POST - Upload a NewDoc SUCCESS --->' + JSON.stringify(newUsers));
        slimLoader.complete();
        thisService.stopCSVOrgBatchUploadProcess();
        
        // add to the component
        component.getAllOrganizationGroups();
      } else {
        console.log('[Data Load Service] POST - Upload a NewDoc ERROR');
        component.uploadOperationErrorsFlag =true;
        thisService.stopCSVOrgBatchUploadProcess();
      }
    };

  oReq.send(formData);
    
    // build the post reuqest with all data
    /**
    return this.http.post(URL_TO_CALL, formData, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Upload a NewDoc '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
*/
  }

    /**
  * Event handler for CSV Batch User File creation upload and coverstion in the server
  * @param file 
  * @param formData 
  * @param slimLoader 
  * @param thisService 
  * @param component 
  */
    onCSVUserBatchFileUpload(file: string, formData: FormData, slimLoader: SlimLoadingBarService, thisService: DataLoadService, component: UserAdminTable){
      //
      // init
      component.uploadOperationErrorsFlag =false;
  
      // headers
      let headers = new Headers({ 'Content-Type': 'multipart/form-data' });
      headers.append('user-name', localStorage.getItem('username'));
      let options = new RequestOptions({ headers: headers });
      // usrl
      const URL_TO_CALL = this.UPLOAD_USER_BATCH_AS_CSV_URL;
      console.log('[Data Load Service] POST - Upload a CSV User Batch File Upload URL'.concat(JSON.stringify(URL_TO_CALL)));
      console.log('[Data Load Service] POST - Upload a CSV User Batch File Upload Options'.concat(JSON.stringify(options)));
      console.log('[Data Load Service] POST - Upload a CSV User Batch File Upload FORM DATA'.concat(JSON.stringify(formData)));
  
      var oReq = new XMLHttpRequest();
      oReq.open("POST", URL_TO_CALL, true);
      oReq.onload = function(oEvent) {
        if (oReq.status == 200) {
          console.log('[Data Load Service] POST - Upload User Batch CSV SUCCESS');
          //
          // fetch response data
  
          var restResponse: RESTResponse = JSON.parse(oReq.response);
          var newUsers: User[] = restResponse.data;
          var responseErrorDataItems: ResponseErrorData;;
          var errorMessages: ResponseIssue[];
          if (restResponse.errorData.length > 0) {
            //
            // We have errors
  
            // Data Items
            responseErrorDataItems = restResponse.errorData[0];
            // extract issues
            if(responseErrorDataItems.issues.length > 0){
              errorMessages = responseErrorDataItems.issues;
              // send it upstream
              component.uploadOperationErrorsFlag = true;
              component.errorMessages = errorMessages;
              component.showErrorToasterBackendCSV();
            } else {
              component.uploadOperationErrorsFlag = false;
            }
          }
  
          console.log('[Data Load Service] POST - Upload a NewDoc SUCCESS --->' + JSON.stringify(newUsers));
          slimLoader.complete();
          thisService.stopCSVUserBatchUploadProcess();
          
          // add to the component
          component.getAllUsers();
        } else {
          console.log('[Data Load Service] POST - Upload a NewDoc ERROR');
          component.uploadOperationErrorsFlag =true;
          thisService.stopCSVUserBatchUploadProcess();
        }
    };
  
    oReq.send(formData);
      
      // build the post reuqest with all data
      /**
      return this.http.post(URL_TO_CALL, formData, options)
          .map( (response: Response) => {
              console.log('[Data Load Service] POST - Upload a NewDoc '.concat(JSON.stringify(response.json())));
              return response;
          } )
          .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  */
    }

  /**
   * Get the REST Server URI
   */
  getServerURI() {
    //return AppGlobals.SERVER_URI;
    return localStorage.getItem(ServerUtils.BACK_END_SERVER_URL)
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
   * Get all the dynamic field types types from the backend
   */
  getAllDynamicFieldTypes(): Observable<DynamicFieldType[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] GET ALL Dynamic Field Definition Types RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.GET_ALL_DYNAMIC_FIELD_TYPES_URL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

    /**
   * Get all the dynamic field types types from the backend
   */
  getAllDynamicFieldDefinitions(): Observable<DynamicFieldDefinition[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] <getAllDynamicFieldDefinitions> GET ALL Dynamic Field Definition TYPES RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.GET_ALL_DYNAMIC_FIELD_DEFINITIONS_URL, options)
        .map( (res: Response) => {

          return res.json();
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Get all the resources from the backend
   */
  getAllServerResources(): Observable<AppResource[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[dataLoad Service (Admin)] <getAllServerResources> GET ALL Resources from Server '.concat(JSON.stringify(options)));

    return this.http.get(this.GET_ALL_APP_RESOURCES_URL, options)
        .map( (res: Response) => {

          return res.json();
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

     /**
   * Update the given user
   * @param user Update the 
   */
  createDynamicFieldDefinition(definition: DynamicFieldDefinition) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    let fullDefinition: DynamicFieldDefinition = new DynamicFieldDefinition();
    // create the URL
    const URL_TO_CALL = this.CREATE_DYNAMIC_FIELD_DEFINITION_URL;
    let body = JSON.stringify(definition);
    console.log('[Data Load Service Dynamic Field Definition '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(definition)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create Dynamic Field Definition '.concat(JSON.stringify(response.json())));
            fullDefinition = response.json()
            // add the definition to the list of definitions in memory
            
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  updateAppResource(resource : AppResource) {
    //
    // TODO
    return this.createAppResource(resource);
  }

  createAppResource(resource :AppResource) {
    //
    // TODO
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    let fullResource: AppResource = new AppResource();
    // create the URL
    const URL_TO_CALL = this.CREATE_APP_RESOURCE_URL;
    let body = JSON.stringify(resource);
    console.log('[Data Load Service Resource Definition '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(resource)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Create Dynamic Field Definition '.concat(JSON.stringify(response.json())));
            fullResource = response.json()
            // add the definition to the list of definitions in memory
            
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Update the given user
   * @param user Update the 
   */
  updateDynamicFieldDefinition(definition: DynamicFieldDefinition) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_DYNAMIC_FIELD_DEFINITION_URL;
    let body = JSON.stringify(definition);
    console.log('[Data Load Service Dynamic Field Definition '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(definition)));

    return this.http.put(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] PUT - Update Dynamic Field Definition '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  /**
   * Get all the users front the backend
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
  updateUserProfile(user: User) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_USER_PROFILE_DATA_URL;
    let body = JSON.stringify(user);
    console.log('[Data Load Service User Profile Update '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(user)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Update User Profile '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

   /**
   * Create the given user
   * @param user Create the user based on the provided data
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
  updateGroupOrganization(organizationGroup: Group) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_GROUP_ORGANIZATION_URL;
    let body = JSON.stringify(organizationGroup);
    console.log('[Data Load Service Organization UPDATE '.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(organizationGroup)));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - UPDATE Organization Group '.concat(JSON.stringify(response.json())));
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
   * Update doc type
   * @param existingDocType the eisting doc type being updated 
   */
  updateExistingDocumentType(existingDocType: DocumentType) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.UPDATE_DOC_TYPE_URL;
    let body = JSON.stringify(existingDocType);
    console.log('[Data Load Service] PUT Update Doc Type'.concat(URL_TO_CALL).concat(' ').concat(JSON.stringify(existingDocType)));

    return this.http.put(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] PUT - Update Doc Type '.concat(JSON.stringify(response.json())));
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

