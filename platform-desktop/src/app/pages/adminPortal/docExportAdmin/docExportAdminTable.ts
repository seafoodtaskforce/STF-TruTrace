import { Component } from '@angular/core';

import { LocalDataSource } from 'ng2-smart-table';
import { User } from 'app/models/user';
import { Group } from 'app/models/group';
import { Document } from 'app/models/document';
import { DocExportData } from 'app/models/admin/docExportData';


import {SmartTableListItem} from '../../../models/admin/smartTableListItem';

import {DataLoadService} from '../dataLoad.service';
import { DocumentService } from 'app/pages/documents/document.service';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';
import { ToasterService } from 'app/toaster-service.service';
import { AppResource } from 'app/models/AppResource';
import { LocaleUtils } from 'app/utils/locale.utils';

@Component({
  selector: 'doc-export-admin-table',
  templateUrl: './docExportAdminTable.html'
})

export class DocExportAdminTable {

  
    query: string = '';
    settings: any;

    //
    // User Data
    currUser: User =null;
    loggedInName :string;


    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.loggedInName = localStorage.getItem('username');
  
      // fix up the 
      this.initTableData();
      //
      // get all the data for the doucment filter
      this.getAllDocuments();

    }

    ngAfterViewInit() {

      //document.getElementsByClassName('locale')['0'].style.width = '120px';
      //document.getElementsByClassName('type')['0'].style.width = '250px';
      //document.getElementsByClassName('subtype')['0'].style.width = '250px';
      //document.getElementsByClassName('platform')['0'].style.width = '250px';

    }

    constructor(protected resourceService : DataLoadService, protected _documentService: DocumentService
                ,private slimLoader: SlimLoadingBarService, protected toasterService:ToasterService, ) {

    }

    //
    // Data Section
    documents: Array<Document>;
    exportTableDocuments : Array<DocExportData>;

    //
    // Table Data
    source: LocalDataSource = new LocalDataSource();

  
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
  }

  onEditConfirm(event): void {
    if (window.confirm('Are you sure you want to edit?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onRowSelect(event) {
    //alert(`Custom event '${event.action}' fired on row with data: ${event.data} '${JSON.stringify(event)}'`)

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

  /****************************************************************************************************
   * Uploading section
   */


  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  initTableData(){
    this.settings = {
      pager : {
        display : true,
        perPage:10,
      },
      actions: false,
      columns: {
        creationDate: {
          title: 'Date Created',
          type: 'string',
          editable:false,
          addable: false,
        },
        stage: {
          title: 'Organization Type',
          type: 'string',
          editable:false,
          addable: false,
        },
        organization: {
          title: 'Organization Name',
          type: 'string',
          editable:false,
          addable: false,
        },
        docType: {
          title: 'Document Type',
          type: 'string',
          editable:false,
          addable: false,
        },
        owner: {
          title: 'owner',
          type: 'string',
          width: '80px',
          editable:false,
          addable: false,
        },
        recipient: {
          title: 'recipient',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
        },
        docInfoDefinition: {
          title: 'Doc Info',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
          
        },
        docInfoValue: {
          title: 'Doc Info Value',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
        },
        orgGPS: {
          title: 'Org GPS',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
        },
        docGPS: {
          title: 'Doc GPS',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
        },
        id: {
          title: 'id',
          type: 'string',
          width: '150px',
          editable:false,
          addable: false,
        },
      }
    };
  }

  private getCurrentUser() : User {
    return JSON.parse(localStorage.getItem('user'));
  }

  private getCurrentUserName() : string {
    return localStorage.getItem('username');
  }

    /**
   * Get all the documents that this user has access to
   */
  getAllDocuments() {
    this.startProgress();
    var tempDocuments: Array<Document>;
    this._documentService.getAllTraceDocuments().subscribe(
      data => { 
        this.completeProgress();
        tempDocuments = data;
        //
        // Final Doc Data
        this.documents = new Array<Document>();
        this.exportTableDocuments = new Array<DocExportData>();
            // set the organization names for all documents
            for(const doc of tempDocuments){ 
              if(this.getCurrentUser().roles[0].value == 'User'
                  || this.getCurrentUser().roles[0].value == 'General'
                  || this.getCurrentUser().roles[0].value == 'Shipping'
                  || this.getCurrentUser().roles[0].value == 'Receiving'){
                  if((doc.status == 'DRAFT' ||  doc.status == 'REJECTED') && doc.owner != this.getCurrentUserName()){
                    continue;
                  }
                  if(!doc.toRecipients.find(recipient => recipient.id == this.getCurrentUser().id) && doc.owner != this.getCurrentUserName()){
                    continue;
                  }
              }
                               
              // group data
              doc.groupName = this._documentService.getDocumentGroupName(doc);
              doc.orgGPSLocation = this._documentService.getDocumentGroupGPS(doc);
              // trace id for when the user is tracing the data, set to a default value
              doc.currentTraceId = '';
              doc.partOfTrace = true;
              doc.lotFound = true;
              console.log('GROUP NAME ADDED: '.concat(doc.groupName));

              //
              // Convert the doc data
              var convertedDocs :Array<DocExportData> = this.convertDocumentToExportDocument(doc);

              //
              // Push the converted document
              if(convertedDocs.length > 0) {
                this.exportTableDocuments = this.exportTableDocuments.concat(convertedDocs);
                console.log('[Doc Export Module] Filter DOcs so Far '.concat(JSON.stringify(this.exportTableDocuments)));
              }
              
              this.documents.push(doc);
            }    
            this.exportTableDocuments.sort();
            this.source.load(this.exportTableDocuments);   
      },
      error => {
        console.log('Server Error');
        this.completeProgress();
      }
    );

  }
  
  //
  // Conversion routines
  convertDocumentToExportDocument(doc:Document){
    var exportItems = new Array<DocExportData>();

    //
    // for each field data create a new item
    for(const dataRow of doc.dynamicFieldData){
      //
      // Create a new elements and populate it
      var exportItem = new DocExportData();
      //
      // convert
      exportItem.id = doc.id;
      //exportItem.stage = doc.groupTypeName;
      exportItem.organization = doc.groupName;

      exportItem.stage = LocaleUtils.fetchResourceFromResourceMap(doc.groupTypeName , 'en').value;
      console.log('[Doc Export Module] Get Stage Name '.concat(JSON.stringify(LocaleUtils.fetchResourceFromResourceMap(doc.groupTypeName , 'en').value)));
      exportItem.docType = doc.type.value;
      exportItem.creationDate = doc.creationTimestamp;
      exportItem.owner = doc.owner; 
      if (doc.toRecipients.length > 0) {
        exportItem.recipient = doc.toRecipients[0].name;
      }
      exportItem.docInfoDefinition = dataRow.fieldDisplayNameValue;
      exportItem.docInfoValue = dataRow.data;
      //
      // GPS data
      //
      exportItem.docGPS = doc.gpsLocation;
      exportItem.orgGPS = doc.orgGPSLocation;
      //
      // Add it
      exportItems.push(exportItem);
    }
    console.log('[Doc Export Module] GET ALL DOCUMENTS RESTFUL --> NON-ADMIN '.concat(JSON.stringify(exportItems)));
    return exportItems;
  }

  /************************************************************************************
   * Progress Bar
   */

   /**
    * Start the progress bar
    */
   startProgress() {
    // Progress bar
    
    this.slimLoader.start(() => {
      this.slimLoader.height = '8px';
      this.slimLoader.color = 'green';
        console.log('Loading complete');
    });
  }

  /**
   * Complete the progress
   */
  completeProgress() {
    this.slimLoader.complete();
  }

  /**
   * Data Export
   */
   exportDataToCSV() {
    this.source.getFilteredAndSorted().then(res =>{
      console.log("<Data Export> to CSV: " + res); 
      console.log("<Data Export> to CSV [count]: " + res.length); 

      
    });
   }

   ConvertToCSV(objArray) {
    var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
    var str = '';
    var row = "";
    var globalColumnCount = 0;
    var columnCount = 0;

    for (var index in objArray[0]) {
        //Now convert each value to string and comma-separated
        if(index != 'constructor') {
          row += index + ',';
          globalColumnCount +=1;
        }        
    }
    row = row.slice(0, -1);
    //append Label row with line break
    str += row + '\r\n';
    str = DocExportData.getCSVRowHeader() + '\r\n';

    for (var i = 0; i < array.length; i++) {
        var line = '';
        columnCount = 0;
        for (var index in array[i]) {
            if(columnCount >= globalColumnCount) {
              continue;
            } else {
              if (line != '') line += ','
              if(index === "orgGPS" || index === "docGPS" || index === "docInfoValue") {
                line += '"' + array[i][index] + '"'; 
              } else {
                line += array[i][index]; 
              }
            }
            columnCount +=1;
        }
        str += line + '\r\n';
    }
    return str;
}

   downloadFileToCSV() {
    var csvData;
    this.source.getFilteredAndSorted().then(res =>{
      console.log("<Data Export> to CSV: " + res); 
      console.log("<Data Export> to CSV [count]: " + res.length); 
      //console.log("<Data Export> to CSV [string]: " + JSON.stringify(JSON.parse(res))); 
      var exportItems = new Array<DocExportData>();
      exportItems = res;

      
      csvData = this.ConvertToCSV(exportItems);
      var a = document.createElement("a");
      a.setAttribute('style', 'display:none;');
      document.body.appendChild(a);
      var blob = new Blob([csvData], { type: 'text/csv' });
      var url= window.URL.createObjectURL(blob);
      a.href = url;
      a.download = 'User_Results.csv';/* your file name*/
      a.click();
    });

    return 'success';
   }
}
