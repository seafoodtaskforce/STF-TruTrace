import { Component } from '@angular/core';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { LocalDataSource } from 'ng2-smart-table';
import { UserAdmin } from '../../../models/admin/userAdmin';
import { User } from '../../../models/user';
import { GroupType } from '../../../models/groupType';
import { Group } from '../../../models/group';
import { Role } from '../../../models/role';

import { RoleAdmin } from '../../../models/admin/roleAdmin';
import { DocumentTypeAdmin } from '../../../models/admin/documentTypeAdmin';
import { Organization } from '../../../models/organization';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';

import {DataLoadService} from '../dataLoad.service';
import { DocumentService } from 'app/pages/documents/document.service';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';
import { LocaleUtils } from 'app/utils/locale.utils';
import { BackendCSVErroMessage } from 'app/models/admin/errorBackendCSVMessage';
import { ResponseIssue } from 'app/models/RestResponse';
import { ToasterService } from 'app/toaster-service.service';

@Component({
  selector: 'user-admin-table',
  templateUrl: './userAdminTable.html'
})
export class UserAdminTable {

  
    query: string = '';
    userTypelist:SmartTableListItem[] = [
      { value: 'Super Admin', title: 'Super Admin' }, 
      { value: 'Matrix Admin', title: 'Matrix Admin' }, 
      { value: 'Org Admin',title: 'Org Admin'},
      { value: 'General',title: 'General'},
      ];

    activatedListValue:SmartTableListItem[] = [
      { value: 'Activated', title: 'Activated' }, 
      { value: 'Not Activated', title: 'Not Activated' }
    ];

    verifiedListValue:SmartTableListItem[] = [
      { value: 'Verified', title: 'Verified' }, 
      { value: 'Not Verified', title: 'Not Verified' }
    ];

    organizationList:SmartTableListItem[];
    groupNameList:SmartTableListItem[];

    organizationGroups:Group[];

    settings: any;

    //
    // CSV Batch Upload for User
    selectedCSVFileToUpload: string = null;
    selectedOrgGroupName: string = null;
    uploadOperationErrorsFlag: boolean = false;
    errorMessages: ResponseIssue[] = Array<ResponseIssue>();


    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.loggedInName = localStorage.getItem('username');
      this.organizationList = new Array<SmartTableListItem>();
  
      // fix up the 
      this.getAllUsers();
      this.fetchRoleAdminData();
      this.getAllOrganizationGroups();
      console.log('[User Admin Table] Organization List Item ngOnInit '.concat(JSON.stringify(this.organizationList)));

      
      
    }

    ngAfterViewInit() {
      /** 
      document.getElementsByClassName('firstName')['0'].style.width = '150px';
      document.getElementsByClassName('lastName')['0'].style.width = '200px';
      document.getElementsByClassName('cellnumber')['0'].style.width = '150px';
      document.getElementsByClassName('id')['0'].style.width = '20px';
      */
    }

    readonly CSV_BATCH_USER_UPLOAD_GROUP_NAME_CHOOSE= 'document_import_choose_group';
  
  
    source: LocalDataSource = new LocalDataSource();
  
    //
    // User admin
    adminUserData: UserAdmin[] = new Array<UserAdmin>();
    organizations: Organization[] = new Array<Organization>();
    users: Array<User>;
    currUser: User =null;
    currUserRoles: RoleAdmin[];
    currUserRole: RoleAdmin;
    showRoleAdminDetails:boolean = false;
    currPermissionDetails:string;
    organizationTypes: GroupType[] = new Array<GroupType>();
    loggedInName :string;

    //
    // Batch Upload
    currentOrgNameListValueCSVBatchUpload: string = this.getInternationalizedToken(this.CSV_BATCH_USER_UPLOAD_GROUP_NAME_CHOOSE);

  constructor(protected userService : DataLoadService, protected _documentService: DocumentService
              ,private slimLoader: SlimLoadingBarService, protected toasterService:ToasterService, ) {


  }

  
  /**
   * Deleting an entry for a user
   * @param event  - the event that holds the data about deletion
   */
  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    console.log('[Removing a User] --> User '.concat(JSON.stringify(event.newData)));
    var user:User = this.convertUserAdminToUser(event.newData);


  }

  /**
   * Creating a new user. 
   * Should make sure that the username is unique (backend)
   * @param event - the event that holds the data about creation
   */
  onCreateConfirm(event): void {
    if (window.confirm('Are you sure you want to create?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    console.log('[Creating NewUser] --> User '.concat(JSON.stringify(event.newData)));
    var user:User = this.convertUserAdminToUser(event.newData);
    console.log('[Created NewUser] --> User '.concat(JSON.stringify(user)));
    if(user != null){
      this.createUser(user);
    }
  }

  onEditConfirm(event): void {
    if (window.confirm('Are you sure you want to edit?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    var user:User = this.convertUserAdminToUser(event.newData);
    console.log('[Updated User] --> User '.concat(JSON.stringify(user)));
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
    exportItem.role = item.roles[0].value;
    exportItem.activated = this.convertBooleanToActivatedString(item.contactInfo.activated);
    exportItem.verified = this.convertBooleanToVerifiedString(item.contactInfo.verified);

    return exportItem;
  }

  convertUserAdminToUser(userAdmin: UserAdmin ){
    //
    // find the user in the users array
    var user:any = this.users.find(x => x.id == userAdmin.id);
    if(user == null){
      user = new User();
      user.id = 0;
      user.roles = new Array<Role>();
      user.roles.push(new Role());
      user.userGroups = new Array<Group>();
      user.userGroups.push(new Group());
    }

    user.contactInfo.firstName = userAdmin.firstName;
    user.contactInfo.lastName = userAdmin.lastName;
    user.credentials.username = userAdmin.username;
    user.name = userAdmin.username;
    user.contactInfo.emailAddress = userAdmin.email;
    user.contactInfo.cellNumber = userAdmin.cellnumber;
    user.contactInfo.verified = this.convertVerifiedToBoolean(userAdmin.verified);
    user.contactInfo.activated = this.convertActivatedToBoolean(userAdmin.activated);
    // get the other data
    user.roles[0].id = this.getRoleNameMappingToID(userAdmin.role);
    user.userGroups[0].id = this.getOrganizationNameMappingToID(userAdmin.resourceName);
    user.userGroups[0].organizationId = this.getOrganizationNameMappingToOrganizationID(userAdmin.resourceName);
    

    return user;

  }



  getAllUsers() {
    this.adminUserData = new Array<UserAdmin>();
    this.userService.getAllUsers(false).subscribe(
      data => { 
        this.users = data;
            // convert all the users to admin users
            var organization: string='';
            var loggedUser:any = this.users.find(x => x.name == this.loggedInName);

            console.log('[Checking Roles] --> User '.concat(JSON.stringify(loggedUser)));
            console.log('[Checking Roles] --> Org '.concat(JSON.stringify(organization)));
            console.log('[Checking Roles] --> User Name '.concat(JSON.stringify(this.loggedInName)));
            for (const user of this.users) {
                //
                // convert and addto data array
                this.adminUserData.push(this.convertUserToUserAdmin(user));
                /**
                organization = user.userGroups[0].name;
                // add to the table for all orgs
                var currOrg:any = this.organizationList.find(x => x.value == organization);
                console.log('[User Admin Table] Organization Found Item? '.concat(JSON.stringify(currOrg)));
                if(currOrg == null){
                  var orgItem:SmartTableListItem = new SmartTableListItem();
                  orgItem.title = organization;
                  orgItem.value = organization;
                  this.organizationList.push(orgItem);
                }
                console.log('[User Admin Table] Organization List Item '.concat(JSON.stringify(this.organizationList)));
                */
            }
            this.organizationList.sort();
        this.source.load(this.adminUserData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllOrganizationGroups() {
    this.userService.getAllOrganizationGroups().subscribe(
      data => { 
        this.organizationGroups = data;
        if(data != null){
            for (const organizationGroup of this.organizationGroups) {
                //
                // convert and addto data array
                console.log('[Organization GroupTypes] Row of Data '.concat(JSON.stringify(organizationGroup)));
                // add to the table for all orgs
                  var orgItem:SmartTableListItem = new SmartTableListItem();
                  orgItem.title = organizationGroup.name;
                  orgItem.value = organizationGroup.name;
                  this.organizationList.push(orgItem);
                // this.adminUserData.push(this.convertUserToUserAdmin(user));
            }
        }
        this.initTableData();


      },
      error => console.log('Server Error'),
    );
  }

  updateUser(user:User){
    this.userService.updateUserProfile(user).subscribe(
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

    this.currUserRoles = new Array<RoleAdmin>();

    // get all the allowed docs for the current user
    var i: number = 0;
    console.log('[Smart Tables Service] Row of Data Allowed Docs'.concat(JSON.stringify(this.currUser)));
    if(this.currUser != null){
        var userType: RoleAdmin = new RoleAdmin();
        userType.id = 1;
        userType.isActive = true;
        userType.roleName = this.currUser.roles[0].value;
        userType.docTypes = new Array<DocumentTypeAdmin>();
        for (let allowedDocType of (this.currUser.userGroups[0].allowedDocTypes as any)) {
          //
          // convert and addto data array
          var docType1: DocumentTypeAdmin = new DocumentTypeAdmin();
          docType1.id = ++i;
          docType1.name = 'name';
          docType1.value = allowedDocType.value;
          docType1.docTypeName=allowedDocType.documentDesignation;
          docType1.permissions="READ, WRITE, CREATE, DELETE";

          userType.docTypes.push(docType1);

          console.log('[Smart Tables Service] Row of Data for user docs'.concat(JSON.stringify(allowedDocType)));

        }
        this.currUserRoles.push(userType);
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
    adminUser.roleName = "Super-Admin User";
    adminUser.docTypes= [docType10,docType11,docType12,docType13,docType14];

    this.currUserRoles.push(adminUser);

    


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

  getOrganizationNames(){

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

  createUser(user:User){
    this.userService.createUser(user).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  getRoleNameMappingToID(role:string){
    if(role === 'Admin'){
      return 1;
    }
    if(role === 'Super Admin'){
      return 2;
    }
    if(role === 'Matrix Admin'){
      return 8;
    }
    if(role === 'Org Admin'){
      return 7;
    }
    if(role === 'General'){
      return 3;
    }
    if(role === 'General'){
      return 4;
    }
  }

  getOrganizationNameMappingToID(organizationGroupName:string){
    if(this.organizationGroups !== null){
      var orgGroup:any = this.organizationGroups.find(x => x.name == organizationGroupName);
      console.log('Org Group '.concat(JSON.stringify(orgGroup)));
      if(orgGroup == null){
        console.log('ORGANIZATION GROUP NOT MAPPED'.concat(' '.concat(organizationGroupName)));
        return null;
      }
      return orgGroup.id;
    }
    return null;
    
  }
  getOrganizationNameMappingToOrganizationID(organizationGroupName:string){
    if(this.organizationGroups !== null){
      var orgGroup:any = this.organizationGroups.find(x => x.name == organizationGroupName);
      console.log('Org Group '.concat(JSON.stringify(orgGroup)));
      if(orgGroup == null){
        console.log('ORGANIZATION GROUP NOT MAPPED'.concat(' '.concat(organizationGroupName)));
        return null;
      }
      return orgGroup.organizationId;
    }
    return null;
  }

  /****************************************************************************************************
   * Uploading section
   */

  isUploadingCSVUserBatch(){
    return this.userService.csvUserBatchUploadFlag;
  }


  /**
   *  Upload the CSV file with teh data provided about user batch creation
   */
  onCSVFileUpload(){

    var isEnabled = 
      //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
      !(this.getInternationalizedToken(this.CSV_BATCH_USER_UPLOAD_GROUP_NAME_CHOOSE) == this.currentOrgNameListValueCSVBatchUpload)
      this.selectedCSVFileToUpload !== undefined
      && this.selectedCSVFileToUpload !== null;

    if(isEnabled){
        var fd = new FormData();
        fd.append('file', this.selectedCSVFileToUpload);
        fd.append('userName', this.loggedInName);
        fd.append('orgName', this.currentOrgNameListValueCSVBatchUpload);
        
        console.log("CSV FIle Upload FROM DATA");

        console.log("CSV FIle Upload FROM DATA---> " + JSON.stringify(fd));
        this.startCSVUserBatchFileUploadProgress();
          this.userService.startCSVUserBatchUploadProcess();
          this.userService.onCSVUserBatchFileUpload(this.selectedCSVFileToUpload, fd, this.slimLoader, this.userService, this);
    }
  }

  /**
   * Handling the CSV file removal from the dialog
   */
  onCSVFileRemoveChoice() {
    (<HTMLInputElement>document.getElementById("input-user-batch-upload-file")).value = "";
    this.selectedCSVFileToUpload = null;
    this.errorMessages = Array<ResponseIssue>();
    this.uploadOperationErrorsFlag = false;
    this.currentOrgNameListValueCSVBatchUpload = this.getInternationalizedToken(this.CSV_BATCH_USER_UPLOAD_GROUP_NAME_CHOOSE);
  }

  /**
   * Getting and storing the file that was chosen in the page
   * @param event  - the veent of the file selection
   */
  onCSVFileSelected(event){
    this.selectedCSVFileToUpload = event.target.files[0];
    console.log("CSV FIle Upload" + event.target.files[0]);

  }

  /**
   * Get he styling for the buttons that represent the upload and calcel action for CSF vile operations
   * @param name - the buton name for the styling
   */
  getClassForUploadProcessButton(name:string){
    var isEnabled = 
      //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
      !(this.getInternationalizedToken(this.CSV_BATCH_USER_UPLOAD_GROUP_NAME_CHOOSE) == this.currentOrgNameListValueCSVBatchUpload)
      this.selectedCSVFileToUpload !== undefined
      && this.selectedCSVFileToUpload !== null;


    if(name == 'Submit'){
      // is the doc type chosen?

      if(isEnabled && !this.isUploadingCSVUserBatch()){
        return 'btn btn-info btn-xs active';
      }else{
        return 'btn btn-info btn-xs disabled';
      }
    }
    if(name == 'Cancel'){
      // is the doc type chosen?

      if(this.selectedCSVFileToUpload !== undefined
        && this.selectedCSVFileToUpload !== null
        && !this.isUploadingCSVUserBatch()){
        return 'btn btn-danger btn-xs active';
      }else{
        return 'btn btn-danger btn-xs disabled';
      }
    }

    return 'btn btn-danger btn-xs active';
    
  }

  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  /**
   * Start the progress bar for the upload CSV Batch user operation
   */
  startCSVUserBatchFileUploadProgress() {
    // Progress bar
    
    this.slimLoader.start(() => {
      this.slimLoader.height = '8px';
      this.slimLoader.color = 'green';
        console.log('CSV User Batch File Upload Complete');
    });
  }

  getAllOrganizations() {
    console.log('CSV User Batch File Upload Group Name List' + JSON.stringify(this.organizationGroups));
    return this.organizationGroups;
  }

  /**
   * 
   * @param item - the document type to use when creating a new doc
   */
  setNewOrgNameForBatchUserUpload(item){
    var groupData : any;
    var reversedGroupName:string;

    console.log('CSV IMPORT - GROUP NAME ' + item);
    //fetch the reverse value of the item; parsed out away from UI render and into the doc type
    // reversedDocTypeName = this._documentService.reverseInternationalizedNameStringToKeyString(item);
    reversedGroupName = LocaleUtils.fetchResourceKeyByValue(item);
    console.log('PDF IMPORT - TYPE<reversed>' + reversedGroupName);


    for(var row=0 ; row < this.organizationList.length; row++) {
      groupData = this.organizationList[row];
      console.log('CSV IMPORT ---- GROUP NAME<looking>' + JSON.stringify(groupData.name));
      console.log('CSV IMPORT ---- GROUP NAME<looking> <full>' + JSON.stringify(groupData));
      
      if( groupData.name === reversedGroupName){
        this.selectedOrgGroupName = groupData.name;
        //this.currectDocumentTypeForNewDocListValue = docType.value;
        console.log('PDF IMPORT ---- GROUP NAME<found>' + JSON.stringify(groupData));
        
      }
    }
  }

  getUploadErrorMessages() {
    return this.errorMessages
  }

  getIssueLineNumber(issue: ResponseIssue){
    return Number(issue.lineNumber) + 1;
  }

  initTableData(){
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

        firstName: {
          title: 'First Name',
          type: 'string',
          width: '100px',
        },
        lastName: {
          title: 'Last Name',
          type: 'string',
          width: '150px',
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
          type: 'string',
          width: '70px',
          editable: true,
        },
        role: {
          title: 'User Type',
          type: 'string',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.userTypelist,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.userTypelist,
            },
          },
        },

        resourceName: {
          title: 'Organization',
          type: 'string',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.organizationList,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.organizationList,
            },
          },
        },
        verified: {
          title: 'Verified?',
          type: 'boolean',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.verifiedListValue,
            },
          },
          filter: false
      },
      activated: {
        title: 'Activated?',
        type: 'boolean',
        editable: true,
        editor: {
          type: 'list',
          config: {
            selectText: 'Select...',
            list: this.activatedListValue,
          },
        },
        filter: false
    },

        id:{
          title: 'id',
          type: 'number',
          width: '40px',
          editable : false,
        },
      }
    };

  }

  extractBackendCSVErroMessage(dataRow: string) {
    var message : BackendCSVErroMessage = new BackendCSVErroMessage();
    var splitted = dataRow.split(",", 3);
    message.lineNumber = splitted[0];
    message.lineNumber = splitted[0];
    message.lineNumber = splitted[0];

  }

  showErrorToasterBackendCSV(){
    this.toasterService.Error("There were problems with your file. Upload Failed.");
  }

  convertVerifiedToBoolean(text : string){
    if(text == 'Verified'){
      return true;
    } else {
      return false;
    }
  }

  convertActivatedToBoolean(text : string){
    if(text == 'Activated'){
      return true;
    } else {
      return false;
    }
  }

  convertBooleanToActivatedString(value:boolean){
    if(value == true){
      return 'Activated';
    } else {
      return 'Not Activated';
    }
  }

  convertBooleanToVerifiedString(value:boolean){
    if(value == true){
      return 'Verified';
    } else {
      return 'Not Verified';
    }
  }
}
