import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { LookupEntity } from '../../../models/LookupEntity';
import { AppResource } from '../../../models/AppResource';
import { LocaleUtils } from '../../../utils/locale.utils';

import { DocumentType } from '../../../models/documentType';
import { DataLoadService } from '../dataLoad.service';
import { DocTypeDataTableHelper } from './DocTypeDataTableHelper';
import { GroupType } from 'app/models/groupType';
import { OrgTypeDocumentTypeAdmin } from 'app/models/admin/OrgTypeDocumentTypeAdmin';


@Component({
  selector: 'doc-type-admin-table',
  templateUrl: './docTypeAdminTable.html'
})
export class DocTypeAdminTable {

  
    // the specific supported languages
    languages:LookupEntity[];
    query: string = '';
    selectedLanguageChoices:string[]=['en', 'vi', 'th'];

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
        perPage:10,
      },
  
      columns: {
        documentDesignation: {
          title: 'Document Type',
          type: 'string',
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: [
                  { value: 'Profile', title: 'Profile' }, 
                  { value: 'Passthrough', title: 'Passthrough' }, 
                ],
            },
          },
          filter: false
        },
        value1: {
          title: 'Document Name',
          type: 'string',
          filter: false
        },
        value2: {
          title: 'Document Name',
          type: 'string',
          filter: false
        },
        id: {
          title: 'id',
          type: 'number',
          width: '0px',
          filter: false,
          editable : false,
          hidden: true
        },
      }
    };

    onSearch(query: string = '') {
      this.source.setFilter([
        // fields we want to include in the search
        {
          field: 'documentDesignation',
          search: query
        },
        {
          field: 'value1',
          search: query
        },
        {
          field: 'value2',
          search: query
        }
      ], false); 
      // second parameter specifying whether to perform 'AND' or 'OR' search 
      // (meaning all columns should contain search query or at least one)
      // 'AND' by default, so changing to 'OR' by setting false here
    }
  
    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.getAvailableLanguages();
      this.getAllDocTypes();
      this.getAllOrganizationTypes();
      
    }
  
  
    source: LocalDataSource = new LocalDataSource();
  
    // Admin data
    //
    
    // All the doc types for this applciation
    allDocTypes: DocumentType[] = new Array<DocumentType>();
    // Mapped doc types to specific languages.
    allDocTypesLanguageMapped: Map<string, DocumentType[]> = new Map<string, DocumentType[]>();
    organizationTypes: GroupType[] = new Array<GroupType>();
    //
    // selection data
    currDocTypeId: number;
    currDocType: DocumentType;
    currDocTypeOrgTypes: OrgTypeDocumentTypeAdmin[] = new Array<OrgTypeDocumentTypeAdmin>();;

  constructor(protected organizationService : DataLoadService) {
  }

  
  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete this row?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onRowSelect(event) {
      // Init
      this.currDocTypeOrgTypes = new Array<OrgTypeDocumentTypeAdmin>();


      // get the doc type id
      this.currDocTypeId = event.data.id;
      var tempDocType:any = this.allDocTypes.find(x => x.id == this.currDocTypeId);
      if(tempDocType != null){
        this.currDocType = tempDocType;
      }
      // get the specific org types
      for (const orgDocType of this.organizationTypes) {
        //
        // convert and addto data array
        let tempDocType = new OrgTypeDocumentTypeAdmin();
        tempDocType.org = orgDocType;
        tempDocType.name = orgDocType.name;

        var groupOrgType:any = orgDocType.allowedDocTypes.find(x => x.id == this.currDocTypeId);
        if(groupOrgType != null){
          // set the data
          tempDocType.isActiveinOrg = true;
        }else{
          tempDocType.isActiveinOrg = false;
        }
        // only include actual stages
        if(tempDocType.name != null && tempDocType.name.length > 0){
          this.currDocTypeOrgTypes.push(tempDocType);
        }
        
      }
  }

  onCreateConfirm(event): void {
      if (window.confirm('Are you sure you want to create this row?')) {
        event.confirm.resolve();
      } else {
        event.confirm.reject();
      }
      let resourceKey:string;
      var docType1:DocumentType = this.convertToDocType(event.newData, 0);
      var docType2:DocumentType = this.convertToDocType(event.newData, 1);
      var docTypeEmpty:DocumentType = this.convertToDocTypeEmpty(event.newData);
      console.log('[Created New Doc Type --> DocumentType '.concat(JSON.stringify(docType1)));
      if(docType1 == null || docType2 == null){
        return;
      }else{
        // generate the key
        resourceKey = this.generateDocTypeKey(docType1);
        docType1.id = 0;
        docType2.id = 0;
        docType1.name = resourceKey;
        docType2.name = resourceKey;
        docTypeEmpty.name = resourceKey;
      }
      //
      // Process the rest
    
      //
      // First doc
      this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0]).push(docType1);
      this.saveDocType(docType1);
      let resource1:AppResource = new AppResource();
      resource1.key = resourceKey;
      resource1.value = docType1.value;
      resource1.locale = this.selectedLanguageChoices[0];
      this.saveResource(resource1);
      LocaleUtils.addResourceToResourceMap(resource1);
      //
      // Second doc
      this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1]).push(docType2);
      let resource2:AppResource = new AppResource();
      resource2.key = resourceKey;
      resource2.value = docType2.value;
      resource2.locale = this.selectedLanguageChoices[1];
      this.saveResource(resource2);
      LocaleUtils.addResourceToResourceMap(resource2);

    
      for (let language of this.languages) {
        if(language.name != this.selectedLanguageChoices[0] && language.name != this.selectedLanguageChoices[1]){
          // <TODO> Add the other possible values
          this.allDocTypesLanguageMapped.get(language.name).push(docTypeEmpty);
          let resource:AppResource = new AppResource();
          resource.key = resourceKey;
          resource.value = docType1.value;
          resource.locale = language.name;
          this.saveResource(resource);
          LocaleUtils.addResourceToResourceMap(resource);

        }
      }
  }

  onEditConfirm(event): void {
    if (window.confirm('Are you sure you want to edit and save this row?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    var docType:DocumentType = this.convertToDocType(event.newData, 0);
    if(docType != null){
      this.updateDocType(docType);
      let resource:AppResource = new AppResource();
      resource.key = docType.name;
      resource.value = docType.value;
      resource.locale = this.selectedLanguageChoices[0];
      this.saveResource(resource);
      LocaleUtils.addResourceToResourceMap(resource);
    }
    docType = this.convertToDocType(event.newData, 1);
    if(docType != null){
      let resource:AppResource = new AppResource();
      resource.key = docType.name;
      resource.value = docType.value;
      resource.locale = this.selectedLanguageChoices[1];
      this.saveResource(resource);
      LocaleUtils.addResourceToResourceMap(resource);
      //
      //
    }
  }


  /**
   * Simple conversion routine
   * @param inputItem  - convert the smart table row to the specific DocumentType instance
   */
  convertToDocType(inputItem: any, index: number){
    let docType: DocumentType = new DocumentType();
    docType.documentDesignation = inputItem.documentDesignation;
    docType.hexColorCode = DocumentType.DEFAULT_HEX_COLOR;
    docType.name = inputItem.name; //DocumentType.KEY_PREFIX_INTERNATIONALIZATION + inputItem.value.replace(' ', '_');
    if(index == 0){
      docType.value = inputItem.value1;
    }else{
      docType.value = inputItem.value2;
    }
    docType.id = inputItem.id
    
    //
    return docType;
  } 

  convertToDocTypeEmpty(inputItem: any){
    let docType: DocumentType = new DocumentType();
    docType.documentDesignation = inputItem.documentDesignation;
    docType.hexColorCode = DocumentType.DEFAULT_HEX_COLOR;
    docType.name = inputItem.name; //DocumentType.KEY_PREFIX_INTERNATIONALIZATION + inputItem.value.replace(' ', '_');
    //
    return docType;
  }


  getAllDocTypes() {
    this.organizationService.getAllDocTypes().subscribe(
      data => { 
        this.allDocTypes = data;
        // map data
        for (let language of this.languages) {
          this.convertDocTypes(language.name, this.allDocTypes);
        }
        console.log('[Locale Services - Getting All Doc Types =====] '.concat(JSON.stringify(this.allDocTypesLanguageMapped.get('en'))));
        this.source.load(this.convertToTableFormat(
                                    this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
                                  , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
                          )
                        );
      },
      error => console.log('Server Error'),
    );
  }

  saveDocType(docType: DocumentType) {
    this.organizationService.createNewDocumentType(docType).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  updateDocType(docType: DocumentType) {
    this.organizationService.updateExistingDocumentType(docType).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  saveResource(resource: AppResource) {
    this.organizationService.createNewResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  convertDocTypes(language:string, docTypes: DocumentType[]){
    let tempDocTypes: DocumentType[] = new Array<DocumentType>();
    console.log('[Locale Services - DocType Admin --- ] ');

    for (let docType of docTypes) {
      //
      // Map the value to inner list
      let value = LocaleUtils.getInternationalizedString(docType.name, language);
      console.log('[Locale Services - DocType Admin ] '.concat(docType.name).concat(':').concat(language+'  ').concat(value + ' ').concat(JSON.stringify(docType)));
      
      let clonedType = DocumentType.clone(docType);
      clonedType.value = value;
      tempDocTypes.push(clonedType); 
      console.log('[Locale Services - DocType Admin Array ] '.concat(clonedType.name).concat(':').concat(language+'  ').concat(value + ' ').concat(JSON.stringify(clonedType)));   
    } 
    console.log('[Locale Services - DocType Admin Array FULL =====] '.concat(language+'  ').concat(JSON.stringify(tempDocTypes))); 
    // add to the map
    this.allDocTypesLanguageMapped.set(language, tempDocTypes);
    console.log('[Locale Services - DocType Admin Array FULL MAPPED =====] '.concat(language+'  ').concat(JSON.stringify(this.allDocTypesLanguageMapped.get('en')))); 
  }

  getAllOrganizationTypes() {
    console.log('[Org Type Admin (Admin)] <getAllOrganizationTypes> ');
    this.organizationTypes = [];
    this.organizationService.getAllOrganizationTypes().subscribe(
      data => { 
        this.organizationTypes = data;
      },
      error => console.log('Server Error'),
    );
  }


  getAvailableLanguages(){
    // TODO read from server
    this.languages  = [ 
      {'id' : 1, 'name' : 'en', 'value': 'English'},
      {'id': 2, 'name' : 'vi', 'value': 'Vietnamese'},
      {'id': 3, 'name' : 'th', 'value': 'Thai'},
      {'id': 4, 'name' : 'in', 'value': 'Bahasa'},
      {'id': 5, 'name' : 'es', 'value': 'Spanish'},
      {'id': 6, 'name' : 'hi', 'value': 'Hindi'},
      {'id': 7, 'name' : 'te', 'value': 'Telugu'},
    ]
  }

  /**
   * 
   * @param event 
   */
  setChosenLanguage1(event: any){
    this.selectedLanguageChoices[0] = event.target.value;
    if(event.target.value === 'English'){
      this.selectedLanguageChoices[0] = 'en';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }
    if(event.target.value === 'Thai'){
      this.selectedLanguageChoices[0] = 'th';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }
    if(event.target.value === 'Vietnamese'){
      this.selectedLanguageChoices[0] = 'vi';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }

    if(event.target.value === 'Spanish'){
      this.selectedLanguageChoices[0] = 'es';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }

    if(event.target.value === 'Bahasa'){
      this.selectedLanguageChoices[0] = 'in';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }

    if(event.target.value === 'Hindi'){
      this.selectedLanguageChoices[0] = 'hi';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }

    if(event.target.value === 'Telugu'){
      this.selectedLanguageChoices[0] = 'te';
      this.source.load(this.convertToTableFormat(
          this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
        , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }
  }

  setChosenLanguage2(event: any){
    this.selectedLanguageChoices[1] = event.target.value;
    if(event.target.value === 'English'){
      this.selectedLanguageChoices[1] = 'en';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      
      return;
    }
    if(event.target.value === 'Thai'){
      this.selectedLanguageChoices[1] = 'th';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }

    if(event.target.value === 'Vietnamese'){
      this.selectedLanguageChoices[1] = 'vi';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }

    if(event.target.value === 'Spanish'){
      this.selectedLanguageChoices[1] = 'es';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }

    if(event.target.value === 'Bahasa'){
      this.selectedLanguageChoices[1] = 'in';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }

    if(event.target.value === 'Hindi'){
      this.selectedLanguageChoices[1] = 'hi';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }

    if(event.target.value === 'Telugu'){
      this.selectedLanguageChoices[1] = 'te';
      this.source.load(this.convertToTableFormat(
            this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[0])
          , this.allDocTypesLanguageMapped.get(this.selectedLanguageChoices[1])
        )
      );
      return;
    }
  }

  generateDocTypeKey(docType:DocumentType){
    let stripped:string = docType.value.replace(' ', '.');
    return 'document.type.' + stripped;
  }

  convertToTableFormat(valueList1: DocumentType[], valueList2:DocumentType[]){
    let dataHelperList:DocTypeDataTableHelper[] = new Array<DocTypeDataTableHelper>();
    for (var _i=0; _i<valueList1.length; _i++) {
      dataHelperList.push(this.createDocTypeHelperEntity(valueList1[_i], valueList2[_i]));
    }

    return dataHelperList;
  }

  getCurrentDocType() {
      if(this.currDocType != null){
        return this.currDocType.value;
      } else {
        var tempDocType:DocumentType = new DocumentType();
        tempDocType.name = "New Document Type";
        return tempDocType.value;
      }
  }

  createDocTypeHelperEntity(value1:DocumentType, value2:DocumentType){
    var dataHelper:DocTypeDataTableHelper = new DocTypeDataTableHelper();
    dataHelper.documentDesignation = value1.documentDesignation;
    dataHelper.value1 = value1.value;
    dataHelper.value2 = value2.value;
    dataHelper.id = value1.id
    return dataHelper;

  }
}
