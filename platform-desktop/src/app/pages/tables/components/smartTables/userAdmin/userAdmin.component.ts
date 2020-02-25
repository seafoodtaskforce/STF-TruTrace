import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { UserAdmin } from '../../../../../models/admin/userAdmin';
import { User } from '../../../../../models/user';
import { GroupType } from '../../../../../models/groupType';

import { RoleAdmin } from '../../../../../models/admin/roleAdmin';
import { DocumentTypeAdmin } from '../../../../../models/admin/documentTypeAdmin';
import { DocumentType } from '../../../../../models/documentType';

import {DataLoadService} from '../dataLoad.service';

@Component({
  selector: 'user-admin-table',
  templateUrl: './userAdminTable.html'
})
export class UserAdminTable {

  
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
        firstName: {
          title: 'First Name',
          type: 'string'
        },
        lastName: {
          title: 'Last Name',
          type: 'string'
        },
        username: {
          title: 'Username',
          type: 'string'
        },
        email: {
          title: 'E-mail',
          type: 'string'
        },
        cellnumber: {
          title: 'Cell#',
          type: 'string'
        },

        resourceName: {
          title: 'Organization',
          type: 'string'
        },
        organization: {
          title: 'Organization Type',
          type: 'string',
          editable: false,
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
    
        /**
        userType: {
          title: 'Role',
          type: 'string'
        },
            /** 
        resourceType: {
          title: 'Doc Type',
          type: 'string'
        },
        resourceName: {
          title: 'Doc Name',
          type: 'string'
        },
        permissions: {
          title: 'Permissions',
          type: 'string'
        },
        */
      }
    };
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.getAllUsers();
      this.fetchRoleAdminData();
    }
  
  
    source: LocalDataSource = new LocalDataSource();
  
    //
    // User admin
    adminUserData: UserAdmin[] = new Array<UserAdmin>();
    users: Array<User>;
    currUser: User =null;
    currUserRoles: RoleAdmin[];
    currUserRole: RoleAdmin;
    showRoleAdminDetails:boolean = false;
    currPermissionDetails:string;
    organizationTypes: GroupType[] = new Array<GroupType>();;

  constructor(protected userService : DataLoadService) {
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
    if (window.confirm('Are you sure you want to edit?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    var user:User = this.convertUserAdminToUser(event.newData);
    if(user != null){
      this.updateUser(user);
    }
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

  convertUserToUserAdmin(item:any){
    var exportItem = new UserAdmin();
    exportItem.id = item.id;
    exportItem.firstName = item.contactInfo.firstName;
    exportItem.lastName = item.contactInfo.lastName;
    exportItem.username = item.credentials.username;
    exportItem.email = item.contactInfo.emailAddress;
    exportItem.cellnumber = item.contactInfo.cellNumber;
    exportItem.organization = item.userGroups[0].groupType.name;
    exportItem.userType = '';
    exportItem.resourceType = '';
    exportItem.resourceName = item.userGroups[0].name;
    exportItem.permissions = '';

    return exportItem;
  }

  convertUserAdminToUser(userAdmin: UserAdmin ){
    //
    // find the user in the users array
    var user:any = this.users.find(x => x.id == userAdmin.id);
    if(user == null){
      return null;
    }

    user.id = userAdmin.id;
    user.contactInfo.firstName = userAdmin.firstName;
    user.contactInfo.lastName = userAdmin.lastName;
    user.credentials.username = userAdmin.username;
    user.name = userAdmin.username;
    user.contactInfo.emailAddress = userAdmin.email;
    user.contactInfo.cellNumber = userAdmin.cellnumber;

    return user;

  }

  getAllUsers() {
    this.userService.getAllUsers(false).subscribe(
      data => { 
        this.users = data;
            // convert all the users to admin users
            for (const user of this.users) {
                //
                // convert and addto data array
                console.log('[Smart Tables Service] Row of Data USERS '.concat(JSON.stringify(user)));
                this.adminUserData.push(this.convertUserToUserAdmin(user));
            }
        this.source.load(this.adminUserData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllOrganizationTypes() {
    this.userService.getAllOrganizationTypes().subscribe(
      data => { 
        this.organizationTypes = data;
            // convert all the users to admin users
            for (const user of this.users) {
                //
                // convert and addto data array
                console.log('[Smart Tables Service] Row of Data '.concat(JSON.stringify(user)));
                this.adminUserData.push(this.convertUserToUserAdmin(user));
            }
        this.source.load(this.adminUserData);
      },
      error => console.log('Server Error'),
    );
  }

  updateUser(user:User){
    this.userService.updateUser(user).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  onRowSelect(event) {
    // alert(`Custom event '${event.action}' fired on row with username: ${event.data.username}`)

    this.currUser = this.convertUserAdminToUser(event.data);
    this.fetchRoleAdminData();
    console.log('[Smart Tables Service] CURR USER'.concat(JSON.stringify(this.currUser)));
  }

  /**
   * Selects the specific tab to be active in the tabbed layout
   * @param tabName - the name of the tab to make active
   */
  getTabActiveElement(tabName: string) {
    if (tabName === 'pages') {
      return 'nav-link active';
    }
    if (tabName === 'linked') {
      return 'nav-link';
    }
    if (tabName === 'attached') {
      return 'nav-link';
    }
    if (tabName === 'tags') {
      return 'nav-link';
    }
   }

   fetchRoleAdminData(){

    //
    // Normal User
    var normalUser: RoleAdmin = new RoleAdmin();
    normalUser.id = 1;
    normalUser.isActive = true;
    normalUser.roleName = "Normal User";
    normalUser.docTypes = new Array<DocumentTypeAdmin>();

    

    // get all the allowed docs for the current user
    var i: number = 0;
    console.log('[Smart Tables Service] Row of Data Allowed Docs'.concat(JSON.stringify(this.currUser)));
    if(this.currUser != null){
        for (let allowedDocType of (this.currUser.userGroups[0].allowedDocTypes as any)) {
          //
          // convert and addto data array
          var docType1: DocumentTypeAdmin = new DocumentTypeAdmin();
          docType1.id = ++i;
          docType1.name = 'name';
          docType1.value = allowedDocType.value;
          docType1.docTypeName=allowedDocType.documentDesignation;
          docType1.permissions="READ, WRITE, CREATE, DELETE";

          normalUser.docTypes.push(docType1);

          console.log('[Smart Tables Service] Row of Data for user docs'.concat(JSON.stringify(allowedDocType)));

        }
    }

    //
    // Admin User
    var docType10: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType10.id = 1;
    docType10.name = 'name';
    docType10.value = 'Bill of Lading';
    docType10.docTypeName='Profile Doc';
    docType10.permissions="READ, WRITE, CREATE, DELETE";

    var docType11: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType11.id = 2;
    docType11.name = 'name';
    docType11.value = 'Vessel documents';
    docType11.docTypeName='Profile Doc';
    docType11.permissions="READ, WRITE, CREATE, DELETE";

    var docType12: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType12.id = 3;
    docType12.name = 'name';
    docType12.value = 'Farm Documents';
    docType12.docTypeName='Passthrough';
    docType12.permissions="READ, WRITE, CREATE, DELETE, DOWNLOAD, COPY, LINK, ATTACH, EXECUTE, SEARCH, FILTER";

    var docType13: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType13.id = 4;
    docType13.name = 'name';
    docType13.value = 'Fishing Log book';
    docType13.docTypeName='Passthrough';
    docType13.permissions="READ, WRITE, CREATE, DELETE, DOWNLOAD, COPY, LINK, ATTACH, EXECUTE, SEARCH, FILTER";

    var docType14: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType14.id = 5;
    docType14.name = 'name';
    docType14.value = 'Info of Conveyance';
    docType14.docTypeName='Passthrough';
    docType14.permissions="READ, WRITE, CREATE, DELETE, DOWNLOAD, COPY, LINK, ATTACH, EXECUTE, SEARCH, FILTER";

    // array
    var adminUser: RoleAdmin = new RoleAdmin();
    adminUser.id = 2;
    adminUser.isActive = false;
    adminUser.roleName = "Admin User";
    adminUser.docTypes= [docType10,docType11,docType12,docType13,docType14];

    this.currUserRoles=[normalUser, adminUser];


   }

   showDetails(role:RoleAdmin){
     if(this.currUserRole === role){
      this.currUserRole = null;
      this.showRoleAdminDetails = false;
      
     }else{
      this.currUserRole = role;
      this.showRoleAdminDetails = true;
     }
     this.currPermissionDetails='';
   }

   expandRoleAdminDetails(){
    return this.showRoleAdminDetails = false;;
   }

   getBackgroundColorHighlight(id: number) {
    if (this.currUserRole == null) {
      return '#fff';
    }
    if (id === this.currUserRole.id) {
      // return '#5dd2ff';
      return '#F2F2F2';
    }else {
      return '#fff';
    }
  }

  getShortenedPermissions(permissions:  string){
    // parse around the permissions
    // READ, WRITE, CREATE, DELETE, DOWNLOAD, COPY, LINK, ATTACH, EXECUTE, SEARCH, FILTER
    var permissionDataDefinitions: string[] = ['READ', 'WRITE', 'CREATE', 'DELETE', 'DOWNLOAD', 'COPY', 'LINK', 'ATTACH', 'EXECUTE', 'SEARCH', 'FILTER'];
    var result:string='';

    for(var row=0 ; row < permissionDataDefinitions.length; row++){
      if(permissions.search(permissionDataDefinitions[row])== -1){
        //did not find it
        result = result.concat('X');
      }else{
        result = result.concat(permissionDataDefinitions[row].charAt(0));
      }
    }
    return result;
  }

  showPermissionDetails(permissions:  string){
    this.currPermissionDetails = permissions;
  }

  getCurrentUser(bypass:boolean){
    if(bypass === true && this.currUser ===null){
      return new User();
    }
    return this.currUser;
  }

  getCurrentUserEmail(){
    if(this.currUser ===null){
      return '';
    }else{
      return this.currUser.contactInfo.emailAddress;
    }
  }

  getCurrentUserFirstName(){
    if(this.currUser ===null){
      return '';
    }else{
      return this.currUser.contactInfo.firstName;
    }
  }

  getCurrentUserLastName(){
    if(this.currUser ===null){
      return '';
    }else{
      return this.currUser.contactInfo.lastName;
    }
  }

  getCurrentUserUserName(){
    if(this.currUser ===null){
      return '';
    }else{
      return this.currUser.name;
    }
  }

  getCurrentUserCellNumber(){
    if(this.currUser ===null){
      return '';
    }else{
      return this.currUser.contactInfo.cellNumber;
    }
  }

  getCurrentUserOrganizationType(){
    if(this.currUser === null){
      return '';
    }else{
      return this.currUser.userGroups[0].name;
    }
  }




}
