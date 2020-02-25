import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { UserAdmin } from '../../../../../models/admin/userAdmin';
import { User } from '../../../../../models/user';
import { GroupType } from '../../../../../models/groupType';
import { RoleAdmin } from '../../../../../models/admin/roleAdmin';
import { DocumentTypeAdmin } from '../../../../../models/admin/documentTypeAdmin';

import { OrganizationAdmin } from '../../../../../models/admin/organizationAdmin';
import { Group } from '../../../../../models/group';



import {DataLoadService} from '../dataLoad.service';

@Component({
  selector: 'organization-admin-table',
  templateUrl: './organizationAdminTable.html'
})
export class OrganizationAdminTable {

  
    query: string = '';

    settings = {
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
        organizationName: {
          title: 'Organization Name',
          type: 'string'
        },
        description: {
          title: 'Description',
          type: 'string'
        },
        organizationType: {
          title: 'Organization Type',
          type: 'string',
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: [
                  { value: 'Processor', title: 'Processor' }, 
                  { value: 'Farm', title: 'Farm' }, 
                  { value: 'Feed Mill',title: 'Feed Mill'},
                  { value: 'Fishmeal Plant',title: 'Fishmeal Plant'},
                  { value: 'Vessel',title: 'Vessel'},
                  { value: 'Fish Processor',title: 'Fish Processor'},
                ],
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: [
                  { value: 'Processor', title: 'Processor' }, 
                  { value: 'Farm', title: 'Farm' }, 
                  { value: 'Feed Mill',title: 'Feed Mill'},
                  { value: 'Fishmeal Plant',title: 'Fishmeal Plant'},
                  { value: 'Vessel',title: 'Vessel'},
                  { value: 'Fish Processor',title: 'Fish Processor'},
                ],
            },
          },
        },
        userNumber: {
          title: 'Number of Users',
          type: 'string',
          editable: false
        },

      }
    };
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      // this.getAllUsers();
      this.getAllOrganizationGroups();
    }
  
  
    source: LocalDataSource = new LocalDataSource();
  
    //
    // Organization admin
    organizationTypes: GroupType[] = new Array<GroupType>();
    organizationGroups: Group[] = new Array<Group>(); 
    adminOrganizationData: OrganizationAdmin[] = new Array<OrganizationAdmin>();

    currOrganization: OrganizationAdmin =null;
    showRoleAdminDetails:boolean = false;
    currPermissionDetails:string;
    groupTypes: GroupType[];

    // user data
    allUsers: Array<User>;

  constructor(protected organizationService : DataLoadService) {
    
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
  }

  onEditConfirm(event): void {

  }

  convertUserAdmin(item:any){
    var exportItem = new UserAdmin();
    exportItem.firstName = item.firstName;
    exportItem.lastName = item.lastName;
    exportItem.username = item.username;
    exportItem.email = item.email;
    exportItem.cellnumber = item.cellnumber;
    exportItem.organization = item.organization;
    exportItem.userType = item.userType;
    exportItem.resourceType = item.resourceType;
    exportItem.resourceName = item.resourceName;
    exportItem.permissions = item.permissions;

    return exportItem;
  }

  convertOrganizationToOrganizationAdmin(organization:Group){
    var exportItem = new OrganizationAdmin();
    exportItem.id = organization.id;
    exportItem.organizationName = organization.name;
    exportItem.organizationType = organization.groupType.name;
    exportItem.description = organization.description;
    // get the number of users
    exportItem.userNumber = 0;
    // console.log('[Smart Tables Service - Org Admin Loop] Row of Data USERS '.concat(''+ this.allUsers.length));

    /**
    for(var i=0 ; i < this.allUsers.length; i++){
      const user:User = this.allUsers[i];
      console.log('[Smart Tables Service - Org Admin] Row of Data USERS '.concat(JSON.stringify(user)));
      if(user.userGroups[0].name === organization.name){
        exportItem.userNumber++;
      }
    }
     */
    return exportItem;
  }

  getAllOrganizationGroups() {
    this.organizationService.getAllOrganizationGroups().subscribe(
      data => { 
        this.organizationGroups = data;
            // convert all the users to admin users
            for (const organization of this.organizationGroups) {
                //
                // convert and addto data array
                console.log('[Smart Tables Service getAllOrganizationGroups] Row of Organization Data '.concat(JSON.stringify(organization)));
                this.adminOrganizationData.push(this.convertOrganizationToOrganizationAdmin(organization));
            }
        this.source.load(this.adminOrganizationData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllUsers() {
    this.organizationService.getAllUsers(true).subscribe(
      data => { 
        this.allUsers = data;
        console.log('ORG ADMIN - Number of Users Total ' + this.allUsers.length);
      },
      error => console.log('Server Error'),
    );
  }





}
