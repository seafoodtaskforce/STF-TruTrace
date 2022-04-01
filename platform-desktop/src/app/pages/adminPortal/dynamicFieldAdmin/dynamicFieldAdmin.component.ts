import { Component, OnInit } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from '../../../models/user';
import { DocumentTag } from '../../../models/documentTag';
import { DocumentType } from '../../../models/DocumentType';

import { TagAdmin } from '../../../models/admin/tagAdmin';
import {SmartTableListItem} from '../../../models/admin/smartTableListItem';



import {DataLoadService} from '../dataLoad.service';
import { DynamicFieldDefinition } from 'app/models/dynamicFieldDefinition';
import { LookupEntity } from 'app/models/lookupEntity';

@Component({
  selector: 'dynamic-field-admin-table',
  templateUrl: './dynamicFieldAdminTable.html'
})
export class DynamicFieldAdminTable implements OnInit {

    settings: any;
    query: string = '';
    dynamicFieldTypesList:SmartTableListItem[] = [
      { value: 'Numeric', title: 'Numeric' }, 
      { value: 'AlphaNumeric', title: 'AlphaNumeric' },
      { value: 'Date', title: 'Date' },
      { value: 'Expiry Date', title: 'Expiry Date' }
    ];
    requiredList:SmartTableListItem[] = [
        { value: 'Required', title: 'Required' }, 
        { value: 'Not Required', title: 'Not Required' }
      ];

    docIdListValue:SmartTableListItem[] = [
      { value: 'Yes', title: 'Yes' }, 
      { value: 'No', title: 'No' }
    ];

    requiredDocumentTypeList : SmartTableListItem[] = new Array<SmartTableListItem>();

       // the specific supported languages
       languages:LookupEntity[];
       selectedLanguageChoices:string[]=['en', 'th'];


  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      // this.getAllUsers();
      this.getAllDocTypes()
      this.getAllDynamicFieldDefinitions();
      this.getAvailableLanguages();
    }

    ngAfterViewInit() {
      //document.getElementsByClassName('tagPrefix')['0'].style.width = '175px'
    }

    source: LocalDataSource = new LocalDataSource();

    

    //
    // Dynamic fields Admin
    allDocTypes: DocumentType[] = new Array<DocumentType>();
    allDynamicFieldDefinitions: DynamicFieldDefinition[] = new Array<DynamicFieldDefinition>();

    allTags: DocumentTag[] = new Array<DocumentTag>();
    allAdminTags: TagAdmin[] = new Array<TagAdmin>();
    currTag: DocumentTag =null;

  constructor(protected dynamicFieldsService : DataLoadService) {
    
  }

  
  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onCreateConfirm(event): void {
    if (window.confirm('Are you sure you want to create a new Field Definition?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    //
    // create the new group data
    var definition: DynamicFieldDefinition = this.convertTableRowToDynamicFieldDefinition(event.newData);
    console.log('ORG GROUP ADMIN - Creating a new field definition - convert '.concat(JSON.stringify(definition)));
    if(definition != null){
      this.createDynamicFieldDefinition(definition);
    }
  }




  onEditConfirm(event): void {
    if (window.confirm('Are you sure you want to edit this Field Definition?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    //
    // update Doc Definition Data
    var definition: DynamicFieldDefinition = this.convertTableRowToDynamicFieldDefinition(event.newData);
    console.log('ORG GROUP ADMIN - Creating a new field definition - convert '.concat(JSON.stringify(definition)));

    if(definition != null){
      this.updateDynamicFieldDefinition(definition);
    }
  }

  onSearch(query: string = '') {
    this.source.setFilter([
      // fields we want to include in the search
      {
        field: 'docTypeName',
        search: query
      },
      {
        field: 'displayName',
        search: query
      },
      {
        field: 'description',
        search: query
      },
      {
        field: 'fieldType',
        search: query
      },
      {
        field: 'minLength',
        search: query
      },
      {
        field: 'isRequired',
        search: query
      }
    ], false); 
    // second parameter specifying whether to perform 'AND' or 'OR' search 
    // (meaning all columns should contain search query or at least one)
    // 'AND' by default, so changing to 'OR' by setting false here
  }

  onRowSelect(event) {
    // alert(`Custom event '${event.action}' fired on row with username: ${event.data.username}`)

    //this.currUser = this.convertUserAdminToUser(event.data);
    //this.fetchRoleAdminData();
    console.log('[Smart Tables Service] CURR field Def Selection'.concat(JSON.stringify(event.data)));
  }

  createDynamicFieldDefinition(newDefinition: DynamicFieldDefinition){
    this.dynamicFieldsService.createDynamicFieldDefinition(newDefinition).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
    );
  }

  updateDynamicFieldDefinition(newDefinition: DynamicFieldDefinition){
    this.dynamicFieldsService.updateDynamicFieldDefinition(newDefinition).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
    );
  }




  getUserGroupId() {
    let user: User;
    let result: number = 0;
    user = JSON.parse(localStorage.getItem('user'));
    result = user.userOrganizations[0].id;
    return result;
  }

  getAllDocTypes() {
    this.dynamicFieldsService.getAllDocTypes().subscribe(
      data => { 
        this.allDocTypes = data;
        for (var _i=0; _i<this.allDocTypes.length; _i++) {
            this.requiredDocumentTypeList.push(
                { value: this.allDocTypes[_i].value, title: this.allDocTypes[_i].value }
            );
            console.log('[Smart Tables Service <DynamicFieldAdminTable>] Row of Doc Type Data '.concat(JSON.stringify({ value: ''+this.allDocTypes[_i].id, title: this.allDocTypes[_i].value })));
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
        
            // Doc Type, Display Name, Description, Field Type, Min Length, Max Length, Mask
            columns: {
              docTypeName: {
                  title: 'Document Type',
                  type: 'string',
                  editable: true,
                  editor: {
                    type: 'list',
                    config: {
                      selectText: 'Select...',
                      list: this.requiredDocumentTypeList,
                    },
                  },
                  filter: false
              },
              displayName: {
                  title: 'Input Title',
                  type: 'string',
                  filter: false
              },
              description: {
                  title: 'Description',
                  type: 'string',
                  filter: false
              },
              isDocIdText: {
                title: 'Doc Id?',
                type: 'boolean',
                editable: true,
                editor: {
                  type: 'list',
                  config: {
                    selectText: 'Select...',
                    list: this.docIdListValue,
                  },
                },
                filter: false
              },
              fieldType: {
                title: 'Field Type',
                type: 'string',
                        editable: true,
                        editor: {
                          type: 'list',
                          config: {
                            selectText: 'Select...',
                            list: this.dynamicFieldTypesList,
                          },
                        },
                        filter: false
              },
              maxLength: {
                  title: 'Maximum Length',
                  type: 'number',
                  filter: false
              },
              ocrMatchText: {
                title: 'OCR Match Text',
                type: 'string',
                filter: false
              },
              ocrGrabLength: {
                title: 'OCR Grab Length',
                type: 'number',
                filter: false
              },
              isRequiredText: {
                  title: 'Required?',
                  type: 'boolean',
                  editable: true,
                  editor: {
                    type: 'list',
                    config: {
                      selectText: 'Select...',
                      list: this.requiredList,
                    },
                  },
                  filter: false
              },
              id:{
                  title: 'id',
                  type: 'number',
                  width: '40px',
                  editable : false,
                  filter: false
              },              
            }
          };
          //this.source.load(this.requiredDocumentTypeList);
      },
      error => console.log('Server Error'),
    );
  }

  getAllDynamicFieldDefinitions(){
    this.dynamicFieldsService.getAllDynamicFieldDefinitions().subscribe(
        data => { 
          this.allDynamicFieldDefinitions = data;
          for (var _i=0; _i<this.allDynamicFieldDefinitions.length; _i++) {
            if(this.allDynamicFieldDefinitions[_i].isRequired == true){
              this.allDynamicFieldDefinitions[_i].isRequiredText = 'Required'
            }else {
              this.allDynamicFieldDefinitions[_i].isRequiredText = 'Not Required'
            }
            if(this.allDynamicFieldDefinitions[_i].isDocId == true){
              this.allDynamicFieldDefinitions[_i].isDocIdText = 'Yes'
            }else {
              this.allDynamicFieldDefinitions[_i].isDocIdText = 'No'
            }
          }
          console.log('[Locale Services - Getting All Dynamic Field Defintions =====] '.concat(JSON.stringify(this.allDynamicFieldDefinitions)));
          this.source.load(this.allDynamicFieldDefinitions);
        },
        error => console.log('Server Error'),
      );
  }

  convertTableRowToDynamicFieldDefinition(row : any){
    var definition:DynamicFieldDefinition = this.allDynamicFieldDefinitions.find(x => x.id == row.id);
    if(definition == null){
      definition = new DynamicFieldDefinition();
    }

    definition.description = row.description;
    definition.docTypeName = row.docTypeName;
    definition.displayName = row.displayName;
    definition.maxLength = row.maxLength;
    definition.isRequired = this.convertRequiredToBoolean(row.isRequiredText);
    if(row.maxLength == null || row.maxLength == ''){
      definition.maxLength = 0;
    }else{
      definition.maxLength = row.maxLength;
    }
    definition.isDocId = this.convertIsDocIdToBoolean(row.isDocIdText);
    
    definition.ordinal = 0;

    definition.ocrMatchText = row.ocrMatchText;
    definition.ocrGrabLength = this.convertOCRGrabLengthToInt(row.ocrGrabLength);

    definition.orgID = this.getUserGroupId();
    definition.fieldTypeId = this.getFieldTypeId(row.fieldType);
    definition.docTypeId = this.getDocumentTypeId(row.docTypeName);

    return definition;
  }

  getDocumentTypeId(name : string){
    for (var _i=0; _i<this.allDocTypes.length; _i++) {
        if(this.allDocTypes[_i].value == name){
            return this.allDocTypes[_i].id
        }
      }
  }

  convertRequiredToBoolean(text : string){
    if(text == 'Required'){
      return true;
    } else {
      return false;
    }
  }

  convertIsDocIdToBoolean(text : string){
    if(text == 'Yes'){
      return true;
    } else {
      return false;
    }
  }

  convertOCRGrabLengthToInt(text : string){
    if(text.length == 0){
      return 0;
    } else {
      return +text;
    }
  }

  getFieldTypeId(name : string){
    
    if(name == 'Numeric'){
        return 2;
    }
    if(name == 'AlphaNumeric'){
        return 3;
    }
    if(name == 'Date'){
      return 4;
    }
    if(name == 'Expiry Date'){
      return 5;
    }
  }

  getRequired(value : string){
    
    if(value == 'Required' ){
        return true;
    }else {
        return false;
    }
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
