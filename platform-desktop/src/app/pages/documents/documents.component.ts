import { Component, OnInit } from '@angular/core';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';



import { DocumentService } from './document.service';
import { Document } from '../../models/document';
import { DocumentType } from '../../models/documentType';
import { User } from '../../models/user';
import { Group } from '../../models/group';
import { GroupType } from '../../models/groupType';
import { GroupList } from '../../models/groupList';
import { DocumentTag } from '../../models/documentTag';
import { ScrollToAnimationEasing, ScrollToEvent, ScrollToOffsetMap, ScrollToConfigOptions, ScrollToService } from '@nicky-lenaers/ngx-scroll-to';
import { NoteData } from '../../models/noteData';
import { NgUploaderOptions } from 'ngx-uploader';

// import global data
import * as AppGlobals from '../../config/globals';
import { LocaleUtils } from '../../utils/LocaleUtils';

// Smart Tables
import { LocalDataSource } from 'ng2-smart-table';
import { TagAdmin } from '../../models/admin/tagAdmin';
import {SmartTableListItem} from '../../models/admin/smartTableListItem';
import {DataLoadService} from '../adminPortal/dataLoad.service';

// Toaster
import { ToasterService } from '../../toaster-service.service';
import { DocumentsModule } from './documents.module';

@Component({
  selector: 'documents',
  templateUrl: './documents.html',
  styleUrls: ['./documents.scss'],
})
export class DocumentsComponent implements OnInit {
  // multi-select
  dropdownList = [];
  selectedItems = [];
  dropdownSettings = {};


  public ngxScrollToDestination: string;
  public ngxScrollToEvent: ScrollToEvent;
  public ngxScrollToDuration: number;
  public ngxScrollToEasing: ScrollToAnimationEasing;
  public ngxScrollToOffset: number;
  public ngxScrollToOffsetMap: ScrollToOffsetMap;


  username: string;
  currentUser : User;

  /**
   * Creation of new document data
   */
  currectDocumentTypeForNewDoc: DocumentType = null;
  currectDocumentTypeForNewDocListValue: string;
  currentDocumentRecipients: User[] = new Array<User>();
  currentDocumentLinks: Document[] = new Array<Document>();
  currentDocumentBacking: Document[] = new Array<Document>();
  currentDocumenTags: DocumentTag[] = new Array<DocumentTag>();

  /**
   * Document linking 
   */
  
  // linking tags to linked docs
  linkingDocsByTags: Document[] = new Array<Document>();
  // using a search tag to get a list of matching docs
  linkedDocListBySearchTag: Document[] = new Array<Document>();
  searchDocsByTagInputText: string = null;

  linkingDocs: Document[] = new Array<Document>();
  backingDocs: Document[] = new Array<Document>();
  linkingDocsExtraPermissions: Document[] = new Array<Document>();
  

  fileUploaderOptions: NgUploaderOptions = this.importPDFDocData();
  selectedPDFFileToUpload: string = null;

  /**
   * Trace Data
   */
  allGroupTraceRequiredDocs: DocumentType[]  = new Array<DocumentType>();
  allGroupTraceRequiredDocNames: string[] = new Array<string>();

  /**
   * Smart Table
   */

  //
  // Tag Admin
  allTags: DocumentTag[] = new Array<DocumentTag>();
  allAdminTags: TagAdmin[] = new Array<TagAdmin>();
  currTag: DocumentTag =null;

  query: string = '';
  tagCustomPrefixlist:SmartTableListItem[] = [
    { value: 'Other', title: 'Other' }, 
    { value: 'Invoice #', title: 'Invoice #' }, 
    { value: 'MD', title: 'MD' }, 
    { value: 'FMD',title: 'FMD'},
    { value: 'FIF',title: 'FIF'},
  ];

  settings = {
    actions: { delete: false, edit:false } ,
    add: {
      addButtonContent: '<i class="btn btn-sm btn-primary">Add</i>',
      confirmCreate: true
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

  constructor(private _documentService: DocumentService, private _scrollToService: ScrollToService
            , private slimLoader: SlimLoadingBarService, protected tagService : DataLoadService,
            protected toasterService:ToasterService) {
      this.ngxScrollToEvent = 'mouseenter';
      this.ngxScrollToDuration = 1500;
      this.ngxScrollToEasing = 'easeOutElastic';
      this.filterDocsFlagButton = 'doc_feed_[FilterAllDocs]';

      this.username = localStorage.getItem('username');
      this.currentUser = JSON.parse(localStorage.getItem('user'));

      this.fileUploaderOptions = this.importPDFDocData();


  }


  readonly SORT_DATE_ASCENDING= 'document_filter_sort_newest';
  readonly SORT_DATE_DESCENDING= 'document_filter_sort_oldest';
  readonly FILTER_USER_ALL= '-- All Users';
  readonly FILTER_DOCTYPE_ALL= '-- All Doc Types';

  showDocuemntDetailsflag = false;
  currentDocument: Document;
  documents: Array<Document>;
  tempDocuments: Array<Document>;
  documentTypes: Array<DocumentType>;
  unfilteredDocuments: Array<Document>;
  users: Array<User>;
  importTagsList: Array<DocumentTag>;
  temp = Array;
  math = Math;
  docTypeColor: string = ' #ffffff';
  sortDateAscending= true;
  sortDateFieldName= 'creationTimestamp';
  showFilter = true;
  showTracebilityCard = false;
  tabElements: string[] = new Array<string>();
  traceQueueddDocuments: Array<Document>;
  filterDocsFlag:boolean = false;
  filterDocsFlagButton:string; 

  currentRejectionNote: string = "Hello THere";

  /**
    Traceability
  */

  //
  // Represents all the main stages (i.e. header stage names such as 'Processor')
  stages: GroupType[];

  //
  // Represents all of the groups for the logged in user
  allGroups: Group;
  //
  // Represents all the currently traced group names. IN case the trace is not on this will
  // hold the names of all group names - it is derived from allGroups variable.
  allGroupNames: string[] = new Array<string>();

  //
  // Represents the mapping of Stage names to array actual groups contained in those in those. 
  //    - Key: stage name (such as) "Farm"
  // NOTE: the collection of all the values wiykd give us bascially the allGroups collection.
  //    - Value: an array of actual groups under that stage name (for example) ["Farm 1", Farm 2"...]
  traceabilityStageGridMap: Map<string, Group[]> = new Map<string, Group[]>();

  //
  // Represents all the documents that are traced from (and including) the traced document
  tracedCurrDocument: Document[] = new Array<Document>();

  //
  // Mapping of the tracing
  traceabilityMap: Map<string, Document[]> = new Map<string, Document[]>();

  traceClickSelection: string = '';
  traceClickSelectionType: string = '';
  closeButtonOn: boolean;
  traceDocTriggerId: number= 0;
  traceDocTriggerStartId: number= 0;


  //
  // THe current trace item that was clicked on in the grid. This is the item being highlighted.
  currentTraceItem: string;
  currentTraceItemType: string;

  //
  // Selections for Filter
  filterSelectedUserChoice = this.FILTER_USER_ALL;
  filterSelectedSortChoice = this.getInternationalizedToken(this.SORT_DATE_ASCENDING);
  filterSelectedDocTypeChoice = this.FILTER_DOCTYPE_ALL;
  filterSelectedGroupName = '';

  //
  // selections for tags searches
  tagInputSearch001: string = '';
  tagInputSearch002: string = '';
  tagInputSearch003: string = '';
  tagInputSearch004: string = '';
  tagInputSearch005: string = '';
  tagInputSearch006: string = '';
  tagInputSearch007: string = '';
  tagInputSearch008: string = '';
  tagInputSearch009: string = '';
  tagInputSearch010: string = '';
  isPartialMatch: boolean = true;
  lotSearchOn: boolean = false;
  docImportOn: boolean = false;
  numberOfSearchLotFields: number = 4;

  //
  // Represents all the tag input strings
  inputTagSearchStrings: string[] = new Array<string>();

  //
  // Filtering for documents
  documentFilter: any = {
    currentTraceId: '',
    partOfTrace: true,
    documentType: '',   // filter by type
    owner: '',          // filter by owner
    groupName: {
      $or: this.allGroupNames,
    },      // filter by group
    lotFound:true,
  };

  //
  // Filtering for a trace
  documentTraceFilter: any = {
    groupName: '',    // filter by group name
  };

  // The URL for the server REST communication
  serverURI: string;

  /**
   * Initialization of the component
   */
  ngOnInit() {
    
    this.getAllDocuments();

    

    // multi-select
    this.dropdownList = [
      {"id":1,"itemName":"India"},
      {"id":2,"itemName":"Singapore"},
      {"id":3,"itemName":"Australia"},
      {"id":4,"itemName":"Canada"},
      {"id":5,"itemName":"South Korea"},
      {"id":6,"itemName":"Germany"},
      {"id":7,"itemName":"France"},
      {"id":8,"itemName":"Russia"},
      {"id":9,"itemName":"Italy"},
      {"id":10,"itemName":"Sweden"}
    ];
    this.selectedItems = [
            {"id":2,"itemName":"Singapore"},
            {"id":3,"itemName":"Australia"},
            {"id":4,"itemName":"Canada"},
            {"id":5,"itemName":"South Korea"}
        ];
    this.dropdownSettings = { 
            singleSelection: false, 
            text:"Select Countries",
            selectAllText:'Select All',
            unSelectAllText:'UnSelect All',
            enableSearchFilter: true,
            classes:"myclass custom-class"
          };            
    }

    ngAfterViewInit() {
      //document.getElementsByClassName('tagPrefix')['0'].style.width = '175px'
      this.getAllDocumentTypes();
      this.getServerURI();
      this.getAllUsers();
      this.getAllTags();
      this.stages = this.getAllStages();
      //this.getDocumentTraceById(42);
      this.getGroupsByOrganizationId(JSON.parse(localStorage.getItem('user')).userGroups[0].organizationId);
    }

    source: LocalDataSource = new LocalDataSource();

    // user data
    allUsers: Array<User>;

    onItemSelect(item:any){
      console.log(item);
      console.log(this.selectedItems);
    }
    OnItemDeSelect(item:any){
      console.log(item);
      console.log(this.selectedItems);
    }
    onSelectAll(items: any){
      console.log(items);
    }
    onDeSelectAll(items: any){
      console.log(items);
    }
    

  /**
   * Get all the documents that this user has access to
   */
  getAllDocuments() {
    this.startProgress();
    this._documentService.getAllDocuments().subscribe(
      data => { 
        this.completeProgress();
        this.tempDocuments = data;
        this.documents = new Array<Document>();
        console.log('[Document Component] GET ALL DOCUMENTS RESTFUL '.concat(JSON.stringify(this.documents)));
        console.log('[Document Component] GET ALL DOCUMENTS RESTFUL --> USername '.concat(JSON.stringify(this.username)));
            // set the organization names for all documents
            for(const doc of this.tempDocuments){ 
              if(this.currentUser.roles[0].value == 'User'
                  || this.currentUser.roles[0].value == 'Shipping'
                  || this.currentUser.roles[0].value == 'Receiving'){
                    console.log('[Document Component] GET ALL DOCUMENTS RESTFUL --> NON-ADMIN '.concat(JSON.stringify(this.username)));
                  if(doc.status == 'DRAFT' && doc.owner != this.username){
                    continue;
                  }
                  if(!doc.toRecipients.find(recipient => recipient.id == this.currentUser.id) && doc.owner != this.username){
                    continue;
                  }
              }
              
                //
                // otherwise continue here
                                
                // group names
                doc.groupName = this._documentService.getDocumentGroupName(doc);
                // trace id for when the user is tracing the data, set to a default value
                doc.currentTraceId = '';
                doc.partOfTrace = true;
                doc.lotFound = true;
                console.log('GROUP NAME ADDED: '.concat(doc.groupName));

                // set the tracebility filter to off
                this.setTraceDocuments(false);
                // add the doc
                this.documents.push(doc);
            }
            
      },
      error => {
        console.log('Server Error');
        this.completeProgress();
      }
    );

  }

  /**
   * Get all the yahs for the filter listing. These are all the tags in the groups that has access to the
   * docuemnt data.
   */
  getAllTags() {
    this._documentService.getAllTags().subscribe(
      data => { 
        this.importTagsList = data,
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

  /**
   * Get a list of recipients for the current doc
   */
  getRecipientsListCurrentDoc(){
    var recList = '';
    for (const recipient of this.currentDocument.toRecipients) {
      recList = recList.concat(', ' + recipient.name)
    }
    if(recList.length > 0){
      recList = recList.substr(2);
    }
    return recList
  }

  /**
   * Get all the tags.
   */
  getAllUsers() {
    this._documentService.getAllUsers().subscribe(
      data => { 
        this.users = data;
        this.currentDocumentRecipients = this.users;
      },
      error => console.log('Server Error'),
    );
  }

  /**
   * Get all linkable documents by searching for the input tag
   * @param tag - the tag to search by
   */
  getAllLinkableDocsByTag(tag:string) {
    console.log("SEARCH TAG VALUE = " + tag);
    this._documentService.startTagLinkDocSearching();
    this._documentService.getAllLinkableDocsByTag(tag).subscribe(
      data => { 
        this.linkedDocListBySearchTag = data;
        console.log('[Document Service] GET ALL DOCS By TAG RESTFUL '.concat(JSON.stringify(data)));
        this._documentService.stopTagLinkDocSearching();
      },
      error => console.log('Server Error'),
    );
  }

    /**
   * Get all the users for the filter listing. These are all the users in the groups that has access to the
   * docuemnt data.
   */
  getAllDocumentTypes() {
    this._documentService.getAllDocumentTypes().subscribe(
      data => this.documentTypes = data,
      error => console.log('Server Error'),
    );
  }

  /**
   * Get all the documents by the trace id
   * @param id - the id of the document to trace
   */
  getDocumentTraceById(id: number) {
    this._documentService.getDocumentTraceById(id).subscribe(
      data => { 
        this.tracedCurrDocument = data;
            console.log('Current Traced Document '.concat(JSON.stringify(this.tracedCurrDocument)));
            // set the organization names for all documents
            this.buildTraceabilityMap();
            // set the trace filter on 
            this.setTraceDocuments(true);
            // sort the feeddocs by additional id

      },
      error => console.log('Server Error'),
    );
    
  }

  /**
   * Get all the groups by organization id
   * @param id  - the oerganization id
   */
  getGroupsByOrganizationId(id: number) {
    this._documentService.getGroupsByOrganizationId(id).subscribe(
      data => { 
        this.allGroups = data;
            console.log('All Groups '.concat(JSON.stringify(this.allGroups)));
            // set the organization names for all documents
            this.buildTraceabilityGridMap();
      },
      error => console.log('Server Error'),
    );
    
  }

  /**
   * This map holds Map<string, Document[]>();
   * Where the key is the stage name (such as Farm) and the values
   * are all the documents that belong under that stage
   */
  buildTraceabilityMap() {
    // clear the current map
    this.traceabilityMap.clear();

    console.log('Map [Size] ' + (this.traceabilityMap.size));

    if(this.tracedCurrDocument !== null){
      for (const doc of this.tracedCurrDocument) {
        console.log('Map [Size] ' + (this.traceabilityMap.size));
        console.log('Trecability Map So far -- '.concat(JSON.stringify(this.traceabilityMap)));
        console.log('Creating Traceability Map ---> ' + (doc.id));
  
        for (const linkedDoc of doc.linkedDocuments) {
          // place the linked doc into the map based on groupTypeName  
          let stageDocs: Document[];
          if (this.traceabilityMap.has(linkedDoc.groupTypeName)) {
            stageDocs = this.traceabilityMap.get(linkedDoc.groupTypeName);
          }else {
            stageDocs = new Array<Document>();
          }  
          stageDocs.push(linkedDoc);
          console.log('   Adding ---> ' + (linkedDoc.groupName));
          console.log('Linked Docs Array '.concat(JSON.stringify(stageDocs)));
          this.traceabilityMap.set(linkedDoc.groupTypeName, stageDocs);
  
        }
      }
    }
    
    console.log('Map Final [Size] ' + (this.traceabilityMap.size));
    console.log('Traced Stages '.concat(JSON.stringify(this.traceabilityMap)));
    console.log('Traced Stage Keys ' + this.traceabilityMap.keys);

    this.printTraceabilityMap();
  }

  buildTraceabilityGridMap() {
    // clear the current map
    this.traceabilityStageGridMap.clear();
    // get the groups names
    this.allGroupNames = new Array<string>();

    console.log('Grid Map [Size] ' + (this.traceabilityMap.size));
    console.log('Trecability Group Data -- '.concat(JSON.stringify(this.allGroups)));

    for (const group of this.allGroups.subGroups) {
      console.log('Grid Map [Size] ' + (this.traceabilityStageGridMap.size));
      console.log('Trecability GRID So far -- '.concat(JSON.stringify(this.traceabilityMap)));

        // place the linked doc into the map based on groupTypeName  
        let columnGroupNames: Group[];
        if (this.traceabilityStageGridMap.has(group.groupType.name)) {
          columnGroupNames = this.traceabilityStageGridMap.get(group.groupType.name);
        }else{
          columnGroupNames = new Array<Group>();
        }  
        columnGroupNames.push(group);

        // create the matching group array
        this.allGroupNames.push(group.name);
        console.log('   Adding Column Group ---> ' + (group.name) + ' ' + JSON.stringify(this.traceabilityStageGridMap));
        this.traceabilityStageGridMap.set(group.groupType.name, columnGroupNames);
    }
    console.log('Grid Final [Size] ' + (this.traceabilityStageGridMap.size));
    console.log('Tracibility Grid Final Value: '.concat(JSON.stringify(this.traceabilityStageGridMap)));
    console.log('Current Groups: '.concat(JSON.stringify(this.allGroupNames)));

    //
    // re-init the filter

    this.documentFilter.groupName.$or = this.allGroupNames;
    
  }

  moveToFeedDocument(id: number){
    console.log('Click on the Doc: ' + id)
  }

  /**
   * Show the details of the document as well as trigger the 
   * @param id  - the document id
   */
  showDetails(id: number, scrollFlag:boolean) {
    this.showDocuemntDetailsflag = true;
    for (const doc of this.documents) {
      if (doc.id === id) {
        this.currentDocument = doc;
        const config: ScrollToConfigOptions = {
          target: ""+id
        };
    
        if(scrollFlag){
          this._scrollToService.scrollTo(config);
        }
        //
        // check if the doc has been read
        if (!this.currentDocument.currentUserRead) {
            this.currentDocument.currentUserRead = true;
            // trigger the server
            this._documentService.markDocAsRead(doc).subscribe(
              data =>  console.log('No issues'),
              error => console.log('Server Error'),
          );
        }
      }
    }

    // check if the traceability is up, if yes then match the highlight in the grid
    if (this.showTracebilityCard) {
      // TODO fix the isse with showing the color
      // this.filterByTraceGroup(this.currentDocument.groupName);
    }

    // check the status of the document
    if(this.currentDocument.status == 'SUBMITTED' && !this.isDocumentOwner()){
      this.currentDocument.status = 'PENDING';
      this._documentService.setDocumentStatus(this.currentDocument).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
      );
    }
    /**
    if (this.traceDocTriggerId === id) {
      // do nothing
    } else {
      this.showFilter = true;
      this.showTracebilityCard = false;
    }
    */
  }

  getDocumentByDocId(id: number) {
    for (const doc of this.documents) {
      if (doc.id === id) {
        return doc;
      }
    }
    return null;
  }

  /**
   * Get the URI to the sever (used for REST communication)
   */
  getServerURI() {
    this.serverURI = this._documentService.getServerURI();
    
  }

  /**
   * Filter feed document data by user
   * @param item  - the user to filter by
   */
  filterByUser(item: string) {
    this.filterSelectedUserChoice = item;
    if (item === this.FILTER_USER_ALL) {
      this.documentFilter.owner = '';
    }else {
      this.documentFilter.owner = item;
    }
    this.showDocuemntDetailsflag = false;
    // log the data
    console.log ( 'Sort By User ' + this.filterSelectedUserChoice );

  }

  /**
   * 
   * @param item - the document type to use when creating a new doc
   */
  setDocImportType(item){
    var docType : any;
    var reversedDocTypeName:string;

    console.log('PDF-IMPORT - TYPE ' + item);
    //fetch the reverse value of the item; parsed out away from UI render and into the doc type
    // reversedDocTypeName = this._documentService.reverseInternationalizedNameStringToKeyString(item);
    reversedDocTypeName = LocaleUtils.fetchResourceKeyByValue(item);
    console.log('PDF IMPORT - TYPE<reversed>' + reversedDocTypeName);


    for(var row=0 ; row < this.currentUser.userGroups[0].allowedDocTypes.length; row++) {
      docType = this.currentUser.userGroups[0].allowedDocTypes[row];
      console.log('PDF IMPORT ---- TYPE<looking>' + JSON.stringify(docType.name));
      
      if( docType.name === reversedDocTypeName){
        this.currectDocumentTypeForNewDoc = docType;
        //this.currectDocumentTypeForNewDocListValue = docType.value;
        console.log('PDF IMPORT ---- TYPE<found>' + JSON.stringify(docType));
      }
    }
    
  }

  setTagSelection(tags : DocumentTag[]){
    this.linkDocsByTag(tags);
    
  }

  /**
   * Filter the document feed by date
   * @param item  - the date to filter by
   */
  filterByDateSort(item: string) {
    this.filterSelectedSortChoice = item;
    if ( item === this.getInternationalizedToken(this.SORT_DATE_ASCENDING )) {
      this.sortDateAscending = true;
    } else {
      this.sortDateAscending = false;
    }
    console.log('Sort By Date ' + this.filterSelectedSortChoice);
    this.showDocuemntDetailsflag = false;
  }

  /**
   * Filter the document feed by document type
   * @param item  - the document type
   */
  filterByDocType(item: string) {
    this.filterSelectedDocTypeChoice = item;
    item = this._documentService.reverseInternationalizeString(item);
    if (item === this.FILTER_DOCTYPE_ALL) {
      this.documentFilter.documentType = '';
    }else {
      this.documentFilter.documentType = item;
    }
    this.showDocuemntDetailsflag = false;
    // log it
    console.log('Sort By DocType ' + this.filterSelectedDocTypeChoice);
  }

  /**
   * Filter the document feed by the trace group selector
   * @param item  - the item to filter by which in this case would be the group name
   */
  filterByTraceGroup(item: string) {
    console.log('Filter By Group ' + item);

    this.filterSelectedGroupName = item;
    this.currentTraceItem = item;
    this.documentFilter.groupName = {
      $or: this.getGroupFilterArray(item),
    }, 
    this.traceClickSelection = item;

  }

    /**
   * Filter the document feed by the trace group selector
   * @param item  - the item to filter by which in this case would be the group name
   */
  filterByTraceOrganization(item: string) {
    console.log('Filter By Group ' + item);

    this.showDocTypesForOrganization(item);
    // Filter
    //
    this.filterSelectedGroupName = item;
    this.currentTraceItem = item;
    this.documentFilter.groupName = {
      $or: this.getOrganizationFilterArray(item),
    }, 
    this.traceClickSelection = item;

  }

    /**
   * Filter the document feed by the trace group selector
   * @param item  - the item to filter by which in this case would be the group name
   */
  showDocTypesForOrganization(item: string) {
    //console.log('-- DBL CLICK: DOC TYPES Show Document Types for  ' + item);
    //console.log('-- DBL CLICK: Group Data  ' + JSON.stringify(this.getOrganizationFilterArray(item)));
    //console.log('-- DBL CLICK: Group Doc Names  ' + JSON.stringify(this.getOrganizationRequiredDocTypesFilterArray(item)));

    this.clearDummyDocs();
    this.addDummyDocsForRequiredDocs(item);

  }


  

    /**
   * Filter the document feed by the trace group selector
   * @param item  - the item to filter by which in this case would be the group name
   */
  filterByTraceGroupType(item: string) {
    console.log('Filter By Group Type ' + item);
    // clear any dummy data
    this.clearDummyDocs();

    this.filterSelectedGroupName = item;
    this.currentTraceItem = item;
    this.documentFilter.groupName = {
      $or: this.getGroupTypeFilterArray(item),
    }, 
    this.traceClickSelectionType = item;
  }

  filterByTraceAllDocs() {


    this.filterSelectedGroupName = '';
    this.currentTraceItem = '';
    this.documentFilter.groupName = {
      $or: this.getAllDocsGroupTypeFilterArray(),
    }, 
    this.traceClickSelectionType = '';
  }

  

  showDocIndicator(item: string) {
    if (this.currentDocument.groupName === item) {
      return true;
    } else {
      return false;
    }
    
  }

  /**
   * Reset the trace group filter
   */
  resetTraceGroupFilter() {
    this.documentFilter.groupName = '';
  }

  /**
   * Reset the main filter
   */
  resetFilter() {
    this.filterSelectedUserChoice = '-- All Users';
    this.filterSelectedSortChoice = this.getInternationalizedToken(this.SORT_DATE_ASCENDING);
    this.filterByDateSort(this.filterSelectedSortChoice);
    this.filterSelectedDocTypeChoice = '-- All Doc Types';
    this.documentFilter.documentType = '';
    this.documentFilter.owner = '';
    this.documentFilter.partOfTrace = true;
    this.documentFilter.lotFound = true;
    this.documentFilter.groupName  = {
      $or: this.getGroupFilterArray(''),
      };
    this.filterSelectedGroupName = '';
    // reset any doc side effects
    this.resetDocsforFilter();
    this.lotSearchOn = !this.lotSearchOn;
    this.docImportOn = !this.docImportOn;
    // reset the import page data
    this.currentDocumentRecipients = new Array<User>();
    this.currentDocumentLinks = new Array<Document>();
    this.currentDocumentBacking = new Array<Document>();
    this.currentDocumenTags = new Array<DocumentTag>();
    this.currectDocumentTypeForNewDocListValue = "-- Choose Doc Type";
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
    if (tabName === 'notes') {
      return 'nav-link';
    }
   }

  filterDocumentFeed() {

  }

  getDocumentTypeIcon(type: string) {
    return 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABcklEQVRoQ+2Z/W3CMBDFn5sBYIMyQoDs0Y4AG7AJbEBHgD0SyAjtBC0DREYWUJWKYPt8sRPp8jcX3++9+0CxwsAfNfD8IQCpHXRzID9OkDVraORQmAQlrfGBQ7EMesefYDuASf6lOUJhzHUoGCHsAPNyB+CNLfnbi5gg7ACz8ptFfa2WUHp7JwQDhB1gXmoW9atCYVYtuCHiAhglmCHiAzBDpAFghEgHwASRFoABIj1AIEQ/AAIg+gPQBmH2x5OnXwCPIHoDQF3nAsD1X0gceKxAvCYWB7gdsEwHb8Hbeq2zKSQA/zwSB9qKVvaAdzvfB0gTE5vLW3fiOfRNLGM09Rj1rhFiQGdNTMzHO6wzANceCN0jAkAcb7+lIg6Ef1b5ATDybj6OAI0vHIqnd3Iui6ybKyY3wD2q4j3sw9blhrJO4MIJTZajnn6GAZjoC8Tmes366iYe8VembBRqNNnKlrw5wV5CxDxihQlALKXbzhm8A2fOK/Axnp6lCwAAAABJRU5ErkJggg==';
    // return this._documentService.getDocumentTypeIcon(type);
  }
  
  getDocumentGroupName(doc: Document) {
    return this._documentService.getDocumentGroupName(doc);
  }

  showLinkedDocDetails(doc: Document) {
    return this.showDetails(doc.id, true);
  }

  hasLinkedDocuments(doc: Document) {
    return doc.linkedDocuments.length > 0;
  }

  isCustomTag(doc: Document) {

    for(var row=0 ; row < doc.tags.length; row++) {
        if (doc.tags[row].text.startsWith(this.getCustomTagPrefix())) {
          return true;
        }
      }
    return false;
  }

  isCustomTagString(tag: DocumentTag) {

    if (tag.text.startsWith(this.getCustomTagPrefix())) {
          return true;
    }
    return false;
  }

  isReadDocument(doc: Document) {

    if (doc.currentUserRead) {
      return true;
    }
    return false;
  }

  getCustomTag(doc: Document) {

    for(var row=0 ; row < doc.tags.length; row++) {
      if( doc.tags[row].text.startsWith(this.getCustomTagPrefix())) {
      return doc.tags[row].text.substr(8);
      }
    }
    return '';

  }

  traceDocument(id: number, event: any) {
    // this.toggleTracing();

    this.showFilter = false;
    this.showTracebilityCard = true;
    this.traceDocTriggerId = id;
    if(this.traceDocTriggerId == 0){
      this.traceDocTriggerStartId = id;
    }
    this.resetFilter();

    // event.stopPropagation();
    if (this.showTracebilityCard) {
      this.getDocumentTraceById(id);
      console.log('All Stages '.concat(JSON.stringify(this.getAllStages())));
      console.log(JSON.stringify(this.tracedCurrDocument));
      // get the grid data
      this.getMaxTraceGridRows();
      for(var row=0 ; row < this.getMaxTraceGridRows(); row++){
        console.log("ROW ---> " + "[" + row + "] " + JSON.stringify(this.getGridRowNamesforRow(row)));
      }


      console.log('Setting Trace Filter on');
    }

    
  }

  traceOriginalDocument(event: any) {
    this.clearDummyDocs();
    this.filterByTraceAllDocs();
  }

  stripTag(tag : DocumentTag){

    var replaced = tag.text.replace("CUSTOM: ", "");
    console.log("STRIPPED TAG: " + tag.text);
    return replaced;
  }

  toggleTracing() {
    this.showFilter = !this.showFilter;
    this.showTracebilityCard = !this.showTracebilityCard;
  }

  /**
   * Dismiss the Trace Panel and reset filters.
   */
  dismissTraceDocument(event: any) {
    this.traceDocTriggerId = 0;
    this.showFilter = true;
    this.showTracebilityCard = false;
    this.resetFilter();
    // reset the traceability filter elements
    this.setTraceDocuments(false) ;
    this.clearDummyDocs();
  }

  filterDocs(event: any){
    let user: User = JSON.parse(localStorage.getItem('user'));
    if(this.filterDocsFlag === false){
      // Show my Doc only
      this.filterDocsFlagButton = 'doc_feed_[FilterMyDocs]';
      this.filterDocsFlag = true;
      this.documentFilter.owner = user.name;
    }else{
      //show all docs
      this.filterDocsFlagButton = 'doc_feed_[FilterAllDocs]';
      this.filterDocsFlag = false;
      this.documentFilter.owner = ''
    }
  }


  exportTraceDocument(id: number, event: any){
    console.log('APDF Export for Doc ---> ' +id);
    this._documentService.downloadFile(id)
  }


  getAllStages() {
    let stages = new Array<GroupType>();
    let user: User = JSON.parse(localStorage.getItem('user'));

    for (let group of user.userOrganizations[0].subGroups) {
      if ( !this.doesGroupTypeExist(stages, group.groupType) ) {
        stages.push(group.groupType);
      }
    }

    stages.sort( function(groupType1, groupType2) {
	    if ( groupType1.orderIndex < groupType2.orderIndex ) {
	    	return -1;
        } else if ( groupType1.orderIndex > groupType2.orderIndex ) {
            return 1;
        } else {
          return 0;	
        }
	  });

    console.log('All Stages ---> ' + JSON.stringify(stages));
    return stages;
  }

  doesGroupTypeExist(array: GroupType[], entry: GroupType) {
    for (let groupType of array) {
      if ( groupType.id === entry.id) {
        return true;
      }
    }

    return false;
  }

  splitGridIconName(str: string) {
    return str.split(' ', 2); 
  }

  getHeaderTraceabilityClassColor(headerName: string) {
    console.log('Header Data-Name ' + JSON.stringify(headerName));
    let lookupIndex: number = this.getHeaderIndex(headerName);
    return this._documentService.getHeaderTraceabilityClassColor(lookupIndex).concat(' clickable');
  }

  getCellTraceabilityClassColorByGroup(gridCell: Group) {
    console.log('Header Data ' + JSON.stringify(gridCell));


      if (gridCell.name === this.currentTraceItem){
        return this.isOrganizationTraceFullyCompletedColor(gridCell).concat(' clickable icon-active');
      }else{
        return this.isOrganizationTraceFullyCompletedColor(gridCell).concat(' clickable');
      }

  }

  

  getHeaderTraceabilityClassColorByGroup(gridCell: Group) {
    console.log('Header Data ' + JSON.stringify(gridCell));

    if(!this.checkIfTracedDocumentIsInGroup(gridCell)) {
      return 'shape-placeholder';
    }else{
      let lookupIndex: number = this.getHeaderIndex(gridCell.groupType.name);

      if (gridCell.name === this.currentTraceItem){
        return this._documentService.getHeaderTraceabilityClassColor(lookupIndex).concat(' clickable icon-active');
      }else{
        return this._documentService.getHeaderTraceabilityClassColor(lookupIndex).concat(' clickable');
      }
      
    }
  }

  getHeaderTraceabilityClassColorByGroupStatus(gridCell: Group) {
    console.log('Header Data ' + JSON.stringify(gridCell));
    var isComplete:boolean = true//this.checkIfTracedDocumentHasAllDocuments(gridCell);

      if (gridCell.name === this.currentTraceItem){
        return this._documentService.getCellTraceabilityClassColor(isComplete).concat(' clickable icon-active');
      }else{
        return this._documentService.getCellTraceabilityClassColor(isComplete).concat(' clickable');
      }
  }

  getHeaderIndex(headerName: string) {
    
    let i: number = 0;
    for (let group of this.stages) {
      console.log('Header Data Group ' + JSON.stringify(group) + ' ' + headerName);
        if ( group.name === headerName) {
          return i;
        } else {
          i++;
        }
    }
  }

  getMaxTraceGridRows() {
    let maxRows: number =0;
    this.traceabilityStageGridMap.forEach((value: Group[], key: string) => {
      if ( this.traceabilityStageGridMap.get(key).length > maxRows) {
        maxRows = this.traceabilityStageGridMap.get(key).length;
      }
      console.log('The MAX GRID ROWS ---> ' + maxRows);
    });

    return maxRows;
  }

  getGridRowNamesforRow(row:number){
    let rowNames: Group[] = new Array<Group>();
    for(let groupType of this.stages){
      if(this.traceabilityStageGridMap.get(groupType.name).length-1 < row){
        rowNames.push(null);
      }else{
        rowNames.push(this.traceabilityStageGridMap.get(groupType.name)[row]);
      }
    }
    // console.log('FLattened SINGLE ROW ***** ' + JSON.stringify(rowNames));
    return rowNames;
  }

  getAllGridRowNames() {
    let allRows: GroupList[] = new Array<GroupList>();
    let maxRows: number = this.getMaxTraceGridRows();
    for(var row=0 ; row < maxRows; row++){
      let groups: Group[] = this.getGridRowNamesforRow(row);
      let groupList: GroupList = new GroupList();
      groupList.subGroups = groups;
      allRows.push(groupList);
    }

    // console.log('FLattened Rows ***** ' + JSON.stringify(allRows));
    return allRows;
  }

  isNewRow(column:number){
    console.log('Column # ***** ' + column);
    if(column % this.stages.length === 0) {
      console.log('Column # ***** <SWITCH>' + column);
      return true;
      // return 'diagram-table-row';
    }else{
      return false;
      // return '';
    }

  }

  /**
   * Check if the current traced document is in the input group
   * @param group - the group to check against
   */
  checkIfTracedDocumentIsInGroup(group: Group) {
    const result: boolean = false;

    for (const doc of this.tracedCurrDocument) {
      if (doc.groupName === group.name) {
        return true;
      }
      for (const linkedDoc of doc.linkedDocuments) {
        if (linkedDoc.groupName === group.name) {
          return true;
        }
      }
    }
    return result;
  }

  /**
   * Get the highlight color for the feed docuemnt element
   * @param id - the document id to highlight
   */
  getBackgroundColorHighlight(id: number) {
    if (this.currentDocument == null) {
      return '#fff';
    }
    if (id === this.currentDocument.id) {
      return '#5dd2ff';
    }else {
      return '#fff';
    }
  }

  getGroupFilterArray(groupNameFilter: string) {
    this.allGroupNames = new Array<string>();
    let startAddingFlag: boolean = false;
    let groupType: string;

    
    //
    // Reverse the array

    if (typeof groupNameFilter == 'undefined' || groupNameFilter === '') {
      startAddingFlag = true;
      
    }

    console.log('Group Name Filter --->' + JSON.stringify(groupNameFilter));

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();
    console.log('All Groups ---> ' + JSON.stringify(this.allGroups.subGroups));


    for (const group of this.allGroups.subGroups) {

      if (!startAddingFlag  && group.name === groupNameFilter) {
        startAddingFlag = true;
        groupType = group.groupType.name;
      }
      if (startAddingFlag) {
         if(group.groupType.name === groupType && group.name === groupNameFilter){
          this.allGroupNames.push(group.name);
         }else{
          if(group.groupType.name != groupType){
            this.allGroupNames.push(group.name);
          }
        }
      }

      console.log('Group Name ---> ' + JSON.stringify(group.name));
        
    }

    console.log('Current Filter Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    return this.allGroupNames;

  }

  getOrganizationFilterArray(groupNameFilter: string) {
    this.allGroupNames = new Array<string>();
    let startAddingFlag: boolean = false;
    let groupType: string;

    
    //
    // Reverse the array

    if (typeof groupNameFilter == 'undefined' || groupNameFilter === '') {
      startAddingFlag = true;
      
    }

    console.log('Group Name Filter --->' + JSON.stringify(groupNameFilter));

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();
    console.log('All Groups ---> ' + JSON.stringify(this.allGroups.subGroups));


    for (const group of this.allGroups.subGroups) {

      if (!startAddingFlag  && group.name === groupNameFilter) {
        startAddingFlag = true;
        this.allGroupNames.push(group.name);
      }
      console.log('Group Name ---> ' + JSON.stringify(group.name));
        
    }

    console.log('Current Filter Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    return this.allGroupNames;

  }

  /**
   * Get all the doc types that are required
   * @param groupNameFilter 
   */
  getOrganizationRequiredDocTypesFilterArray(groupNameFilter: string) {
    this.allGroupTraceRequiredDocNames = new Array<string>();
    this.allGroupTraceRequiredDocs = new Array<DocumentType>();
    let startAddingFlag: boolean = false;

    
    //
    // Reverse the array

    if (typeof groupNameFilter == 'undefined' || groupNameFilter === '') {
      startAddingFlag = true;
      
    }

    console.log('Group Name Filter --->' + JSON.stringify(groupNameFilter));

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();
    console.log('All Groups ---> ' + JSON.stringify(this.allGroups.subGroups));


    for (const group of this.allGroups.subGroups) {

      if (!startAddingFlag  && group.name === groupNameFilter) {
        startAddingFlag = true;
        for (const documentType of group.allowedDocTypes) {
          this.allGroupTraceRequiredDocNames.push(documentType.value);
          this.allGroupTraceRequiredDocs.push(documentType);
        }
      }
      console.log('Group Name ---> ' + JSON.stringify(group.name));
        
    }

    console.log('Current Filter Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    // this.allGroupTraceRequiredDocNames = ['Captains Statement', 'Farm License', 'Farmer License', 'Feed Information Form', 'Feed Lot Trace', 'Good Aquaculture Document', 'Hatchery License', 'Movement Document'];

    return this.allGroupTraceRequiredDocNames;

  }

  /**
   * Get The count of all the doc types that are required
   * @param groupNameFilter 
   */
  getOrganizationRequiredDocNumber(gridCell: Group) {
    var count: number = 0;
    let startAddingFlag: boolean = false;
    var groupNameFilter:string;
    var matchedDocs: Document[];

    

    if(gridCell === null){
      return count;
    }
    if(gridCell.groupType === null){
      return count;
    }
    console.log('[Count Doc Types] All Documents <dump> --->' + gridCell.name + ' [' + gridCell.allowedDocTypes.length + '] ' + JSON.stringify(gridCell.allowedDocTypes));
    return  gridCell.allowedDocTypes.length;

     /**
      * 
    //
    // init data
    groupNameFilter = gridCell.groupType.name;

    
    
    //
    // Reverse the array
    
    console.log('[Count Doc Types] Full Cell <required> --->' + JSON.stringify(gridCell));
    console.log('[Count Doc Types] Full Cell <required> --->' + JSON.stringify(gridCell));

    console.log('[Count Doc Types] Full Cell Allowed Docs# <required> --->', gridCell.allowedDocTypes.length);

    
    console.log('[Count Doc Types] All Documents <required> --->' + JSON.stringify(this.documents));

    console.log('[Count Doc Types] Group Name Filter <required> --->' + JSON.stringify(groupNameFilter));

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();
    console.log('[Count Doc Types] All Groups <required> ---> ' + JSON.stringify(this.allGroups.subGroups));


    for (const group of this.allGroups.subGroups) {

      if (group.groupType.name === groupNameFilter) {
        for (const documentType of group.allowedDocTypes) {
          count++;
        }
      }
      if(count > 0){
        break;
      }
      console.log('[Count Doc Types] Group Name <required> ---> ' + JSON.stringify(group.name));
      console.log('[Count Doc Types] Group ---> <required> ' + JSON.stringify(group));
        
    }

    console.log('[Count Doc Types] Current Filter Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    //
    // Walk through docs and find if the cell has it covered
    matchedDocs = this.documents.filter(function(value, index, arr){

      return value.groupName === groupNameFilter;
  
    });

    // this.allGroupTraceRequiredDocNames = ['Captains Statement', 'Farm License', 'Farmer License', 'Feed Information Form', 'Feed Lot Trace', 'Good Aquaculture Document', 'Hatchery License', 'Movement Document'];

    return count;
    */

  }

  /**
   * Get the gree or red color of the organization based on required docuemnt completion in a trace.
   * @param gridCell  - the gridd cell for the trace data
   */
  isOrganizationTraceFullyCompletedColor(gridCell: Group){
    if(this.getOrganizationRequiredDocNumber(gridCell) != this.getOrganizationRequiredDocNumberCovered(gridCell)){
      return 'shape-red-organization';
    }else{
      return 'shape-green-organization';
    }
  }

  /**
   * This computes the number of documents that have been covered by the given cell
   * @param gridCell - the gridd cell for the trace data
   */
  getOrganizationRequiredDocNumberCovered(gridCell: Group) {
    var count: number = 0;
    let startAddingFlag: boolean = false;
    var groupNameFilter:string;
    var matchedDocs: Document[];
    let uniqueDocs = new Map();


    if(gridCell === null || gridCell.groupType === null){
      return count;
    }

    //
    // init data
    groupNameFilter = gridCell.groupType.name;
    
    //
    // Reverse the array
    
    console.log('[Count Doc Types] Full Cell --->' + JSON.stringify(gridCell));
  

    //
    // Walk through docs and find if the cell has it covered
    for (const doc of this.documents){
      console.log('[Count Doc Types] <all docs> --->' + doc.type.value, doc.owner, doc.groupName, doc.status);
    }

    
    
    //
    // Get all document types for allowed docs and cycle
    for (const docType of gridCell.allowedDocTypes){
      console.log('[Count Doc Types] <allowed> --->', docType.name);
      //
      // for each document in our possesion
      for (const doc of this.documents){
          if(docType.name == doc.type.name 
              && doc.status == Document.STATUS_ACCEPTED
              && doc.groupName == gridCell.name
              && doc.id > 0
              && doc.partOfTrace
              && doc.owner != null){
                uniqueDocs.set(doc.type.name, 1);
                console.log('[Count Doc Types] <matched> --->' + ' [' + gridCell.name + '] ', JSON.stringify(doc.id), doc.type.value, doc.owner, doc.status);
                console.log('[Count Doc Types] <matching> --->', doc.type.value, doc.owner, doc.status);
              }
      }
    }

    /**

    matchedDocs = this.documents.filter(function(value, index, arr){

      // TODO - need to fix the issue with not having a trace logic 
      // for determining the doc number, has to be done per trace
      // return value.groupName == gridCell.name && value.status == Document.STATUS_ACCEPTED && value.type.documentDesignation == Document.TYPE_DESIGNATION_PASSTHROUGH;
      console.log('[Count Doc Types] <filter> --->' + JSON.stringify(gridCell));
      return value.groupName == gridCell.name && value.status == Document.STATUS_ACCEPTED;
    });
    console.log('[Count Doc Types] Matched Docs --->', matchedDocs.length);

    // this.allGroupTraceRequiredDocNames = ['Captains Statement', 'Farm License', 'Farmer License', 'Feed Information Form', 'Feed Lot Trace', 'Good Aquaculture Document', 'Hatchery License', 'Movement Document'];

    // remove duplicates
    if(matchedDocs.length > 0){
      for (const doc of matchedDocs){
        uniqueDocs.set(doc.documentType, 1);
        console.log('[Count Doc Types] <matching> --->' + doc.documentType);
      }
    }
    */
   console.log('[Count Doc Types] <matched size> --->', uniqueDocs.size);
    return uniqueDocs.size;

  }

  /**
   * Filter specifically by column name
   * @param groupTypeFilter 
   */
  getGroupTypeFilterArray(groupTypeFilter: string) {
    this.allGroupNames = new Array<string>();
    let startAddingFlag: boolean = false;
    
    //
    // Reverse the array

    if (typeof groupTypeFilter == 'undefined' || groupTypeFilter === '') {
      startAddingFlag = true;
      
    }

    console.log('Group Type Filter --->' + JSON.stringify(groupTypeFilter));

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();
    console.log('All Groups ---> ' + JSON.stringify(this.allGroups.subGroups));


    for (const group of this.allGroups.subGroups) {

      if (!startAddingFlag  && group.groupType.name === groupTypeFilter) {
        startAddingFlag = true;
      }
      if (startAddingFlag) {
         if(group.groupType.name === groupTypeFilter){
          this.allGroupNames.push(group.name);
         }else{
          //if(group.groupType.name != groupTypeFilter){
          //  this.allGroupNames.push(group.name);
          //}
        }
      }

      console.log('Group Name ---> ' + JSON.stringify(group.name));
      console.log('Group Rank ---> ' + JSON.stringify(group.groupType.orderIndex));
        
    }

    console.log('Current Filter Type Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    return this.allGroupNames;

  }

  getAllDocsGroupTypeFilterArray() {
    this.allGroupNames = new Array<string>();

    //
    // Reverse the group
    this.allGroups.subGroups.reverse();

    for (const group of this.allGroups.subGroups) {
          this.allGroupNames.push(group.name);
          console.log('Group Name ---> ' + JSON.stringify(group.name));
          console.log('Group Rank ---> ' + JSON.stringify(group.groupType.orderIndex));
      }

    console.log('Current Filter Type Group Names ---> ' + JSON.stringify(this.allGroupNames));

    this.allGroups.subGroups.reverse();

    return this.allGroupNames;

  }

  printTraceabilityMap() {

    const mapIter = this.traceabilityMap.values();
    /**
    for (const doc of mapIter) {
        console.log('Allowed Documents ' + JSON.stringify(doc));
    }
    */
  }

  /**
   * This method will mark specific records in the document feed as either being traceable or not.
   * Setting the trace docuemnts to 'true' would make the current trace document and its linked
   * chain as the only docuemnts traceable, eother wise a 'false' will reset the whole feed to make 
   * all docuemnts visible.
   * @param isTraceOn - the flag to mark specific (i.e. traceable) elements as either traceable or not.
   */
  setTraceDocuments(isTraceOn: boolean) {
    
    if(this.documents){
      if (isTraceOn) {
        // the trace flag is on, set only current trace docs to be flagged
        for (const doc of this.documents) {
          console.log("   Doc being looked for " + JSON.stringify(doc));
          if (this.tracedCurrDocument.find(tracedDoc => tracedDoc.id == doc.id)) {
            doc.partOfTrace = true;
          }else {
            doc.partOfTrace = false;
          }
        }
      }else {
        // the trace is off, make all the doc part of the filter
        for (const doc of this.documents) {
          doc.partOfTrace = true;
        }
      }
    }
  }

  resetFilterPriorityForGroup(item: string){

    // go through all elements and find the one that is in the group and change its sort time

  }

  executeSearch(){
    if(this.tagInputSearch001 === ''&& this.tagInputSearch002 === '' 
        && this.tagInputSearch003 === ''&& this.tagInputSearch004 === ''
        && this.tagInputSearch005 === ''&& this.tagInputSearch006 === ''
        && this.tagInputSearch007 === ''&& this.tagInputSearch008 === ''
        && this.tagInputSearch009 === ''&& this.tagInputSearch010 === ''){
          this.resetDocsforFilter();
          return;
    }
    console.log("   Lot # Search " + JSON.stringify(this.tagInputSearch001));
    for (const doc of this.documents) {
      if(this.isTagPresentInDocument(doc)){
        doc.lotFound = true;
      }else{
        doc.lotFound = false;
      }
    }
  }


  addLotSearchRow(){
    this.numberOfSearchLotFields++;
  }

  /**
   * Filter the document feed by date
   * @param item  - the date to filter by
   */
  tagInputValueChange(item: string) {
    // log it
    console.log('Lot # Search Trigger ' + item);
  }

  isTagPresentInDocument(doc: Document){

    for (const tag of doc.tags) {
      var currentTag: string = tag.text;
      console.log('Current Tag PRE' + currentTag);
      if(this.isCustomTagString(tag)){
        // strip the prefix
        currentTag = currentTag.replace(this.getCustomTagPrefix(), '');
      }
      currentTag = currentTag.toUpperCase().trim();

      console.log('Current Tag POST' + currentTag);

      if(!this.isPartialMatch){
        if (this.tagInputSearch001 != '' && currentTag === this.tagInputSearch001.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch002 != '' && currentTag === this.tagInputSearch002.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch003 != '' && currentTag === this.tagInputSearch003.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch004 != '' && currentTag === this.tagInputSearch004.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch005 != '' && currentTag === this.tagInputSearch005.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch006 != '' && currentTag === this.tagInputSearch006.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch007 != '' && currentTag === this.tagInputSearch007.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch008 != '' && currentTag === this.tagInputSearch008.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch009 != '' && currentTag === this.tagInputSearch009.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch010 != '' && currentTag === this.tagInputSearch010.toUpperCase()) {
          return true;
        }
      }else{
        if (this.tagInputSearch001 != '' && currentTag.includes(this.tagInputSearch001.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch002 != '' && currentTag.includes(this.tagInputSearch002.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch003 != '' && currentTag.includes(this.tagInputSearch003.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch004 != '' && currentTag.includes(this.tagInputSearch004.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch005 != '' && currentTag.includes(this.tagInputSearch005.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch006 != '' && currentTag.includes(this.tagInputSearch006.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch007 != '' && currentTag.includes(this.tagInputSearch007.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch008 != '' && currentTag.includes(this.tagInputSearch008.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch009 != '' && currentTag.includes(this.tagInputSearch009.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch010 != '' && currentTag.includes(this.tagInputSearch010.toUpperCase())) {
          return true;
        }
      }
      
    }
    return false;
  }

  resetDocsforFilter(){
    for (const doc of this.documents) {
        doc.lotFound = true;
    }
    this.isPartialMatch = true;
    this.tagInputSearch001 = '';
    this.tagInputSearch002 = '';
    this.tagInputSearch003 = '';
    this.tagInputSearch004 = '';
    this.tagInputSearch005 = '';
    this.tagInputSearch006 = '';
    this.tagInputSearch007 = '';
    this.tagInputSearch008 = '';
    this.tagInputSearch009 = '';
    this.tagInputSearch010 = '';
    this.numberOfSearchLotFields = 4;
  }

  getCustomTagPrefix(){
    return "CUSTOM:";
  }

    /** Internationalization */
    getInternationalizedToken(token: string){
      return this._documentService.internationalizeString(token);
    }

  getCanAcceptRejectDoc(){
    if(this.currentDocument == null || this.isDocumentOwner()){
      return false;
    }
    if(this.currentDocument.status == 'PENDING' || this.currentDocument.status == 'RESUBMITTED'){
      return true;
    }
    return false;
  }

  getCanRecallDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if(this.currentDocument.status == 'SUBMITTED' || this.currentDocument.status == 'RESUBMITTED'){
      if(this.isDocumentOwner()){
        return true;
      }
    }
    return false;
  }

  getCanDeleteDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if(this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED'){
      if(this.isDocumentOwner()){
        return true;
      }
    }
    return false;
  }

  getCanSubmitDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if(this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED'){
      if(this.isDocumentOwner()){
        return true;
      }
    }
    return false;
  }

  acceptDocument(){
      this.currentDocument.status = 'ACCEPTED';
      this._documentService.setDocumentStatus(this.currentDocument).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
      );
  }

  recallDocument(){
    this.currentDocument.status = 'DRAFT';
    this._documentService.recallDocument(this.currentDocument).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  deleteDocument(){
    this._documentService.deleteDocument(this.currentDocument).subscribe(
      data =>  {
        console.log('No issues [DELETE DOCUMENT');
        console.log('DELETE DOC ARRAY Before ---> ' + JSON.stringify(this.documents));
        this.documents = this.arrayRemove(this.documents, this.currentDocument); 
        console.log('DELETE DOC ARRAY After ---> ' + JSON.stringify(this.documents));
      },
      error => console.log('Server Error'),
    );
    // remove the document
    this.documents = this.arrayRemove(this.documents, this.currentDocument); 
    // reset the doc panel
    this.dismissTraceDocument(null);
  }

  submitDocument(){
    this.currentDocument.status = 'SUBMITTED';
    this._documentService.setDocumentStatus(this.currentDocument).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  rejectDocument(valueHeader, valueText){
        this.currentDocument.status = 'REJECTED';

        // set the data in the document
        var note:NoteData = new NoteData();
        note.note = valueHeader + AppGlobals.FORMATTING_DELIMITER + valueText;
        note.owner = JSON.parse(localStorage.getItem('user')).name;
        this.currentDocument.notes.push(note);

        this._documentService.setDocumentStatus(this.currentDocument).subscribe(
          data =>  console.log('No issues'),
          error => console.log('Server Error'),
        );

        this._documentService.AddDocumentNote(note, this.currentDocument).subscribe(
          data =>  console.log('No issues'),
          error => console.log('Server Error'),
        );

  }

  isDocumentOwner(){
    if(JSON.parse(localStorage.getItem('user')).name == this.currentDocument.owner){
      return true;
    }else{
      return false;
    }
  }

  getNoteHeader(note:NoteData){
    console.log('note - full data - '.concat(note.note));
    var noteStrings = new Array();
    noteStrings = note.note.split(AppGlobals.FORMATTING_DELIMITER);
    return noteStrings[0];
  }

  getNoteText(note:NoteData){
    return note.note.split(AppGlobals.FORMATTING_DELIMITER)[1];
  }

  getLabelForStatus(doc: Document){
    if(doc.status == 'PENDING'){
      return 'label label-warning';
    }
    if(doc.status == 'SUBMITTED'){
      return 'label label-info';
    }
    if(doc.status == 'ACCEPTED'){
      return 'label label-success';
    }
    if(doc.status == 'REJECTED'){
      return 'label label-danger';
    }
    return 'label label-info';
    
  }

  getClassForTagSearchProcessButton(name:string){
    var isEnabled = this.linkedDocListBySearchTag.length > 0;
    if(name == 'Link Documents'){
      // is the doc type chosen?

      if(isEnabled){
        return 'btn btn-primary btn-xs active';
      }else{
        return 'btn btn-primary btn-xs disabled';
      }
    }
  }
  getClassForUploadProcessButton(name:string){
    var isEnabled = 
      !(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
      && this.selectedPDFFileToUpload !== undefined
      && this.selectedPDFFileToUpload !== null;
    if(name == 'Browse'){
      // is the doc type chosen?

      if(isEnabled){
        return 'btn btn-warning btn-xs active';
      }else{
        return 'btn btn-warning btn-xs disabled';
      }
    }
    if(name == 'Save'){
      // is the doc type chosen?

      if(isEnabled){
        return 'btn btn-info btn-xs active';
      }else{
        return 'btn btn-info btn-xs disabled';
      }
    }
    if(name == 'Submit'){
      // is the doc type chosen?

      if(isEnabled){
        return 'btn btn-info btn-xs active';
      }else{
        return 'btn btn-info btn-xs disabled';
      }
    }
    if(name == 'Cancel'){
      // is the doc type chosen?

      if(this.selectedPDFFileToUpload !== undefined
        && this.selectedPDFFileToUpload !== null){
        return 'btn btn-danger btn-xs active';
      }else{
        return 'btn btn-danger btn-xs disabled';
      }
    }

    return 'btn btn-danger btn-xs active';
    
  }

  importPDFDocData(){
    var result = {
      // url: 'http://website.com/upload'
      url: this._documentService.getServerURI().concat('/document/upload'),
      allowedExtensions: ['.pdf'],
      data: { 
          userName: this.username, 
          creationDate: this.getCurrentDate(),
          docTypeName : this.getImportDocTypeName(),
          docTypeId : this.getImportDocTypeId(),
          docTypeHexColorCode : this.getImportDocTypeHexColorCode(),
          docRecipients: this.getRecipientsToCreate(),
          docLinkedDocs: this.getLinkedDocsToCreate(),
          docBackingDocs: this.getBackingDocsToCreate(),
          docImportDocTags: this.getImportDocTagsCreate()
      }
    }


      return result;
  }

  getCurrentDate(){
    var currDate =  new Date().toISOString(); 
    var currDateArray = currDate.split('T');
    return currDateArray[0] + ' ' + currDateArray[1].substring(0,8);
  }

  getImportDocTypeName(){
    if(this.currectDocumentTypeForNewDoc == null){
      return 'Fishmeal Lot Traceability';
    }else{
      return this.currectDocumentTypeForNewDoc.value;
    }
    
  }

  getImportDocTypeId(){
    if(this.currectDocumentTypeForNewDoc == null){
      return 5;
    }else{
      return this.currectDocumentTypeForNewDoc.id;
    }
  }

  getImportDocTypeHexColorCode(){
    if(this.currectDocumentTypeForNewDoc == null){
      return '#ffe250fb';
    }else{
      return this.currectDocumentTypeForNewDoc.hexColorCode;
    }
  }

  getAllowedDocTypes(){
    console.log('PDF Import <doc types allowed> <user> ---> ' + JSON.stringify(this.currentUser));
    console.log('PDF Import <doc types allowed> ---> ' + JSON.stringify(this.currentUser.userGroups[0].allowedDocTypes));
    return this.currentUser.userGroups[0].allowedDocTypes;;
  }

  getRecipientsToCreate(){
    var result:string = '';

    // go through each chosen recipinet and extract the id
    for (const recipient of this.currentDocumentRecipients) {
      result += ',' + recipient.id;
    }
    if(result != null){
      result = result.substr(1);
    }
    return result;
  }

  getLinkedDocsToCreate(){
    var result:string = '';

    // go through each chosen recipinet and extract the id
    for (const linkedDoc of this.currentDocumentLinks) {
      result += ',' + linkedDoc.id;
    }
    if(result != null){
      result = result.substr(1);
    }
    console.log('getLinkedDocsToCreate ---> : '.concat(JSON.stringify(result)));
    return result;
  }

  getBackingDocsToCreate(){
    var result:string = '';

    // go through each chosen recipinet and extract the id
    for (const backingDoc of this.currentDocumentBacking) {
      result += ',' + backingDoc.id;
    }
    if(result != null){
      result = result.substr(1);
    }
    console.log('getBackingDocsToCreate ---> : '.concat(JSON.stringify(result)));
    return result;
  }

  getImportDocTagsCreate(){
    var result:string = '';

    // go through each chosen recipinet and extract the id
    for (const tag of this.currentDocumenTags) {
      result += ',' + tag.id;
    }
    if(result != null){
      result = result.substr(1);
    }
    console.log('getImportDocTagsCreate ---> : '.concat(JSON.stringify(result)));
    return result;
  }

  onSelect(event: any){
    console.log('SELECT EVENT ---> : '.concat(JSON.stringify(event)));
  }

  /**************************************************************************
   * Auto Tag Linking of Documents
   * 
   */

  /**
   * Auto Link documents based on teh tags that the user has chosen.
   * 
   * @param linkingTags - the tags that were chosen by the user
   */
  linkDocsByTag(linkingTags: DocumentTag[]){
    var currentDocumentLinksNew = new Array<Document>();

    console.log('NEW DOCS_ TAGS ---> : '.concat(JSON.stringify(linkingTags)));
    console.log('PREVIOUS DOCS_ TAGS ---> : '.concat(JSON.stringify(this.linkingDocsByTags)));

    // go through all the documents to find the ones that have the same tag
    var linkingDocsByTagsNew: Document[] = new Array<Document>();

    console.log('NEW DOCS_ ---> : '.concat(JSON.stringify(linkingDocsByTagsNew)));

    //
    // create a new list of docs for the tags
    for(const linkingTag of linkingTags){
      //if(linkingTag.text.includes(AppGlobals.CUSTOM_TAG_PREFIX)){
        for(const doc of this.getDocsToLink()){
          // get the tags
          for(const tag of doc.tags){
            // check if the tag is the same
            if(tag.text === linkingTag.text){
              linkingDocsByTagsNew.push(doc);
            }
          }
        }
      //}
    }

    console.log('NEW DOCS_ AFTER---> : '.concat(JSON.stringify(linkingDocsByTagsNew)));

    // 
    // Remove the olds list entries from the main list
    for(const linkTagDoc of this.linkingDocsByTags){
      console.log('REMOVING ATTEMPT <doc tags> OLD DOCS_ ---> : '.concat(JSON.stringify(linkTagDoc)));
      var index:number = this.currentDocumentLinks.indexOf(linkTagDoc);
        if(index != -1){
          this.currentDocumentLinks.splice(index, 1);
          console.log('--- REMOVING <doc tags> OLD DOCS_ ---> : '.concat(JSON.stringify(linkTagDoc)));
        }
    }

    console.log('REMOVING OLD DOCS_ ---> : '.concat(JSON.stringify(this.currentDocumentLinks)));

    //
    // Add the new entries into the list

    for(const linkTagDocTemp of linkingDocsByTagsNew){
      var index:number = this.currentDocumentLinks.indexOf(linkTagDocTemp);
        if(index == -1){
          currentDocumentLinksNew.push(linkTagDocTemp);
        }
    }
    currentDocumentLinksNew = currentDocumentLinksNew.concat(this.currentDocumentLinks);
    console.log('ADDING NEW DOCS_ ---> : '.concat(JSON.stringify(currentDocumentLinksNew)));

    // 
    // replace the two lists
    this.linkingDocsByTags = linkingDocsByTagsNew;
    this.currentDocumentLinks = currentDocumentLinksNew;

    
  }

  /**
   * get the documents that this user can link to
   */
  getDocsToLink(){
    var result = new Array<Document>();
    if(this.documents == null){
      return result;
    }
    for(const doc of this.documents){
        //only if the user is a recipinet
        // <TODO> 
        if(doc.toRecipients.find(recipient => recipient.id == this.currentUser.id) && doc.owner != this.username){
          result.push(doc);
        }
     }
     result = result.concat(this.linkingDocsExtraPermissions);

    return result;
  }

  getTagsToLink(){
    var result = new Array<DocumentTag>();
    if(this.importTagsList == null){
      return result;
    }
    result = this.importTagsList;

    return result;
  }

  getTagsToLink2(){
    var result = new Array<DocumentTag>();
    if(this.importTagsList == null){
      return result;
    }
    result = this.importTagsList.splice(0,4);

    return result;
  }

  showTagManagementScreen(){

  }

  getDocsToBackup(){
    var result = new Array<Document>();
    if(this.documents == null){
      return result;
    }
    for(const doc of this.documents){
      if(doc.owner == this.currentUser.name){
        result.push(doc);
      }
    }

    return result;
  }

  /**
   * Get the internationalized status of the document
   * @param status  - the document status
   */
  getStatusInternationalizedStringCode(status : string){
    return this._documentService.getStatusInternationalizedStringCode(status);
  }

  getDocumentsByTagSearchSelection(tag: string){
    console.log("SEARCH TAG VALUE = " + tag);
  }


  /**
   * Getting and storing the file that was chosen in the page
   * @param event  - the veent of the file selection
   */
  onPDFFileSelected(event){
    this.selectedPDFFileToUpload = event.target.files[0];
    console.log("PDF FIle Upload" + event.target.files[0]);
    console.log(event);
  }

  onPDFFileUpload(docStatus){

    var isEnabled = 
    !(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
    && this.selectedPDFFileToUpload !== undefined
    && this.selectedPDFFileToUpload !== null;

    if(isEnabled){
        var fd = new FormData();
        fd.append('file', this.selectedPDFFileToUpload);
        fd.append('userName', this.username);
        fd.append('creationDate', this.getCurrentDate());
        fd.append('docTypeName', this.getImportDocTypeName());
        fd.append('docTypeId', ''+this.getImportDocTypeId());
        fd.append('docTypeHexColorCode', this.getImportDocTypeHexColorCode());
        fd.append('docRecipients', this.getRecipientsToCreate());
        fd.append('docLinkedDocs', this.getLinkedDocsToCreate());
        fd.append('docBackingDocs', this.getBackingDocsToCreate());
        fd.append('docImportDocTags', this.getImportDocTagsCreate());
        fd.append('docImportDocStatus', docStatus);
        
        console.log("PDF FIle Upload FROM DATA <status> ---> " + docStatus);

        console.log("PDF FIle Upload FROM DATA---> " + JSON.stringify(fd));
        this.startProgress();

        this._documentService.startUploadProcess();

        this._documentService.onFileUpload(this.selectedPDFFileToUpload, fd, this.slimLoader, this._documentService, this);
    }
  }

  onPDFFileRemoveChoice(){
    (<HTMLInputElement>document.getElementById("input-file-now")).value = "";
    this.selectedPDFFileToUpload = null;

  }

  /************************************************************************
   * Tag Search Processing and Doc Linking
   */


   /**
    * Process the act of linking the documents frin teh search to the feed and to the linked docs list
    */
  processSearchTagLinking(){

    var isEnabled = this.linkedDocListBySearchTag.length > 0;
    if(!isEnabled){
      return;
    }


    var currSearchTag: DocumentTag = null;
    var currSearchTagDoc: Document;
    // new list of selected tags
    var currentDocumenTagsNew = new Array<DocumentTag>();

    // get the first doc and extract the tag from it
    currSearchTagDoc = this.linkedDocListBySearchTag[0];

    //
    // Add the found docs to the linked docs
    this.linkingDocsExtraPermissions = this.linkingDocsExtraPermissions.concat(this.linkedDocListBySearchTag);
    console.log("SEARCH TAG <docs found> --> " + JSON.stringify(this.linkedDocListBySearchTag));

    // 
    // Add it to the feed docs
    this.documents = this.documents.concat(this.linkedDocListBySearchTag);

    // get the tag used to searchand add it to the list of tags
    for(const tag of this.getTagsToLink()){ 
      console.log("SEARCH TAG <Tag Loop> --> " + JSON.stringify(this.stripTag(tag)));
      if(this.stripTag(tag).includes(this.searchDocsByTagInputText)){
        currSearchTag = tag;
        break;
      }
    }
    console.log("SEARCH TAG <select list pre> --> " + JSON.stringify(this.currentDocumenTags))
    console.log("SEARCH TAG DOC --> " + JSON.stringify(currSearchTagDoc));
    console.log("SEARCH TAG TEXT--> " + JSON.stringify(this.searchDocsByTagInputText));
    console.log("SEARCH TAG --> " + JSON.stringify(currSearchTag));


    // 
    if(currSearchTag !== null){
      currentDocumenTagsNew.push(currSearchTag);
    }else{
      //create this tag as a new tag in the backend
      //
      // <TODO>
    }
   
    
    // check if the tag already exists
    var index:number = this.currentDocumenTags.indexOf(currSearchTag);
    if(index != -1){
      this.currentDocumenTags.splice(index, 1);
    }
    console.log("SEARCH TAG <result pre> --> " + JSON.stringify(currentDocumenTagsNew));
    // consolidate the list
    this.currentDocumenTags = currentDocumenTagsNew;

    // <TODO>
    //this.currentDocumenTags = [this.getTagsToLink()[0]];
    this.linkDocsByTag(this.currentDocumenTags);
    console.log("SEARCH TAG <result post> --> " + JSON.stringify(this.currentDocumenTags));

    //
    //clear the screen for popup
    this.linkedDocListBySearchTag = new Array<Document>();
    this.searchDocsByTagInputText = "";
  }

  /**
   * 
   */
  processSearchTagCancel(){
    this.linkedDocListBySearchTag = new Array<Document>();
    this.searchDocsByTagInputText = "";
  }

  isLinkedDocListBySearchTagResultEmpty(){
    return this.linkedDocListBySearchTag.length <=0;
  }

  getSearchTagDocListMessage(){
    // check if we have anything to show
    if(this.linkedDocListBySearchTag == null){
      return "";
    }
    if(this.linkedDocListBySearchTag.length !== 0){
      return this.getInternationalizedToken('document_search_tag_documents_found_to_link') + ': ' +  this.linkedDocListBySearchTag.length;
    }else{
      return this.getInternationalizedToken('document_search_tag_documents_none_found_to_link');
    }
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

  isUploadingDoc(){
    return this._documentService.isUploadingDoc();
  }

  isTagLinkDocSearching(){
    return this._documentService.isTagLinkDocSearching();
  }

  getDocumentPages(docId){
    var document = this.getDocumentByDocId(docId);
    if(document){
      return document.pages;
    }else{
      return [];
    }
  }

  getDocumentPagesLength(docId){
    var document = this.getDocumentByDocId(docId);
    if(document){
      return document.pages.length;
    }else{
      return 0;
    }
  }

  /******************************************************************************************
   * Tag Administration
   */
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
    //
    // reload all the tags in the field

  }

  onEditConfirm(event): void {

  }

  /**
   * Create a new Tag. The method returns the tag that was created.
   * @param newTag - the new tag to be created
   */
  createTag(newTag: DocumentTag){
    this.tagService.createNewTag(newTag).subscribe(
      data => { 
        var currSearchTag: DocumentTag = data.json();
        console.log('CREATE NEW TAG --->' + JSON.stringify(currSearchTag));
        // Push the data into the existing list
        this.importTagsList.unshift(currSearchTag);
        // set the selection to this element as well

        
        // new list of selected tags
        var currentDocumenTagsNew = new Array<DocumentTag>();
        currentDocumenTagsNew.push(currSearchTag);

       
        
        // check if the tag already exists
        var index:number = this.currentDocumenTags.indexOf(currSearchTag);
        if(index != -1){
          this.currentDocumenTags.splice(index, 1);
        }
        // consolidate the list
        this.currentDocumenTags = currentDocumenTagsNew;
    
        // <TODO>
        //this.currentDocumenTags = [this.getTagsToLink()[0]];
        this.linkDocsByTag(this.currentDocumenTags);
        console.log("SEARCH TAG <result post> --> " + JSON.stringify(this.currentDocumenTags));
    
      },
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

  getDocumentTypeListTarget(){
    return "#myModalTagSearch";
  }

  getCurrentGroupNameForTrace(){

  }

  /**
   * Remove any dummy docs from the documents feed
   */
  clearDummyDocs(){
    //
    // 
    console.log('-- CLEAR DUMMY DATA: BEFORE  ' + JSON.stringify(this.documents) + ' ' + this.documents.length);
    this.documents = this.documents.filter(function(value, index, arr){

      return value.id > 0;
  
    });
    console.log('-- CLEAR DUMMY DATA: AFTER ' + JSON.stringify(this.documents) + ' ' + this.documents.length);
  }

  /**
   * This creates dummy docs in the listing to dtand in for the missing docs
   * @param item The item for whcih to ad the dummy docs
   */
  addDummyDocsForRequiredDocs(item:any){
    //
    // get a new list of doc types
    this.getOrganizationRequiredDocTypesFilterArray(item);

    var currentDummyDocs = new Array<Document>();
    var newId:number = 0;
    for(const docType of this.allGroupTraceRequiredDocs){ 
      
      // create a new docuemnt with the data
      let doc= Object.assign({}, this.documents[this.documents.length - 1]);
      //var doc: Document = new Document();
      doc.id = newId;
      doc.type = docType;
      doc.groupName = item;
      doc.creationTimestamp = new Date().toString();
      doc.partOfTrace = true;
      // clear some data
      doc.owner = null;
      doc.pages = [];
      currentDummyDocs.push(doc);
      newId--;
      console.log("Adding Doc <Required Doc> --> " + JSON.stringify(doc));
    }

    this.documents = this.documents.concat(currentDummyDocs);
    console.log("Adding Doc <Required Documents> --> " + JSON.stringify(this.documents) + ' ' + this.documents.length);
  }

  isDummy(document:Document){
    if(document.id <= 0){
      return true;
    }else{
      return false;
    }
  }


  getClassForRequiredDocumentStatus(document:Document){
    //
    // find the same doc type with the organization but not a dummy
    var foundDocs: Document[];
    var foundDoc: Document;
    
    // search the array
    foundDocs = this.documents.filter(function(value, index, arr){
      return (document.groupName == value.groupName
        && value.type.value== document.type.value
        && value.id > 0);
    });

    if(foundDocs.length > 0){
      foundDoc = foundDocs[0];
    }else{
      foundDoc = null;
    }
    
      // if we have found a document 
    if(foundDoc != null){
      // gets its status
      if(foundDoc.status == Document.STATUS_ACCEPTED){
        return 'requiredDocGreen';
      }
      if(foundDoc.status == Document.STATUS_DRAFT || foundDoc.status == Document.STATUS_REJECTED){
        return 'requiredDocRed';
      }
      if(foundDoc.status == Document.STATUS_PENDING || foundDoc.status == Document.STATUS_SUBMITTED || foundDoc.status == Document.STATUS_RESUBMITTED){
        return 'requiredDocYellow';
      }
      
    }
    // The docuemnt does not exist
    else{
      return 'requiredDocReD';
    }
  }

  getClassLabelForRequiredDocumentStatus(document:Document){
    //
    // find the same doc type with the organization but not a dummy
    var foundDocs: Document[];
    var foundDoc: Document;
    
    // search the array
    foundDocs = this.documents.filter(function(value, index, arr){
      return (document.groupName == value.groupName
        && value.type.value== document.type.value
        && value.id > 0);
    });

    if(foundDocs.length > 0){
      foundDoc = foundDocs[0];
    }else{
      foundDoc = null;
    }

      // if we have found a document 
    if(foundDoc != null){
      // gets its status
      if(foundDoc.status == Document.STATUS_ACCEPTED){
        return 'label label-success';
      }
      if(foundDoc.status == Document.STATUS_DRAFT || foundDoc.status == Document.STATUS_REJECTED){
        return 'label label-danger';
      }
      if(foundDoc.status == Document.STATUS_PENDING || foundDoc.status == Document.STATUS_SUBMITTED || foundDoc.status == Document.STATUS_RESUBMITTED){
        return 'label label-warning';
      }
      
    }
    // The docuemnt does not exist
    else{
      return 'label label-danger';
    }
  }

  /**
   * Get the styling border color for the dummy document status
   * @param document - the document to get the color status for.
   */
  getBorderColorForRequiredDocumentStatus(document:Document){
    //
    // find the same doc type with the organization but not a dummy
    var foundDocs: Document[];
    var foundDoc: Document;

    console.log("Doc Feed Mini-Card <before> --> " +  JSON.stringify(document));
    console.log("Doc Feed Mini-Card <before> --> " + document.owner + ": " + document.type.documentDesignation + ": " + document.groupName + ": " + document.type.value + ": " + document.status);
    
    // search the array
    foundDocs = this.documents.filter(function(value, index, arr){
      return (document.groupName == value.groupName
        && value.type.value == document.type.value
        && value.id > 0
        && value.partOfTrace);
    });

    if(foundDocs.length > 0){
      foundDoc = foundDocs[0];
      console.log("Doc Feed Mini-Card <found> --> " + foundDoc.type.documentDesignation + ": " + foundDoc.groupName + ": " + foundDoc.type.value + ": " + foundDoc.status);
    }else{
      console.log("Doc Feed Mini-Card <*NOT* found> --> ");
      // document.status == Document.STATUS_ACCEPTED ||  document.status == Document.STATUS_SUBMITTED) && 
      if(document.type.documentDesignation == Document.TYPE_DESIGNATION_PROFILE){
        foundDocs = this.documents.filter(function(value, index, arr){
          return (document.groupName == value.groupName
            && value.type.value == document.type.value
            && value.id > 0
            && value.owner != null
            && value.partOfTrace)
            
        });
        if(foundDocs.length > 0){
          foundDoc = foundDocs[0];
          console.log("Doc Feed Mini-Card <found PROFILE> --> " + foundDoc.type.documentDesignation + ": " + foundDoc.groupName + ": " + foundDoc.type.value + ": " + foundDoc.status);
        }
      }else{
        foundDoc = null;
      }
      
    }

      // if we have found a document 
    if(foundDoc != null){
      // gets its status
      if(foundDoc.status == Document.STATUS_ACCEPTED){
        return '#28a745'; // GREEN
      }
      if(foundDoc.status == Document.STATUS_DRAFT || foundDoc.status == Document.STATUS_REJECTED){
        return '#dc3545'; //RED
      }
      if(foundDoc.status == Document.STATUS_PENDING || foundDoc.status == Document.STATUS_SUBMITTED || foundDoc.status == Document.STATUS_RESUBMITTED){
        return '#ffc107'; // ORANGE
      }
      
    }
    // The docuemnt does not exist
    else{
      return '#dc3545'; // RED
    }
  }

  /**
   * Get teh Text for class (CSS) that defines the document status
   * @param document  - the document to label
   */
  getClassLabelTextForRequiredDocumentStatus(document:Document){
    //
    // find the same doc type with the organization but not a dummy
    var foundDocs: Document[];
    var foundDoc: Document;
    
    // search the array
    foundDocs = this.documents.filter(function(value, index, arr){
      return (document.groupName == value.groupName
        && value.type.value== document.type.value
        && value.id > 0);
    });

    if(foundDocs.length > 0){
      foundDoc = foundDocs[0];
    }else{
      foundDoc = null;
    }

      // if we have found a document 
    if(foundDoc != null){
      // gets its status
      if(foundDoc.status == Document.STATUS_ACCEPTED){
        return 'OK';
      }
      if(foundDoc.status == Document.STATUS_DRAFT || foundDoc.status == Document.STATUS_REJECTED){
        return 'REQUIRED';
      }
      if(foundDoc.status == Document.STATUS_PENDING || foundDoc.status == Document.STATUS_SUBMITTED || foundDoc.status == Document.STATUS_RESUBMITTED){
        return 'IN-PROGRESS';
      }
      
    }
    // The docuemnt does not exist
    else{
      return 'REQUIRED';
    }
  }

  arrayRemove(arr, document:Document) {

    return arr.filter(function(documentElement:Document){
        return documentElement.id != document.id;
    });
 
 }

 SuccessToaster(){
   this.toasterService.Success("Success Clicked");
 }

 InfoToaster(){
  this.toasterService.Info("Info Clicked");
}

WarningToaster(){
  this.toasterService.Warning("Warning Clicked");
}

ErrorToaster(){
  this.toasterService.Error("Error Clicked");
}

getCurrentTraceDocumentId(){
  console.log('Trace Document ID '.concat('' + this.tracedCurrDocument[0].id));
  return this.tracedCurrDocument[0].id
}


}
