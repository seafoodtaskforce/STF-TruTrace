import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from '../../../models/user';
import { Group } from '../../../models/group';

import {SmartTableListItem} from '../../../models/admin/smartTableListItem';

import {DataLoadService} from '../dataLoad.service';
import { DocumentService } from 'app/pages/documents/document.service';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';
import { ToasterService } from 'app/toaster-service.service';
import { AppResource } from 'app/models/AppResource';

@Component({
  selector: 'resources-admin-table',
  templateUrl: './resourceAdminTable.html'
})
export class ResourceAdminTable {

  
    query: string = '';
    platform:SmartTableListItem[] = [
      { value: 'Desktop', title: 'Desktop' }, 
      { value: 'Android', title: 'Android' }, 
      { value: 'iOS',title: 'iOS'},
      ];

    localeList:SmartTableListItem[];

    type:SmartTableListItem[] = [
      { value: 'Page', title: 'Page' }, 
      { value: 'Stage', title: 'Stage' }, 
      { value: 'Template',title: 'Template'},
      ];

    subType:SmartTableListItem[] = [
      { value: 'Header', title: 'Header' }, 
      { value: 'Footer', title: 'Footer' }, 
      { value: 'Button',title: 'Button'},
      { value: 'Label',title: 'Label'},
      { value: 'Menu',title: 'Menu'},
      { value: 'Drop Down',title: 'Drop Down'},
      { value: 'email',title: 'email'},
      ];

    //
    // dropdown lists
    platformTypeList:SmartTableListItem[];
    resourceTypeList:SmartTableListItem[];
    resourceSubTypeList:SmartTableListItem[];
    resourceLocaleList:SmartTableListItem[];
    //
    // drop down
    emailTemplates = new Map();
    resourceLocaleMap = new Map();
    platformTypeMap = new Map();
    resourceSubTypeMap = new Map();
    resourceTypeMap = new Map();
    organizationGroups:Group[];

    settings: any;

    //
    // Editor
    public ckeditorContent:string = '<p>Hello CKEditor</p>';
    public config = {
      uiColor: '#F0F3F4',
      height: '500',
    };

    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.loggedInName = localStorage.getItem('username');
      this.resourceTypeList = new Array<SmartTableListItem>();
      this.resourceSubTypeList = new Array<SmartTableListItem>();
      this.platformTypeList = new Array<SmartTableListItem>();
      this.resourceLocaleList = new Array<SmartTableListItem>();

      this.localeList = new Array<SmartTableListItem>();
      this.emailTemplates = new Map();
      this.resourceLocaleMap = new Map();
      this.platformTypeMap = new Map();
      this.resourceSubTypeMap = new Map();
      this.resourceTypeMap = new Map();
  
      // fix up the 
      
      this.getAllServerResources();
  
      console.log('[User Admin Table] Reource List Item ngOnInit '.concat(JSON.stringify(this.resourceData)));
    }

    ngAfterViewInit() {

      //document.getElementsByClassName('locale')['0'].style.width = '100px';
      //document.getElementsByClassName('type')['0'].style.width = '250px';
      //document.getElementsByClassName('Meta Type')['0'].style.width = '200px';
      //document.getElementsByClassName('Platform')['0'].style.width = '100px';

    }
    source: LocalDataSource = new LocalDataSource();
  

    currUser: User =null;
    loggedInName :string;

    //
    // Resource Admin
    resourceData : AppResource[] = new Array<AppResource>();

    constructor(protected resourceService : DataLoadService, protected _documentService: DocumentService
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
    console.log('[Removing a Resource] --> Resource '.concat(JSON.stringify(event.newData)));
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
    console.log('[Creating NewUser] --> AppResource '.concat(JSON.stringify(event.newData)));
    var resource:AppResource = event.newData;
    console.log('[Created New App Resource] --> User '.concat(JSON.stringify(resource)));
    if(resource != null){
      this.createAppResource(resource);
    }
  }

  onEditConfirm(event): void {
    if (window.confirm('Are you sure you want to edit?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
    var resource:AppResource = event.newData;
    console.log('[Updated Resource] --> User '.concat(JSON.stringify(resource)));
    if(resource != null){
      this.updateAppResource(resource);
    }
  }

  getAllServerResources() {
    this.resourceService.getAllServerResources().subscribe(
      data => { 
        this.resourceData = data;

            for (const resource of this.resourceData) {
                //
                // Fetch the drop down lists
                //var typeIndex:number = this.resourceTypeList.indexOf({ value: resource.type, title: resource.type });
                //var subTypeIndex:number = this.resourceSubTypeList.indexOf({ value: resource.subType, title: resource.subType });
                //var platformTypeIndex:number = this.platformTypeList.indexOf({ value: resource.platfrom, title: resource.platfrom });
                //var resourceLocaleIndex:number = this.resourceLocaleList.indexOf({ value: resource.locale, title: resource.locale });
                
                //if(typeIndex == -1){
                //  this.resourceTypeList.push({ value: resource.type, title: resource.type });
                //}
                //if(subTypeIndex == -1){
                //  this.resourceSubTypeList.push({ value: resource.subType, title: resource.subType });
                //}
                //if(platformTypeIndex == -1){
                //  this.platformTypeList.push({ value: resource.platfrom, title: resource.platfrom});
                //}
                //if(resourceLocaleIndex == -1){
                //  this.resourceLocaleList.push({ value: resource.locale, title: resource.locale});
                //}
                //
                // convert and addto data array
                //this.resourceData.push(resource);

                //
                // Encode resources that are too long to show
                // 
                if(resource.value.length > 300){
                  //
                  // 
                  this.emailTemplates.set(resource.key, resource.value);
                  resource.value = "Too Long... see in editor"
                }

                //
                // Extract Lists of drop down values for filters
                // 

                //
                // Locale List
                this.resourceLocaleMap.set(resource.locale, resource.locale);    
                //
                // platform map
                this.platformTypeMap.set(resource.platform, resource.platform);   
                //
                // Meta Type
                this.resourceTypeMap.set(resource.type, resource.type); 
                //
                // Meta subtype
                this.resourceSubTypeMap.set(resource.subType, resource.subType); 

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

            this.resourceData.sort();
            this.source.load(this.resourceData);
            this.initTableData();
            
      },
      error => console.log('Server Error'),
    );
  }

  updateAppResource(resource : AppResource) {
    //
    // TODO
    this.resourceService.createAppResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  createAppResource(resource : AppResource) {
    //
    // TODO
    this.resourceService.createAppResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  onRowSelect(event) {
    this.ckeditorContent = "";
    var currResource : AppResource = event.data;
    var value: string = this.emailTemplates.get(currResource.key);
    if(value != null){
      console.log('[Smart Tables Service] CURR Resource Choice in Map'.concat(JSON.stringify(currResource)));
      this.ckeditorContent = value;

    }
    console.log('[Smart Tables Service] CURR Resource Choice'.concat(JSON.stringify(currResource)));
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


   expandAppResourceDetails(){
    //
    // TODO
   }


createResource(resource:AppResource){
    this.resourceService.createAppResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  /****************************************************************************************************
   * Uploading section
   */


  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  initTableData(){
    
    //
    // locale dropdown
    this.localeList = new Array<SmartTableListItem>();
    this.resourceLocaleMap.forEach((value: string, key: string) => {
      var localeItem:SmartTableListItem = new SmartTableListItem();
      localeItem.title = key
      localeItem.value = value
      this.localeList.push(localeItem);
      console.log(key, value);
    });
    //
    // platform dropdown
    this.platformTypeList = new Array<SmartTableListItem>();
    this.platformTypeMap.forEach((value: string, key: string) => {
      var platformItem:SmartTableListItem = new SmartTableListItem();
      platformItem.title = key
      platformItem.value = value
      this.platformTypeList.push(platformItem);
      console.log(key, value);
    });
    //
    // type dropdown
    this.resourceTypeList = new Array<SmartTableListItem>();
    this.resourceTypeMap.forEach((value: string, key: string) => {
      var resourceTypeItem:SmartTableListItem = new SmartTableListItem();
      resourceTypeItem.title = key
      resourceTypeItem.value = value
      this.resourceTypeList.push(resourceTypeItem);
      console.log(key, value);
    });
    //
    // subtype dropdown
    this.resourceSubTypeList = new Array<SmartTableListItem>();
    this.resourceSubTypeMap.forEach((value: string, key: string) => {
      var resourceSubTypeItem:SmartTableListItem = new SmartTableListItem();
      resourceSubTypeItem.title = key
      resourceSubTypeItem.value = value
      this.resourceSubTypeList.push(resourceSubTypeItem);
      console.log(key, value);
    });



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

        key: {
          title: 'Resource Key',
          type: 'string',
        },
        value: {
          title: 'Value',
          type: 'string',
        },
        platform: {
          title: 'Platform',
          type: 'string',
          width: '150px',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.platformTypeList,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.platformTypeList,
            },
          },
        },
        locale: {
          title: 'Locale',
          type: 'string',
          width: '120px',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.localeList,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.localeList,
            },
          },
        },
        type: {
          title: 'Meta Type',
          type: 'string',
          width: '150px',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.resourceTypeList,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.resourceTypeList,
            },
          },
        },
        subType: {
          title: 'Meta Subtype',
          type: 'string',
          width: '150px',
          editable: true,
          editor: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.resourceSubTypeList,
            },
          },
          filter: {
            type: 'list',
            config: {
              selectText: 'Select...',
              list: this.resourceSubTypeList,
            },
          },
        },
        description: {
          title: 'Description',
          type: 'string'
        },
      }
    };
  }
}
