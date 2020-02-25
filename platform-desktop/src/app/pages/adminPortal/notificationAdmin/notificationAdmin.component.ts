import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from '../../../models/user';
import { NotificationData } from '../../../models/notificationData';

import { NotificationAdmin } from '../../../models/admin/notificationAdmin';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';



import {DataLoadService} from '../dataLoad.service';

@Component({
  selector: 'notification-admin-table',
  templateUrl: './notificationAdminTable.html'
})
export class NotificationAdminTable {

  
    query: string = '';
    notificationScopeList:SmartTableListItem[] = [
      { value: 'System', title: 'System' }, 
      { value: 'Group', title: 'Group' }, 
      { value: 'Individual', title: 'Individual' }, 
    ];
    notificationTargetList:SmartTableListItem[] = [
      { value: 'System', title: '--- System' }, 
    ];

    settings = {};
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      
      this.getAllNotifications();
      this.loggedInName = localStorage.getItem('username');
    }

    ngAfterViewInit() {
      
    }
  
  
    source: LocalDataSource = new LocalDataSource();

    //
    // Tag Admin
    allNotifications: NotificationData[] = new Array<NotificationData>();
    allAdminNotifications: NotificationAdmin[] = new Array<NotificationAdmin>();
    currNotification: NotificationData =null;
    currentTargets: string[] = new Array<string>();
    loggedInName: string = null;
    currUser: User =null;


    // user data
    allUsers: Array<User>;
    // group data
    allGroupNames : string[] = new Array<string>();

  constructor(protected notificationService : DataLoadService) {
    this.loggedInName = localStorage.getItem('username');
    this.getAllUsers();
    
  }

  
  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onCreateConfirm(event): void {
    if (window.confirm('Are you sure you want to create?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    //
    // create the new group data
    var newNotification: NotificationData = this.convertNotificationAdminToNotificationData(event.newData);
    // add the user target
    var userTarget = event.newData.notificationTarget;
    var notificationScope = event.newData.notificationScope;

    console.log('NOTIFICAION ADMIN - Creating a new notification - convert '.concat(JSON.stringify(newNotification)));
    if(newNotification != null){
       this.createNotification(newNotification, notificationScope, userTarget, userTarget);
    }
  }

  onEditConfirm(event): void {

  }


  getAllNotifications() {
    this.notificationService.getAllNotifications().subscribe(
      data => { 
        this.allNotifications = data;
        if(data != null){
          // convert all the users to admin users
          for (const notification of this.allNotifications) {
            //
            // convert and addto data array
            console.log('[Smart Tables Service getAllNotifications] Row of Organization Data '.concat(JSON.stringify(notification)));
            this.allAdminNotifications.push(this.convertNotificationDataToNotificationAdmin(notification));
          }
        }
        this.source.load(this.allAdminNotifications);
      },
      error => console.log('Server Error'),
    );
  }

  createNotification(newNotification: NotificationData, scope:string, target: string, orgName:string){

    if(scope == 'Individual'){
      scope = "INDIVIDUAL";
    }
    if(scope == 'System'){
      scope = "SYSTEM";
    }
    if(scope == 'Group'){
      scope = "GROUP";
    }
    this.notificationService.createNewNotification(newNotification, scope, target, orgName).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
    );
  }

  convertNotificationDataToNotificationAdmin(item:any){
    var exportItem = new NotificationAdmin();

    exportItem.id = item.id;
    exportItem.notificationType = item.notificationType;
    if(item.auditData.itemType == 'Document'){
      exportItem.notificationScope = 'INDIVIDUAL';
      exportItem.notificationTarget  = item.user.name;
    }
    if(item.auditData.itemType == 'Notification'){
      exportItem.notificationScope = 'INDIVIDUAL';
      exportItem.notificationTarget  = item.user.name;
    }
    // exportItem.notificationTarget = item.auditData.itemType;
    exportItem.notificationText = item.notificationText;
    exportItem.notificationDescription = item.notificationDescription;
    exportItem.creationTimestamp = item.creationTimestamp;
    exportItem.notificationTimestamp = item.notificationTimestamp;
    
    return exportItem;
  }

  convertNotificationAdminToNotificationData(notificationAdmin: NotificationAdmin ){
    //
    var newNotification: NotificationData = new NotificationData();

    newNotification.id = notificationAdmin.id;
    newNotification.notificationType = notificationAdmin.notificationType;
    newNotification.notificationText = notificationAdmin.notificationText;
    newNotification.notificationDescription = notificationAdmin.notificationDescription;
    newNotification.creationTimestamp = notificationAdmin.creationTimestamp;
    newNotification.notificationTimestamp = notificationAdmin.notificationTimestamp;

    return newNotification;

  }

  getUserGroupId() {
    let user: User;
    let result: number = 0;
    user = JSON.parse(localStorage.getItem('user'));
    result = user.userOrganizations[0].id;
    return result;
  }


  getAllUsers() {
    this.notificationService.getAllUsers(false).subscribe(
      data => { 
        this.allUsers = data;
        this.allUsers.sort();
        this.getAllGroups();
        console.log('---> Notifications groups target '.concat(JSON.stringify(this.allGroupNames)));
        console.log('Notifications users '.concat(JSON.stringify(data)));
        console.log('Notifications users target '.concat(JSON.stringify(this.notificationTargetList)));
        console.log('Notifications users target LOGGED IN uSER ----> '.concat(JSON.stringify(this.loggedInName)));
            for (const groupName of this.allGroupNames) {
                //
                // convert and addto data array
                //if(user.credentials.username !== this.loggedInName){
                  var item: SmartTableListItem = { value: groupName, title: '(group) '.concat(groupName) };
                  this.notificationTargetList.push(item);
                  console.log('---> Notifications users iTEM  '.concat(JSON.stringify(item)));
                  console.log('---> Notifications users target '.concat(JSON.stringify(this.notificationTargetList)));
                //}
            }
            for (const user of this.allUsers) {
                //
                // convert and addto data array
                //if(user.credentials.username !== this.loggedInName){
                  var item: SmartTableListItem = { value: user.credentials.username, title: user.credentials.username };
                  this.notificationTargetList.push(item);
                  console.log('---> Notifications users iTEM  '.concat(JSON.stringify(item)));
                  console.log('---> Notifications users target '.concat(JSON.stringify(this.notificationTargetList)));
                //}
            }
            this.settings = {
              add: {
                addButtonContent: '<i class="ion-ios-plus-outline"></i>',
                createButtonContent: '<i class="ion-checkmark"></i>',
                cancelButtonContent: '<i class="ion-close"></i>',
                confirmCreate: true
              },
              edit: {
                editButtonContent: '<i class="ion-edit"></i>',
                saveButtonContent: '<i class="ion-checkmark"></i>',
                cancelButtonContent: '<i class="ion-close"></i>',
                confirmSave: true
              },
              delete: {
                deleteButtonContent: '<i class="ion-trash-a"></i>',
                confirmDelete: true
              },
              pager : {
                display : true,
                perPage:5,
              },
          
              columns: {
                notificationScope: {
                  title: 'Notification Scope',
                  type: 'string',
                          editable: true,
                          editor: {
                            type: 'list',
                            config: {
                              selectText: 'Select...',
                              list: this.notificationScopeList,
                            },
                          },
                          filter: {
                            type: 'list',
                            config: {
                              selectText: 'Select...',
                              list: this.notificationScopeList,
                            },
                          },
                },
                notificationTarget: {
                  title: 'Target',
                  type: 'string',
                  editable: true,
                          editor: {
                            type: 'list',
                            config: {
                              selectText: 'Select...',
                              list: this.notificationTargetList,
                            },
                          },
                          filter: {
                            type: 'list',
                            config: {
                              selectText: 'Select...',
                              list: this.notificationTargetList,
                            },
                          },
                },
                notificationText: {
                  title: 'Notification Text',
                  type: 'string'
                },
                notificationDescription: {
                  title: 'Notification Description',
                  type: 'string'
                },
                creationTimestamp: {
                  title: 'Creation Date',
                  type: 'string',
                  editable: false,
                },
                notificationTimestamp: {
                  title: 'Sent Date',
                  type: 'string',
                  editable: false
                },
              }
            };
            
      },
      error => console.log('Server Error'),
    );
  }

  getAllGroups(){
    console.log('--- ALL USERS '.concat(JSON.stringify(this.allUsers)));
    for (const user of this.allUsers) {
      // check if the group already exists in array of groups
      var group:any = this.allGroupNames.find(x => x == user.userGroups[0].name);
      if(group == null){
        this.allGroupNames.push(user.userGroups[0].name);
      }
    }
  }
}
