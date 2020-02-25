import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { UserAdmin } from '../../../../../models/admin/userAdmin';
import { User } from '../../../../../models/user';
import { GroupType } from '../../../../../models/groupType';
import { RoleAdmin } from '../../../../../models/admin/roleAdmin';
import { DocumentTypeAdmin } from '../../../../../models/admin/documentTypeAdmin';
import { DocumentTypeOrgTypeAdmin } from '../../../../../models/admin/DocumentTypeOrgTypeAdmin';

import { DocumentType } from '../../../../../models/DocumentType';

import { OrganizationTypeAdmin } from '../../../../../models/admin/organizationTypeAdmin';
import { OrganizationAdmin } from '../../../../../models/admin/organizationAdmin';
import { Group } from '../../../../../models/group';



import {DataLoadService} from '../dataLoad.service';

@Component({
  selector: 'organization-type-admin-table',
  templateUrl: './organizationTypeAdminTable.html'
})
export class OrganizationTypeAdminTable {

  
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
        organizationsNumber: {
          title: 'Number of Organizations',
          type: 'string',
          editable: false
        },
        allowedDocsNumber: {
          title: 'Number of Allowed Docs',
          type: 'string',
          editable: false
        },

      }
    };
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.getAllOrganizationGroups();
      this.getAllOrganizationTypes();
      this.getAllDocTypes();
    }
  
  
    source: LocalDataSource = new LocalDataSource();
  
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

    exportItem.id = organizationType.id;
    exportItem.organizationType = organizationType.name;
    //
    // get the number of organizations
    exportItem.organizationsNumber = 0;
    for (const organization of this.organizationGroups) {
      //
      // convert and addto data array
      if(organization.groupType.name == organizationType.name){
        exportItem.organizationsNumber++;
        // set the allowed docs
        exportItem.allowedDocsNumber = organizationType.allowedDocTypes.length;
      }
    }
    return exportItem;
  }

  convertDocTypeToGroupType(docType:DocumentType){
    var exportItem = new DocumentTypeOrgTypeAdmin();

    exportItem.id = docType.id;
    exportItem.docTypeName = docType.documentDesignation;
    exportItem.docName = docType.value;
    // permissions:  string;
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
    this.organizationService.getAllOrganizationTypes().subscribe(
      data => { 
        this.organizationTypes = data;
            // convert all the users to admin users
            for (const organizationType of this.organizationTypes) {
                //
                // convert and addto data array
                console.log('[Smart Tables Service] Row of Organization Data '.concat(JSON.stringify(organizationType)));
                this.adminOrganizationTypeData.push(this.convertOrganizationTypeToOrganizationTypeAdmin(organizationType));
            }
        this.source.load(this.adminOrganizationTypeData);
      },
      error => console.log('Server Error'),
    );
  }

  getAllDocTypes() {
    this.organizationService.getAllDocTypes().subscribe(
      data => { 
        this.allDocTypes = data;
        // convert all the users to admin users
        for (const docType of this.allDocTypes) {
          //
          // convert and addto data array
          console.log('[Smart Tables Service] Row of Organization Data '.concat(JSON.stringify(docType)));
          this.currOrgDocTypes.push(this.convertDocTypeToGroupType(docType));
      }

      },
      error => console.log('Server Error'),
    );
  }

  getAllOrganizationGroups() {
    this.organizationService.getAllOrganizationGroups().subscribe(
      data => { 
        this.organizationGroups = data;
      },
      error => console.log('Server Error'),
    );
  }





}
