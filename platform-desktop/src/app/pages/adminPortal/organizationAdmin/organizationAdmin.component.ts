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
import { LocaleUtils } from '../../../utils/LocaleUtils';

@Component({
  selector: 'organization-admin-table',
  templateUrl: './organizationAdminTable.html'
})
export class OrganizationAdminTable {

  
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
      // this.getAllUsers();
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
    //
    // create the new group data
    var organizationGroup:Group = this.convertOrganizationAdminToOrganizationGroup(event.newData);
    console.log('ORG GROUP ADMIN - Creating a new org group - convert '.concat(JSON.stringify(organizationGroup)));
    if(organizationGroup != null){
      this.createGroupOrganization(organizationGroup);
    }
    this.getAllOrganizationGroups()
  }

  onSearch(query: string = '') {
    this.source.setFilter([
      // fields we want to include in the search
      {
        field: 'organizationName',
        search: query
      },
      {
        field: 'description',
        search: query
      },
      {
        field: 'organizationType',
        search: query
      }
    ], false); 
    // second parameter specifying whether to perform 'AND' or 'OR' search 
    // (meaning all columns should contain search query or at least one)
    // 'AND' by default, so changing to 'OR' by setting false here
  }

  onEditConfirm(event): void {

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
    
    exportItem.description = organization.description;
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


  convertOrganizationAdminToOrganizationGroup(data:any){
    var organizationGroup:Group = new Group();
    console.log('ORG GROUP ADMIN - Creating a new org group '.concat(JSON.stringify(data)));
    organizationGroup.name = data.organizationName;
    organizationGroup.description = data.description;
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
          title: 'Organization Name',
          type: 'string',
          filter: false
        },
        description: {
          title: 'Description',
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

  getAvailableLanguages(){
    // TODO read from server
    this.languages  = [ 
      {'id' : 1, 'name' : 'en', 'value': 'English'},
      {'id': 2, 'name' : 'th', 'value': 'Thai'},
      {'id': 3, 'name' : 'vi', 'value': 'Vietnamese'},
      {'id': 4, 'name' : 'vi', 'value': 'Bahasa'},
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

}
