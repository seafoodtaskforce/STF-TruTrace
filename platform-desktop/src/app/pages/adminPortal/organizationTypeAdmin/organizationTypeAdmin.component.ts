import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { GroupType } from '../../../models/groupType';
import { RoleAdmin } from '../../../models/admin/roleAdmin';
import { DocumentTypeOrgTypeAdmin } from '../../../models/admin/DocumentTypeOrgTypeAdmin';

import { DocumentType } from '../../../models/DocumentType';

import { OrganizationTypeAdmin } from '../../../models/admin/organizationTypeAdmin';
import { OrganizationAdmin } from '../../../models/admin/organizationAdmin';
import { Group } from '../../../models/group';

import { OrganizationStage } from './../../../models/OrganizationStage';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';
import {DataLoadService} from '../dataLoad.service';

import { LookupEntity } from './../../../models/LookupEntity';
import { LocaleUtils } from '../../../utils/LocaleUtils';

@Component({
  selector: 'organization-type-admin-table',
  templateUrl: './organizationTypeAdminTable.html'
})
export class OrganizationTypeAdminTable {

  
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

    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.getAvailableLanguages();
      this.getAllStages();
      this.getAllOrganizationGroups();
      this.getAllOrganizationTypes();
      this.getAllDocTypes();

    }

    ngAfterViewInit() {
      console.log('[Org Type Admin (Admin)] <ngAfterViewInit> ');
    }

    ngOnChanges(){
      console.log('[Org Type Admin (Admin)] <ngOnChanges> ');
    }

    
    ngDoCheck(){
      console.log('[Org Type Admin (Admin)] <ngDoCheck> ');
    }
    //
    // Organization admin
    organizationTypes: GroupType[] = new Array<GroupType>();
    organizationGroups: Group[] = new Array<Group>(); 
    adminOrganizationTypeData: OrganizationTypeAdmin[] = new Array<OrganizationTypeAdmin>();
    allDocTypes: DocumentType[] = new Array<DocumentType>();

    currGroupType:GroupType = null;
    currOrgDocTypes: DocumentTypeOrgTypeAdmin[] = new Array<DocumentTypeOrgTypeAdmin>();;

    currOrganization: OrganizationAdmin =null;
    currUsers: RoleAdmin[];
    currUserRole: RoleAdmin;
    showRoleAdminDetails:boolean = false;
    currPermissionDetails:string;
    groupTypes: GroupType[];



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

  onSearch(query: string = '') {
    this.source.setFilter([
      // fields we want to include in the search
      {
        field: 'organizationType',
        search: query
      }
    ], false); 
    // second parameter specifying whether to perform 'AND' or 'OR' search 
    // (meaning all columns should contain search query or at least one)
    // 'AND' by default, so changing to 'OR' by setting false here
  }

  onRowSelect(event) {
    // alert(`Custom event '${event.action}' fired on row with username: ${event.data.username}`)
    this.currGroupType = this.convertOrganizationTypeAdminToOrganizationType(event.data);
    // create alist of specific elements
    for (const orgDocType of this.currOrgDocTypes) {
      //
      // convert and addto data array
      var groupOrgType:any = this.currGroupType.allowedDocTypes.find(x => x.id == orgDocType.id);
      if(groupOrgType != null){
        orgDocType.isActiveinOrg = true;
      }else{
        orgDocType.isActiveinOrg = false;
      }
      // permissions
      orgDocType.permissions = "READ, WRITE, CREATE, DELETE"
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

  saveData(){
    // calculate the specific values
    let allowedDocs:string = '';
    for (const orgDocType of this.currOrgDocTypes) {
      //
      // convert and addto data array
      if(orgDocType.isActiveinOrg === true){
        allowedDocs =  allowedDocs.concat(orgDocType.id + ',');
        // remove from the other group
      }
    }
    // remove last character
    allowedDocs = allowedDocs.slice(0,-1);
    // update
    this.updateAllowedDocs(allowedDocs, this.currGroupType.id);
    // refresh local data
        
  }

  syncWithGlobalGroup(){

  }

  updateAllowedDocs(allowedDocs:string, groupTypeId: number){
    this.organizationService.updateAllowedDocs(allowedDocs, groupTypeId).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }


  convertOrganizationTypeToOrganizationTypeAdmin(organizationType:GroupType){
    var exportItem = new OrganizationTypeAdmin();
    let value:string = LocaleUtils.getInternationalizedString(organizationType.value, this.selectedLanguageChoices[0]);

    exportItem.id = organizationType.id;
    exportItem.organizationType = value;
    //
    // get the number of organizations
    exportItem.organizationsNumber = 0;
    console.log('[Org Type Admin (Admin)] <organizationGroups> DATA ', JSON.stringify(this.organizationGroups));
    for (const organization of this.organizationGroups) {
      console.log('[Org Type Admin (Admin)] <organization> DATA <stict>', organization.groupType.name, organization.groupType.value);
      console.log('[Org Type Admin (Admin)] <organization> DATA <type>', organizationType.name, organizationType.value);
      //
      // convert and addto data array
      if(organization.groupType.name == organizationType.value){
        exportItem.organizationsNumber++;
        // set the allowed docs
        exportItem.allowedDocsNumber = organizationType.allowedDocTypes.length;
      }
    }

    console.log('[Org Type Admin (Admin)] <export item> DATA '.concat(JSON.stringify(exportItem)));
    return exportItem;
  }

  convertDocTypeToGroupType(docType:DocumentType){
    var exportItem = new DocumentTypeOrgTypeAdmin();

    exportItem.id = docType.id;
    exportItem.docTypeName = docType.documentDesignation;
    exportItem.docName = docType.value;
    exportItem.permissions = "READ, WRITE, CREATE, DELETE";
    // roleName: string;
    exportItem.isActiveinOrg = false;
    exportItem.org = null;

    return exportItem;
  }

  convertOrganizationTypeAdminToOrganizationType(inputItem:OrganizationTypeAdmin){
    var groupOrgType:any = this.organizationTypes.find(x => x.id == inputItem.id);
    return groupOrgType;

  }

  getAllOrganizationTypes() {
    console.log('[Org Type Admin (Admin)] <getAllOrganizationTypes> ');
    this.organizationTypes = [];
    this.adminOrganizationTypeData = [];
    this.organizationService.getAllOrganizationTypes().subscribe(
      data => { 
        this.organizationTypes = data;
        console.log('[Org Type Admin (Admin)] <getAllOrganizationTypes> DATA '.concat(JSON.stringify(this.organizationTypes)));
            // convert all the users to admin users
            for (const organizationType of this.organizationTypes) {
                //
                // convert and addto data array
                console.log('[Org Type Admin (Admin)] Row of Organization Data '.concat(JSON.stringify(organizationType)));
                this.adminOrganizationTypeData.push(this.convertOrganizationTypeToOrganizationTypeAdmin(organizationType));
            }
        this.source.load(this.adminOrganizationTypeData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllDocTypes() {
    this.allDocTypes = [];
    this.organizationService.getAllDocTypes().subscribe(
      data => { 
        this.allDocTypes = data;
        // convert all the users to admin users
        for (const docType of this.allDocTypes) {
          //
          // convert and addto data array
          console.log('[Org Type Admin (Admin)] Row of Doc Type Data '.concat(JSON.stringify(docType)));
          this.currOrgDocTypes.push(this.convertDocTypeToGroupType(docType));
      }

      },
      error => console.log('Server Error'),
    );
  }

  getAllOrganizationGroups() {
    console.log('[Org Type Admin (Admin)] <getAllOrganizationGroups> ');
    this.organizationGroups = [];
    this.organizationService.getAllOrganizationGroups().subscribe(
      data => { 
        this.organizationGroups = data;
      },
      error => console.log('Server Error'),
    );
  }

  /**
   * Initialize the paged table's data and look
   */
  initTableData(){
    this.settings = {
      actions: {
        add: false,
        edit: false,
        delete: false,
        },
      pager : {
        display : true,
        perPage:5,
      },
  
      columns: {
        organizationType: {
          title: 'Organization Type',
          type: 'string',
          editable: false,
          filter:false
        },
        organizationsNumber: {
          title: 'Number of Organizations',
          type: 'number',
          editable: false,
          filter:false
        },
        allowedDocsNumber: {
          title: 'Number of Allowed Docs',
          type: 'number',
          editable: false,
          filter:false
        },

      }
    };

  }

  
  getAllStages(){
    this.organizationStages = [];
    this.organizationService.getAllStages().subscribe(
      data => { 
          this.organizationStages = data;
          console.log('[Org Type Admin (Admin)] GET ALL STAGES RESTFUL '.concat(JSON.stringify(data)));
          // 
          // fill in the rest of the data for the stages

          //
          // Set the mapping of the languages to the stages

          // get the value of iterations
          let numberOfStages: number = this.organizationStages.length;

          // stages and headers
          for (var _j = 0; _j < numberOfStages; _j++) {
            // get the resource from the resource map
            let value:string = LocaleUtils.getInternationalizedString(this.organizationStages[_j].name, this.selectedLanguageChoices[0]);
            this.organizationTypelist.push({ value: this.organizationStages[_j].name, title: value });
            console.log('[Org Type Admin (Admin)] Stage '.concat(JSON.stringify(this.organizationTypelist[_j])));
          }
          //
          //
          this.initTableData();
        },
      error => console.log('Server Error'),
    );
  }

  getAvailableLanguages(){
    // TODO read from server
    this.languages  = [ 
      {'id' : 1, 'name' : 'en', 'value': 'English'},
      {'id': 2, 'name' : 'th', 'value': 'Thai'},
      {'id': 3, 'name' : 'vi', 'value': 'Vietnamese'},
      {'id': 4, 'name' : 'vi', 'value': 'Bahasa'},
    ]
  }

  getLanguageKey(value:string){
    for (let language of this.languages) {
      if(language.value === value){
        return language.name;
      }
    }
    return null;
  }


  setChosenLanguage(event: any){
    this.selectedLanguageChoices[0] = this.getLanguageKey(event.target.value);
    console.log('[Org Type Admin (Admin)]  <language> switch ', this.selectedLanguageChoices[0], event, event.target.value);
    this.getAllOrganizationTypes();
  }
}
