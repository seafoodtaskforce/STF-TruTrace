import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from '../../../models/user';
import { GroupType } from '../../../models/groupType';
import { OrganizationAdmin } from '../../../models/admin/organizationAdmin';
import { Group } from '../../../models/group';

import {DataLoadService} from '../dataLoad.service';
import { LookupEntity } from './../../../models/LookupEntity';
import { OrganizationStage } from './../../../models/OrganizationStage';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';
import { LocaleUtils } from '../../../utils/locale.utils';
import { DocumentService } from 'app/pages/documents/document.service';
import { ResponseIssue } from 'app/models/RestResponse';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';
import { ToasterService } from 'app/toaster-service.service';
import { Role } from 'app/models/role';
import { ROLE_ORG_ADMIN } from 'app/config/globals';

@Component({
  selector: 'organization-admin-table',
  templateUrl: './organizationAdminTable.html'
})
export class OrganizationAdminTable {

    activatedListValue:SmartTableListItem[] = [
      { value: 'Activated', title: 'Activated' }, 
      { value: 'Not Activated', title: 'Not Activated' }
    ];

    verifiedListValue:SmartTableListItem[] = [
      { value: 'Verified', title: 'Verified' }, 
      { value: 'Not Verified', title: 'Not Verified' }
    ];

    /**
     * Paged Table Data
     */
    query: string = '';
    settings:any;
    source: LocalDataSource = new LocalDataSource();
    organizationTypelist:SmartTableListItem[] = new Array<SmartTableListItem>();
    // Stages from server
    organizationStages: OrganizationStage[]  = new Array<OrganizationStage>(); 

    // the specific supported languages
    languages:LookupEntity[];
    selectedLanguageChoices:string[]=['en', 'th'];

    readonly CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE= 'document_import_choose_stage';

    //
    // CSV Batch Upload for User
    selectedCSVFileToUpload: string = null;
    selectedStageName: string = null;
    selectedStageId: number = null;
    uploadOperationErrorsFlag: boolean = false;
    errorMessages: ResponseIssue[] = Array<ResponseIssue>();
    loggedInName :string;

    //
    // Batch Upload
    currentStageNameListValueCSVBatchUpload: string = this.getInternationalizedToken(this.CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE);

  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      // this.getAllUsers();
      this.loggedInName = localStorage.getItem('username');
      this.getAllOrganizationGroups();
      this.getAllStages();
      this.getAvailableLanguages();
    }
  
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

  constructor(protected organizationService : DataLoadService, protected _documentService: DocumentService
                , private slimLoader: SlimLoadingBarService, protected toasterService:ToasterService) {
    
  }

  
  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onCreateConfirm(event): void {
    //
    // create the new group data
    var organizationGroup:Group = this.convertOrganizationAdminToOrganizationGroup(event.newData);
    console.log('ORG GROUP ADMIN - Creating a new org group - convert '.concat(JSON.stringify(organizationGroup)));

    if(!this.validateTableRowData(organizationGroup)){
      window.alert('Cannot create a new Organization. Please check your input data.');
    }else{
      if (window.confirm('Are you sure you want to create a new Organization?')) {
        if(organizationGroup != null){
          this.createGroupOrganization(organizationGroup);
        }
        this.getAllOrganizationGroups()
        event.confirm.resolve();
      } else {
        event.confirm.reject();
      }
    }
  }

  onEditConfirm(event): void {
    //
    // Update existing group data
    var organizationGroup:Group = this.convertOrganizationAdminToOrganizationGroup(event.newData);
    console.log('ORG GROUP ADMIN - Updating a new org group - convert '.concat(JSON.stringify(organizationGroup)));

    if(!this.validateTableRowData(organizationGroup)){
      window.alert('Cannot edit the Organization. Please check your input data.');
    }else{
      if (window.confirm('Are you sure you want to update this Organization?')) {
        if(organizationGroup != null){
          this.updateGroupOrganization(organizationGroup);
        }
        event.confirm.resolve();
      } else {
        event.confirm.reject();
      }
    }
  }

  onSearch(query: string = '') {
    this.source.setFilter([
      // fields we want to include in the search
      {
        field: 'organizationName',
        search: query
      },
      {
        field: 'emailAddress',
        search: query
      },
      {
        field: 'organizationType',
        search: query
      }
      ,
      {
        field: 'businessIDNumber',
        search: query
      }
      ,
      {
        field: 'legalBusinessName',
        search: query
      }
      ,
      {
        field: 'businessAddress',
        search: query
      }
      ,
      {
        field: 'gpsCoordinates',
        search: query
      }
    ], false); 
    // second parameter specifying whether to perform 'AND' or 'OR' search 
    // (meaning all columns should contain search query or at least one)
    // 'AND' by default, so changing to 'OR' by setting false here
  }



  onRowSelect(event): void {

  }

  convertOrganizationToOrganizationAdmin(organization:Group){
    var exportItem = new OrganizationAdmin();

    // get the resource from the resource map
    let value:string = LocaleUtils.getInternationalizedString(organization.groupType.name, this.selectedLanguageChoices[0]);
    
    exportItem.id = organization.id;
    exportItem.organizationName = organization.name;
    exportItem.organizationType = value;
    exportItem.businessIDNumber = organization.businessIDNumber;
    exportItem.legalBusinessName = organization.legalBusinessName;
    exportItem.businessAddress = organization.businessAddress;
    exportItem.gpsCoordinates = organization.gpsCoordinates;

    exportItem.activated = this.convertBooleanToActivatedString(this.isOrganizationActivated(organization));
    exportItem.verified = this.convertBooleanToVerifiedString(organization.verified);

    //user.contactInfo.verified = this.convertVerifiedToBoolean(userAdmin.verified);
    //user.contactInfo.activated = this.convertActivatedToBoolean(userAdmin.activated);
    
    exportItem.emailAddress = organization.emailAddress;
    // get the number of users
    exportItem.userNumber = 0;

    console.log('[Org Admin (Admin)] <converted> Row of Organization Data ', JSON.stringify(organization), JSON.stringify(exportItem));

    return exportItem;
  }

  getAllOrganizationGroups() {
    this.adminOrganizationData = [];
    this.organizationService.getAllOrganizationGroups().subscribe(
      data => { 
        this.organizationGroups = data;
            // convert all the users to admin users
            for (const organization of this.organizationGroups) {
                //
                // convert and addto data array
                console.log('[Org Admin (Admin)] Row of Organization Data '.concat(JSON.stringify(organization)));
                this.adminOrganizationData.push(this.convertOrganizationToOrganizationAdmin(organization));
            }
        this.source.load(this.adminOrganizationData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllStageData() {
    console.log('CSV Org Batch File Upload Group Name List' + JSON.stringify(this.organizationStages));
    return this.organizationStages;
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

  createGroupOrganization(organizationGroup: Group){
    this.organizationService.createGroupOrganization(organizationGroup).subscribe(
        data =>  {
          console.log('No issues');
        },
        error => {
          console.log('Server Error');
        } 
    );
  }

  updateGroupOrganization(organizationGroup: Group){
    this.organizationService.updateGroupOrganization(organizationGroup).subscribe(
        data =>  {
          console.log('No issues');
        },
        error => {
          console.log('Server Error');
        } 
    );
  }

  getOrganizationTypeId(organizationName:string){
    console.log('[Org Admin (Admin)]  Reversing <name> to <id> ', organizationName);
    for (const organization of this.organizationStages) {
      // get the id
      if(organization.name == organizationName){
        console.log('[Org Admin (Admin)]  Found <name> to <id> ', organizationName, organization.id);
        return organization.id;
      }
    }
    return null;
  }

  /**
   * 
   * @param item - the document type to use when creating a new doc
   */
   setNewStageNameForBatchUserUpload(item){
    var groupData : any;
    var reversedGroupName:string;

    // stages and headers
    for (var _j = 0; _j < this.organizationStages.length; _j++) {
      if(this.organizationStages[_j].value === item) {
        this.selectedStageId = this.organizationStages[_j].id
      }
    }

    console.log('CSV IMPORT - GROUP NAME ' + item);

  }


  convertOrganizationAdminToOrganizationGroup(data:any){
    var organizationGroup:Group = new Group();
    console.log('ORG GROUP ADMIN - Creating a new org group '.concat(JSON.stringify(data)));
    organizationGroup.id = data.id;
    organizationGroup.name = data.organizationName;
    organizationGroup.emailAddress = data.emailAddress;
    organizationGroup.businessIDNumber = data.businessIDNumber;
    organizationGroup.legalBusinessName = data.legalBusinessName;
    organizationGroup.businessAddress = data.businessAddress;
    organizationGroup.gpsCoordinates = data.gpsCoordinates;
    organizationGroup.groupType = new GroupType();
    organizationGroup.groupType.id = this.getOrganizationTypeId(data.organizationType);


    return organizationGroup;
  }

  
  /**
   * Initialize the paged table's data and look
   */
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
        perPage:10,
      },
  
      columns: {
        organizationName: {
          title: 'Display Name',
          type: 'string',
          filter: false
        },
        legalBusinessName: {
          title: 'Legal Business Name',
          type: 'string',
          filter: false
        },
        businessIDNumber: {
          title: 'Business Number',
          type: 'string',
          filter: false
        },
        businessAddress: {
          title: 'Business Address',
          type: 'string',
          filter: false
        },
        gpsCoordinates: {
          title: 'GPS Location',
          type: 'string',
          filter: false
        },
        organizationType: {
          title: 'Organization Type',
          type: 'string',
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.organizationTypelist,
            },
          },
          filter: false
        },
        emailAddress: {
          title: 'Email Address',
          type: 'string',
          filter: false
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
      }
    };
  }

  getAllStages(){
    this.organizationService.getAllStages().subscribe(
      data => { 
          this.organizationStages = data;
          console.log('[Org Admin (Admin)] GET ALL STAGES RESTFUL '.concat(JSON.stringify(data)));
          // 
          // fill in the rest of the data for the stages

          //
          // Set the mapping of the languages to the stages

          // get the value of iterations
          let emptyStages:boolean = true;
          let numberOfStages: number = this.organizationStages.length;

          // stages and headers
          for (var _j = 0; _j < numberOfStages; _j++) {
            this.organizationTypelist.push({ value: this.organizationStages[_j].name, title: this.organizationStages[_j].value });
          }
          //
          //
          this.initTableData();
        },
      error => console.log('Server Error'),
    );
  }

  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  getAvailableLanguages(){
    // TODO read from server
    this.languages  = [ 
      {'id' : 1, 'name' : 'en', 'value': 'English'},
      {'id': 2, 'name' : 'th', 'value': 'Thai'},
      {'id': 3, 'name' : 'vi', 'value': 'Vietnamese'},
      {'id': 4, 'name' : 'in', 'value': 'Bahasa'},
      {'id': 5, 'name' : 'es', 'value': 'Spanish'},
      {'id': 6, 'name' : 'hi', 'value': 'Hindi'},
      {'id': 7, 'name' : 'te', 'value': 'Telugu'},
    ];
  }

  setChosenLanguage(event: any){
    console.log('[Org Admin (Admin)]  <language> switch ', event, event.target.value);
    this.selectedLanguageChoices[0] = this.getLanguageKey(event.target.value);
    this.getAllOrganizationGroups()
  }

  getLanguageKey(value:string){
    for (let language of this.languages) {
      if(language.value === value){
        return language.name;
      }
    }
    return null;
  }

  validateTableRowData(organizationGroup:Group){
    console.log('[Org Admin (Admin)]  <validation> create ', JSON.stringify(organizationGroup));
    if(organizationGroup.groupType.id == null){
      return false;
    }

    if(organizationGroup.name == null || organizationGroup.name.length == 0){
      return false;
    }
    if(organizationGroup.legalBusinessName == null || organizationGroup.legalBusinessName.length == 0){
      return false;
    }
    if(organizationGroup.businessIDNumber == null || organizationGroup.businessIDNumber.length == 0){
      return false;
    }
    if(organizationGroup.gpsCoordinates == null || organizationGroup.gpsCoordinates.length == 0){
      return false;
    }

    if(organizationGroup.businessAddress == null || organizationGroup.businessAddress.length == 0){
      return false;
    }

    return true;
  }

  getAllStagesData() {
    console.log('CSV User Batch File Upload Stage Name List' + JSON.stringify(this.organizationStages));
    return this.organizationStages;
  }

    /**
   *  Upload the CSV file with teh data provided about user batch creation
   */
     onCSVFileUpload(){

      var isEnabled = 
        //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
        !(this.getInternationalizedToken(this.CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE) == this.CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE)
        this.selectedCSVFileToUpload !== undefined
        && this.selectedCSVFileToUpload !== null;
  
      if(isEnabled){
          var fd = new FormData();
          fd.append('file', this.selectedCSVFileToUpload);
          fd.append('userName', this.loggedInName);
          fd.append('stageId', '' + this.selectedStageId);
          
          console.log("CSV FIle Upload FROM DATA");
  
          console.log("CSV FIle Upload FROM DATA---> " + JSON.stringify(fd));
          this.startCSVOrgBatchFileUploadProgress();
            this.organizationService.startCSVOrgBatchUploadProcess();
            this.organizationService.onCSVOrgBatchFileUpload(this.selectedCSVFileToUpload, fd, this.slimLoader, this.organizationService, this);
      }
    }

      /**
   * Handling the CSV file removal from the dialog
   */
  onCSVFileRemoveChoice() {
    (<HTMLInputElement>document.getElementById("input-org-batch-upload-file")).value = "";
    this.selectedCSVFileToUpload = null;
    this.errorMessages = Array<ResponseIssue>();
    this.uploadOperationErrorsFlag = false;
    this.currentStageNameListValueCSVBatchUpload = this.getInternationalizedToken(this.CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE);
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
        !(this.getInternationalizedToken(this.CSV_BATCH_ORG_UPLOAD_GROUP_NAME_CHOOSE) == this.currentStageNameListValueCSVBatchUpload)
        this.selectedCSVFileToUpload !== undefined
        && this.selectedCSVFileToUpload !== null;
  
  
      if(name == 'Submit'){
        // is the doc type chosen?
  
        if(isEnabled && !this.isUploadingCSVOrgBatch()){
          return 'btn btn-info btn-xs active';
        }else{
          return 'btn btn-info btn-xs disabled';
        }
      }
      if(name == 'Cancel'){
        // is the doc type chosen?
  
        if(this.selectedCSVFileToUpload !== undefined
          && this.selectedCSVFileToUpload !== null
          && !this.isUploadingCSVOrgBatch()){
          return 'btn btn-danger btn-xs active';
        }else{
          return 'btn btn-danger btn-xs disabled';
        }
      }
      return 'btn btn-danger btn-xs active';
    }

  /****************************************************************************************************
   * Uploading section
   */

  showErrorToasterBackendCSV(){
    this.toasterService.Error("There were problems with your file. Upload Failed.");
  }

  isUploadingCSVOrgBatch(){
    return this.organizationService.csvOrgBatchUploadFlag;
  }

  /**
   * Start the progress bar for the upload CSV Batch user operation
   */
   startCSVOrgBatchFileUploadProgress() {
    // Progress bar
    
    this.slimLoader.start(() => {
      this.slimLoader.height = '8px';
      this.slimLoader.color = 'green';
        console.log('CSV Org Batch File Upload Complete');
    });
  }

  getUploadErrorMessages() {
    return this.errorMessages
  }

  getIssueLineNumber(issue: ResponseIssue){
    return Number(issue.lineNumber) + 1;
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

  isOrganizationActivated(org : Group){

    //
    // check if there is a user who is an admin and is actiavted
    for (let user of org.users) {
      if(user.roles[0].id === ROLE_ORG_ADMIN){
        return true;
      }
    }
    return false;

  }
}
