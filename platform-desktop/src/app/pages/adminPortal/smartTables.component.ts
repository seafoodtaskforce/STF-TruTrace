import { Component } from '@angular/core';

import { DataLoadService } from './dataLoad.service';
import { LocalDataSource } from 'ng2-smart-table';
import { UserAdmin } from '../../models/admin/userAdmin';
import { User } from '../../models/user';
import { RoleAdmin } from '../../models/admin/roleAdmin';
import { DocumentTypeAdmin } from '../../models/admin/documentTypeAdmin';

@Component({
  selector: 'smart-tables',
  templateUrl: './smartTables.html',
  styleUrls: ['./smartTables.scss']
})
export class SmartTables {

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
      organization: {
        title: 'Organization',
        type: 'string'
      },
      resourceName: {
        title: 'Doc Name',
        type: 'string'
      },
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
  loggedInName: String;
  loggedInUser: User;

  constructor(protected userService : DataLoadService) {
    /** 
    this.service.getData().then((data) => {
      for(var i=0 ; i < data.length; i++){
        console.log('[Smart Tables Service] Row of Data '.concat(JSON.stringify(data[i])));
        this.adminData.push(this.convertUserAdmin(data[i]));
      }
      this.source.load(this.adminData);
    });
    */
    //this.getAllUsers();
    //this.source.load(this.adminData);
    this.loggedInName = localStorage.getItem('username');
    this.loggedInUser =  JSON.parse(localStorage.getItem('user'))
    
    
    
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
    exportItem.organization = item.userOrganizations[0].name;
    exportItem.userType = '';
    exportItem.resourceType = '';
    exportItem.resourceName = '';
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
                console.log('[Smart Tables Service] Row of Data '.concat(JSON.stringify(user)));
                this.adminUserData.push(this.convertUserToUserAdmin(user));
            }
            
            var user:any = this.users.find(x => x.name == this.loggedInName);
            if(user == null){
              // do nothing
              console.log('NEW Logged In USER: DID NOT FIND IT');
            }else{
              this.loggedInUser = user;
              console.log('NEW Logged In USER: FOUND IT');
            }
            this.loggedInUser =  JSON.parse(localStorage.getItem('user'))
            console.log('NEW Logged In USER: ' + JSON.stringify(this.loggedInUser));
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
    var docType1: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType1.id = 1;
    docType1.name = 'name';
    docType1.value = 'Bill of Lading';
    docType1.docTypeName='Profile Doc';
    docType1.permissions="READ, WRITE, CREATE, DELETE";

    var docType2: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType2.id = 2;
    docType2.name = 'name';
    docType2.value = 'Vessel documents';
    docType2.docTypeName='Profile Doc';
    docType2.permissions="READ, WRITE, CREATE, DELETE";

    var docType3: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType3.id = 3;
    docType3.name = 'name';
    docType3.value = 'Farm Documents';
    docType3.docTypeName='Passthrough';
    docType3.permissions="READ, WRITE, CREATE";

    var docType4: DocumentTypeAdmin = new DocumentTypeAdmin();
    docType4.id = 4;
    docType4.name = 'name';
    docType4.value = 'Fishing Log book';
    docType4.docTypeName='Passthrough';
    docType4.permissions="READ, WRITE, CREATE";


    // array
    var normalUser: RoleAdmin = new RoleAdmin();
    normalUser.id = 1;
    normalUser.isActive = true;
    normalUser.roleName = "Normal User";
    normalUser.docTypes= [docType1,docType2,docType3,docType4];


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

  tabClick(event:any, name:string){
    console.log('TAB EVENTS ',name,event);
  }

  /**
   * This will check the permissions for the specific tab and the currently logged in user
   * @param name -  the name of the tab to be checked
   */
  checkAdminTabPermission(name : string) {
    var result : boolean  = false;

    //
    // Super Admin
    if(this.loggedInUser.roles[0].value ==='Super Admin' ){
      return true;
    }

    //
    // Org Admin
    if(this.loggedInUser.roles[0].value ==='Org Admin' ){
      if(name == 'user-admin-table'
        || name == 'notification-admin-table'
        || name == 'tag-admin-table'
        || name == 'doc-export-admin-table') {
        return true;
      }
    }

    //
    // Matrix Admin
    if(this.loggedInUser.roles[0].value ==='Matrix Admin' ){
      if(name == 'user-admin-table'
        || name == 'notification-admin-table'
        || name == 'tag-admin-table'
        || name == 'organization-admin-table'
        || name == 'doc-export-admin-table') {
        return true;
      }
    }

    return result;
  }
}
