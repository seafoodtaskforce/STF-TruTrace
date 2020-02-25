import { Component, OnInit } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from '../../../models/user';
import { DocumentTag } from '../../../models/documentTag';

import { TagAdmin } from '../../../models/admin/tagAdmin';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';



import {DataLoadService} from '../dataLoad.service';

@Component({
  selector: 'tag-admin-table',
  templateUrl: './tagAdminTable.html'
})
export class TagAdminTable implements OnInit{

  
    query: string = '';
    tagCustomPrefixlist:SmartTableListItem[] = [
      { value: 'Other', title: 'Other' }, 
      { value: 'Invoice #', title: 'Invoice #' }, 
      { value: 'MD', title: 'MD' }, 
      { value: 'FMD',title: 'FMD'},
      { value: 'FIF',title: 'FIF'},
    ];

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
        tagPrefix: {
          title: 'Tag Prefix',
          type: 'string',
                  editable: true,
                  editor: {
                    type: 'list',
                    config: {
                      selectText: 'Select...',
                      list: this.tagCustomPrefixlist,
                    },
                  },
                  filter: {
                    type: 'list',
                    config: {
                      selectText: 'Select...',
                      list: this.tagCustomPrefixlist,
                    },
                  },
        },
        tagName: {
          title: 'Tag Text',
          type: 'string'
        },
      }
    };
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      // this.getAllUsers();
      this.getAllTags();
    }

    ngAfterViewInit() {
      document.getElementsByClassName('tagPrefix')['0'].style.width = '175px'
    }
  
  
    source: LocalDataSource = new LocalDataSource();

    //
    // Tag Admin
    allTags: DocumentTag[] = new Array<DocumentTag>();
    allAdminTags: TagAdmin[] = new Array<TagAdmin>();
    currTag: DocumentTag =null;


    // user data
    allUsers: Array<User>;

  constructor(protected tagService : DataLoadService) {
    
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
    var newTag: DocumentTag = this.convertTagAdminToTagData(event.newData);
    console.log('ORG GROUP ADMIN - Creating a new tag - convert '.concat(JSON.stringify(newTag)));
    if(newTag != null){
      this.createTag(newTag);
    }
  }

  onEditConfirm(event): void {

  }


  getAllTags() {
    this.tagService.getAllTags().subscribe(
      data => { 
        this.allTags = data;
            // convert all the users to admin users
            for (const tag of this.allTags) {
                //
                // convert and addto data array
                console.log('[Smart Tables Service getAllOrganizationGroups] Row of Organization Data '.concat(JSON.stringify(tag)));
                this.allAdminTags.push(this.convertTagDataToTagAdmin(tag));
            }
        this.source.load(this.allAdminTags);
      },
      error => console.log('Server Error'),
    );
  }

  createTag(newTag: DocumentTag){
    this.tagService.createNewTag(newTag).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
    );
  }

  convertTagDataToTagAdmin(item:any){
    var exportItem = new TagAdmin();

    exportItem.id = item.id;
    if(item.text.indexOf('CUSTOM: ') == -1){ 
      exportItem.tagName = item.text;
    }else{
      exportItem.tagName = item.text.replace('CUSTOM: ', '');
    }
    exportItem.tagPrefix = item.customPrefix;
    exportItem.custom = item.custom;
    return exportItem;
  }

  convertTagAdminToTagData(tagAdmin: TagAdmin ){
    //
    var newTag: DocumentTag = new DocumentTag();

    if(tagAdmin.tagPrefix === 'Other'){
      newTag.customPrefix = '';
      newTag.custom = false;
    }else{
      newTag.custom = true;
      newTag.customPrefix = tagAdmin.tagPrefix;
    }
    
    // get the organization id
    newTag.organizationId = this.getUserGroupId();
    // get the text
    newTag.text = tagAdmin.tagName;

    return newTag;

  }

  getUserGroupId() {
    let user: User;
    let result: number = 0;
    user = JSON.parse(localStorage.getItem('user'));
    result = user.userOrganizations[0].id;
    return result;
  }
}
