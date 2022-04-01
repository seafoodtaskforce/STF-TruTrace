import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, ElementRef } from '@angular/core';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';

import { DocumentService } from './document.service';
import { Document } from '../../models/document';
import { DocumentType } from '../../models/documentType';
import { User } from '../../models/user';
import { Role } from '../../models/role';
import { Group } from '../../models/group';
import { GroupType } from '../../models/groupType';
import { GroupList } from '../../models/groupList';
import { DocumentTag } from '../../models/documentTag';
import { ScrollToAnimationEasing, ScrollToEvent, ScrollToOffsetMap, ScrollToConfigOptions, ScrollToService } from '@nicky-lenaers/ngx-scroll-to';
import { NoteData } from '../../models/noteData';
import { NgUploaderOptions } from 'ngx-uploader';

// import global data
import * as AppGlobals from '../../config/globals';
import { LocaleUtils } from '../../utils/locale.utils';
import { InterComponentDataService } from "../../utils/inter.component.data.service";

// Smart Tables
import { LocalDataSource } from 'ng2-smart-table';
import { TagAdmin } from '../../models/admin/tagAdmin';
import {SmartTableListItem} from '../../models/admin/smartTableListItem';
import {DataLoadService} from '../adminPortal/dataLoad.service';

// Toaster
import { ToasterService } from '../../toaster-service.service';


// Photo Viewer
import * as $ from 'jquery';

//
// Dynamic Data
import { DynamicFieldData } from 'app/models/dynamicFieldData';
import { DynamicFieldType } from 'app/models/dynamicFieldType';

//
// Gallery
import { NgxGalleryOptions, NgxGalleryImage, NgxGalleryAnimation } from 'ngx-gallery';
import { DynamicFieldDefinition } from 'app/models/dynamicFieldDefinition';
import { ApplicationErrorData } from 'app/models/applicationErrorData';
import { Page } from 'app/models/page';
import {CustomEvent} from 'ngx-image-viewer';

//
// Drag and Drop
import { DragulaService } from 'ng2-dragula/ng2-dragula';

//
// Modal Popups
import {NgbModal, ModalDismissReasons}  
      from '@ng-bootstrap/ng-bootstrap'; 
import { stringify } from 'querystring';

//
// Mapping
import 'leaflet-map';
import { ArrayUtils } from 'app/utils/array.utils';
import { LeafletMaps } from './leafletMaps/leafletMaps.component';
import { Router } from '@angular/router'
import { DateUtils } from 'app/utils/date.utils';



enum FilterFlags {
  RESET_FILTER_GLOBAL = 1,
  RESET_FILTER_NEW_DOCUMENT = 2,
  RESET_FILTER_EDIT_DOCUMENT = 3,
  RESET_FILTER_TRACE = 4,
  RESET_FILTER_TAG_SEARCH = 5
}

enum PageViewerSource {
  NOT_ASSIGNED = 0,
  DOC_FEED_DETAILS = 1,
  DOC_CREATION_PREVIEW = 2,
  DOC_EDIT_PREVIEW = 3,
  DOC_LINKED_DOC_TAB_PREVIEW = 4,
  DOC_BACKING_DOC_TAB_PREVIEW = 5,
}

@Component({
  selector: 'documents',
  templateUrl: './documents.html',
  styleUrls: ['./documents.scss']
})
export class DocumentsComponent implements OnInit {

  /**
   * COnstants
   */
  public static readonly SESSION_STORAGE_KEY_MAP_GROUPS = "key.mapping.groups";

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
   * Gallery
   */
  galleryOptions: NgxGalleryOptions[];
  galleryImages: NgxGalleryImage[];

  /**
   * Creation of new document data/editing doc data
   */
  currectDocumentTypeForNewDoc: DocumentType = null;
  currectDocumentTypeForNewDocListValue: string;
  currentDocumentRecipients: User[] = new Array<User>();
  currentDocumentRecipient: User = new User();
  currentDocumentLinks: Document[] = new Array<Document>();
  currentDocumentBacking: Document[] = new Array<Document>();
  currentDocumenTags: DocumentTag[] = new Array<DocumentTag>();
  
  newDocumentCreationFlag: boolean = true;
  newDocumentDynamicFieldDefinitions : DynamicFieldDefinition[] = new Array<DynamicFieldDefinition>();
  newDocumentDynamicFieldData : DynamicFieldData[] = new Array<DynamicFieldData>();
  newDocumentCreationLinkedDocsEnabledFlag: boolean = true;
  newDocumentCreationBackingDocsEnabledFlag: boolean = true;
  documentEditionLinkedDocsEnabledFlag: boolean = true;
  documentEditionBackingDocsEnabledFlag: boolean = true;
  documentDocDataPanelEnabledFlag: boolean = true;
  // errors
  newDocumentDynamicFieldDataErrors : ApplicationErrorData[] = new Array<ApplicationErrorData>();

  //
  // Sorting
  isFilterDateRangeOn: boolean = false;
  filterDocDateFrom: string = '2222-04-04';
  filterDocDateTo: string = '';
  datesToFilter: string[] = new Array<string>();
  minDocDate: Date;
  maxDocDate: Date;

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
  selectedUploadPDFFiles: File[] = new Array<File>();
  currentDocumentFileList: File[] = new Array<File>();

  /**
   * Trace Data Input hacks
   */

   element: HTMLElement;

  /**
   * Trace Data
   */
  allGroupTraceRequiredDocs: DocumentType[]  = new Array<DocumentType>();
  allGroupTraceRequiredDocNames: string[] = new Array<string>();

  /**
   * Notification Session ID for Document Detail
   */
  notificationbasedDcumentSessionId : string = null;
  showNotificationDetailsFlag : boolean = false;

  /**
   * Page Image 
   */
  pageViewWidgetIndex : number = -1;
  viewWidgetPages : string[] = new Array<string>();
  currentPageIndex : number = -1;
  pageViewWidgeSource: PageViewerSource = PageViewerSource.NOT_ASSIGNED;
  viewWidgetCurrentDocumentId : number = -1;


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

  options: any = {
    removeOnSpill: true
  }

  //
  // Modal Popups
  closeResult = '';

  constructor(private _documentService: DocumentService, private _scrollToService: ScrollToService
            , private slimLoader: SlimLoadingBarService, protected tagService : DataLoadService
            , protected toasterService:ToasterService, protected _documentDetailTrigger: InterComponentDataService
            , private _cdr: ChangeDetectorRef, private dragula: DragulaService, private modalService: NgbModal
            , private _elementRef:ElementRef, private router: Router) {
      this.ngxScrollToEvent = 'mouseenter';
      this.ngxScrollToDuration = 1500;
      this.ngxScrollToEasing = 'easeOutElastic';
      this.filterDocsFlagButton = 'doc_feed_[FilterAllDocs]';

      this.username = localStorage.getItem('username');
      this.currentUser = JSON.parse(localStorage.getItem('user'));

      this.fileUploaderOptions = this.importPDFDocData();

      dragula.removeModel.subscribe((value) => {
        this.onRemoveModel(value);
      });
  }


  readonly SORT_DATE_ASCENDING= 'document_filter_sort_newest';
  readonly SORT_DATE_DESCENDING= 'document_filter_sort_oldest';
  readonly FILTER_USER_ALL= 'document_filter_sort_by_user_all';
  readonly FILTER_DOCTYPE_ALL= 'document_filter_sort_by_doc_types_all';
  readonly FILTER_IMPORT_DOCTYPE_ALL= 'document_import_choose_doc_type';

  //
  // Linked and backing docs show indicator
  showBackigDocDetails = false
  showDocDetailsPagePreview = false
  showBackigDocDetailsCountBefore = 0;
  showBackigDocDetailsCountAfter = 0;
  currentDocumentToShow : Document = new Document(); 

  showDocuemntDetailsflag = false;
  currentDocument: Document;
  currentRevertDocumentPages : Array<Page>;
  documents: Array<Document>;
  shadowFeedDocuments : Array<Document>;
  tempDocuments: Array<Document>;
  documentTypes: Array<DocumentType>;
  unfilteredDocuments: Array<Document>;
  users: Array<User>;
  recipients: Array<User>
  importTagsList: Array<DocumentTag>;
  temp = Array;
  math = Math;
  docTypeColor: string = ' #ffffff';
  sortDateAscending= true;
  sortDateFieldName= 'updationServerTimestamp';
  showFilter = true;
  showTracebilityCard = false;
  tabElements: string[] = new Array<string>();
  traceQueueddDocuments: Array<Document>;
  filterDocsFlag:boolean = false;
  filterDocsFlagButton:string; 

  currentRejectionNote: string = "Hello THere";

  /**
   * Dragg and Drop
   */
  dragulaMessage : string = "None";

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
  traceabilityStageGridMapTemp: Map<string, Group[]> = new Map<string, Group[]>();

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
  filterSelectedUserChoice = this.getInternationalizedToken(this.FILTER_USER_ALL);
  filterSelectedSortChoice = this.getInternationalizedToken(this.SORT_DATE_ASCENDING);
  filterSelectedDocTypeChoice = this.getInternationalizedToken(this.FILTER_DOCTYPE_ALL);
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
  docEditOn: boolean = false;
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
    partOfFeed:true,
  };

  //
  // Filtering for a trace
  documentTraceFilter: any = {
    groupName: '',    // filter by group name
  };

  //
  //
  showDocsDateSortButtonIconIsUp : boolean = true;

  // The URL for the server REST communication
  serverURI: string;

  //
  //
  // Mapping
  traceMapOnFlag: boolean = false;
  traceMap: any;
  mapOptions : any;
  groupsForMap : Group[] = new Array<Group>();


  /**
   * Initialization of the component
   */
  ngOnInit() {

    console.log('[documents.component] <ngOnInit> <initializaing> ');
    
    // Get all teh docs
    this.getAllDocuments(false, 0);

    /**
     * Gallery
     */
    this.galleryOptions = [
      { width: '600px',
      height: '400px',
      thumbnailsColumns: 7,
      imageAnimation: NgxGalleryAnimation.Slide },
      { "breakpoint": 500, "width": "100%" },
      { "previewZoom": true, "previewRotate": true },

      , 

      /**
      {
          width: '600px',
          height: '400px',
          thumbnailsColumns: 4,
          imageAnimation: NgxGalleryAnimation.Slide
      },
      
      // max-width 800
      {
          breakpoint: 800,
          width: '100%',
          height: '600px',
          imagePercent: 80,
          thumbnailsPercent: 20,
          thumbnailsMargin: 20,
          thumbnailMargin: 20
      },
      // max-width 400
      {
          breakpoint: 400,
          preview: false
      }
       */
    ];

    this.galleryImages = [
      {
        small: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/1-small.jpeg',
        medium: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/1-medium.jpeg',
        big: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/1-big.jpeg'
      },
      {
        small: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/2-small.jpeg',
        medium: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/2-medium.jpeg',
        big: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/2-big.jpeg'
      },
      {
        small: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/3-small.jpeg',
        medium: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/3-medium.jpeg',
        big: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/3-big.jpeg'
      },
      {
        small: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/4-small.jpeg',
        medium: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/4-medium.jpeg',
        big: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/4-big.jpeg'
      },
      {
        small: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/5-small.jpeg',
        medium: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/5-medium.jpeg',
        big: 'https://lukasz-galka.github.io/ngx-gallery-demo/assets/img/5-big.jpeg'
      }   ,  
      
      {
        small: 'http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/pagethumbnail?doc_id=10056',
        medium: 'http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/page?doc_id=10056',
        big: 'http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/page?doc_id=10056'
      } 


      
    ];


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
          
    //
    // subscribe to messages from another component
    this._documentDetailTrigger.showDocumentDetailsMessage.subscribe(notificationDoc => {
        //this.notificationbasedDcumentSessionId = docSessionId;
        let _notificationDoc : Document = notificationDoc;
        console.log('[DocumentsComponent] <inter comm> <start> Just Got A notification ID '.concat(_notificationDoc.syncID));

        if(AppGlobals.EMITTER_SEED_VALUE.syncID == _notificationDoc.syncID){
          this.showDocuemntDetailsflag = false;
          console.log('[DocumentsComponent] <inter comm> <end> Just Got A notification ID '.concat(_notificationDoc.syncID));
          return;
        }
        //this.showDocuemntDetailsflag = true;
        

        // first check if the specific id exists in the current docs
        if(this.documents.find(doc => doc.syncID == _notificationDoc.syncID)){
          // carry on with current set of docs
          if(AppGlobals.EMITTER_SEED_VALUE.syncID != _notificationDoc.syncID) {
            console.log('[DocumentsComponent] <inter comm> <found> '.concat(_notificationDoc.syncID));
            this.showNotificationDetails(_notificationDoc.syncID, true);
          } else {
            this.showDocuemntDetailsflag = false;
          }
          //this.showDocuemntDetailsflag = false;
        }else{
          console.log('[DocumentsComponent] <inter comm> <NOT found> '.concat(_notificationDoc.syncID));
          // cannot find docs so need to refresh
          // this.refreshNotificationDocs(_notificationDoc.syncID);
          console.log('[DocumentsComponent] <inter comm> <refreshing> '.concat(_notificationDoc.syncID));
          this.refreshNotificationDocandShowDetails(_notificationDoc);
        }
        
    }); // end Subscription for notifications

      /**
       * Gallery Data
       */

    /**
    * Drag and Dop Dragula
    */
    this.dragula
    .drag
    .subscribe(value => {
      this.dragulaMessage = `Dragging the ${ value[1].innerText }!`;
    });

    this.dragula
      .drop
      .subscribe(value => {
      // recollate the pages
      this.recollatePages()


        this.dragulaMessage = `Dropped the ${ value[1].innerText }!`;

        setTimeout(() => {
          this.dragulaMessage = '';
        }, 1000);
      });

      this.dragula
      .remove
      .subscribe(value => {
        this.dragulaMessage = `Removed the ${ value[1].innerText }!`;
        this.recollatePages()
      }); 

      //this.initializeMap();
    } // ngOnInit

  private removeModel(args) {
      let [bagName, el, container] = args;
      // do something
  }

  ngOnChanges(){
    console.log('[Document Component] <ngOnChanges> ');
  }

  ngAfterViewInit() {
    //document.getElementsByClassName('tagPrefix')['0'].style.width = '175px'
    this.getAllDocumentTypes();
    this.getServerURI();
    //this.getAllUsers();
    this.getAllUserRecipients()
    this.getAllTags();
    this.stages = this.getAllStages();
    //this.getDocumentTraceById(42);
    this.getGroupsByOrganizationId(JSON.parse(localStorage.getItem('user')).userGroups[0].organizationId);

    this.filterDocDateFrom = DateUtils.getDateAsString(this.minDocDate);
    this.filterDocDateTo = DateUtils.getDateAsString(this.maxDocDate);
    
    //this.filterDocDateTo = this.getMaxDocsDate();
    //this.filterDocDateTo = "2045-06-06"; 
    
    
  }

  ngAfterContentInit() {
   // this.filterDocDateTo = "2045-06-06"; //this.getMaxDocsDate();
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
  getAllDocuments(flag : boolean, showDocId : number) {
    var docFeedFlag : boolean = true;
    var minDate : Date = null;
    var maxDate : Date  = minDate;

    this.showDocuemntDetailsflag = flag;

    this.startProgress();
    this._documentService.getAllDocuments().subscribe(
      data => { 
        this.completeProgress();
        this.users = new Array<User>();
        this.tempDocuments = data;
        this.documents = new Array<Document>();
        let currentUser = this.getCurrentUser();
        this.users.push(currentUser);
        let currentUserName = this.getCurrentUserName();
        console.log('[Document Component] GET ALL DOCUMENTS RESTFUL <data>'.concat(JSON.stringify(data)));
        console.log('[Document Component] GET ALL DOCUMENTS RESTFUL --> USername '.concat(JSON.stringify(this.username)));
        console.log('[Document Component] GET ALL DOCUMENTS RESTFUL --> User Role '.concat(JSON.stringify(this.currentUser.roles[0].value)));
            // set the organization names for all documents
            for(const doc of data){ 
              doc.partOfFeed = true;

              if(currentUser.roles[0].value == Role.ROLE_NAME_SUPER_ADMIN) {
                doc.partOfFeed = true;
              }else {
                //
                // if not my document
                if(!LocaleUtils.ciEquals(doc.owner, currentUserName)) {
                  //
                  // and there are recipients
                  if(doc.toRecipients.length > 0){
                    //
                    // if I am not a recioinent
                    if(!doc.toRecipients.find(recipient => recipient.id == this.currentUser.id)){
                      doc.partOfFeed = false;
                    }
                  } else {
                    //
                    // there are no recipients
                    doc.partOfFeed = false;
                  }
                }
              } // end if

              /*
              if(currentUser.roles[0].value == 'User'
                  || currentUser.roles[0].value == 'General'
                  || currentUser.roles[0].value == 'Shipping'
                  || currentUser.roles[0].value == 'Receiving'){
                    console.log('[Document Component] GET ALL DOCUMENTS RESTFUL --> NON-ADMIN '.concat(JSON.stringify(currentUserName)));
                  if((doc.status == 'DRAFT' ||  doc.status == 'REJECTED') && doc.owner != currentUserName){
                    
                    continue;
                  } // end if
                  if(doc.owner != currentUserName && doc.toRecipients.length > 0) {
                    if(!doc.toRecipients.find(recipient => recipient.id == this.currentUser.id)){
                      doc.partOfFeed = false;
                    }
                    
                  } // end if
              } // end if
              */
                               
                // group names
                doc.groupName = this._documentService.getDocumentGroupName(doc);
                // trace id for when the user is tracing the data, set to a default value
                doc.currentTraceId = '';
                doc.partOfTrace = true;
                doc.lotFound = true;
                console.log('GROUP NAME ADDED: '.concat(doc.groupName));

                // set the tracebility filter to off
                this.setTraceDocuments(false);
                //
                // figure out the dates
                var currDocDate = DateUtils.getDateFromString(doc.updationTimestamp);
                if(minDate == null ){
                  minDate = currDocDate;
                  maxDate = currDocDate;
                }
                if(minDate > currDocDate){
                  minDate = currDocDate;
                }
                if(maxDate < currDocDate){
                  maxDate = currDocDate;
                }

                // add the doc
                this.documents.push(doc);

                // add the owner to list of users to filter by
                if(doc.partOfFeed == true) {
                  if(!this.users.find(owner => LocaleUtils.ciEquals(doc.owner, owner.name))){
                    var newUser : User = new User();
                    newUser.name = doc.owner;
                    this.users.push(newUser);
                  }
                }
                
                
            } // end for
            //
            // Get the final sort dates
            //this.filterDocDateFrom = DateUtils.getDateAsString(minDate);
            //this.filterDocDateTo = DateUtils.getDateAsString(maxDate);

            this.minDocDate = minDate;
            this.maxDocDate = maxDate;
            this.filterDocDateFrom = DateUtils.getDateAsString(minDate);
            this.filterDocDateTo = DateUtils.getDateAsString(maxDate);


            if(showDocId > 0){
              //
              // set the current document
              let itemIndex = this.documents.findIndex(item => item.id == showDocId);
              this.currentDocument = this.documents[itemIndex];
            }
            
      },
      error => {
        console.log('Server Error');
        this.completeProgress();
      }
    );

    
    //this.filterDocDateTo = "2045-06-06"; 
    //this.filterDocDateTo = this.getMaxDocsDate();
    

  }

  refreshNotificationDocandShowDetails(doc: Document){
            // add to the component
            var newDocs = this.documents.slice(0);
            newDocs.push(doc);
            //component.documents = newDocs;
            this.currentDocument = doc;
            //component.resetFilter();
            this.getAllDocuments(true, doc.id)
  }
  
  /**
   * Refrsh the list of documents for a notification
   */

   refreshNotificationDocs(docSessionId : string){
    this.showDocuemntDetailsflag = false;
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
                  if(doc.status == 'DRAFT' && !LocaleUtils.ciEquals(doc.owner, this.username)){
                    continue;
                  }
                  if(!doc.toRecipients.find(recipient => recipient.id == this.currentUser.id) && !LocaleUtils.ciEquals(doc.owner, this.username)){
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
                // 
                // get the notification
                this.showNotificationDetails(docSessionId, true);
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
    if(this.currentDocument.toRecipients.length == 0) {
      return recList;
    }
    for (const recipient of this.currentDocument.toRecipients) {
      if(recipient == null) {
        continue;
      }
      recList = recList.concat(', ' + recipient.name)
    }
    if(recList.length > 0){
      recList = recList.substr(2);
    }
    return recList;
  }

  /**
   * Get all the users in this user's organization.
   */
  getAllUsers() {
    this._documentService.getAllUsers().subscribe(
      data => { 
        this.users = data;
        // this.currentDocumentRecipients = this.users;
      },
      error => console.log('Server Error'),
    );
  }

    /**
   * Get all the recipients that this user has access to
   */
  getAllUserRecipients() {
    this._documentService.getAllUserRecipients().subscribe(
      data => { 
        this.recipients = data;
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
        //this.documents = [];
        //this.documents.push(...this.tracedCurrDocument);
        //this.documents.push(...this.tracedCurrDocument);
            console.log('Current Document Trace <trace list size> ' + this.tracedCurrDocument.length);
            console.log('Current Document Trace <feed size>' + this.documents.length);
            console.log('Current Document Trace <trace list> '.concat(JSON.stringify(this.tracedCurrDocument)));
            console.log('Current Document Trace <feed>'.concat(JSON.stringify(this.documents)));
            
            // set the organization names for all documents
            this.buildTraceabilityMap();
            // set the trace filter on 
            this.setTraceDocuments(true);
            // sort the feeddocs by additional id

            console.log('All Stages '.concat(JSON.stringify(this.getAllStages())));
            console.log(JSON.stringify(this.tracedCurrDocument));

            // get the grid data
            this.getMaxTraceGridRows();
            for(var row=0 ; row < this.getMaxTraceGridRows(); row++){
              console.log("ROW ---> " + "[" + row + "] " + JSON.stringify(this.getGridRowNamesforRow(row)));
            }


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
  
        //
        // LInked Docs on Trace
        for (const linkedDoc of doc.linkedDocuments) {
          // place the linked doc into the map based on groupTypeName  
          let stageDocs: Document[];
          if (this.traceabilityMap.has(linkedDoc.groupTypeName)) {
            stageDocs = this.traceabilityMap.get(linkedDoc.groupTypeName);
          }else {
            stageDocs = new Array<Document>();
          }  
          let itemIndex = stageDocs.findIndex(item => item.id == linkedDoc.id);
          if(itemIndex != -1) {
            stageDocs.push(linkedDoc);
          }
          console.log('   Adding ---> ' + (linkedDoc.groupName));
          console.log('Linked Docs Array '.concat(JSON.stringify(stageDocs)));
          this.traceabilityMap.set(linkedDoc.groupTypeName, stageDocs);
        }
        //
        // Backup Docs in
        for (const attachedDoc of doc.attachedDocuments) {
          // place the linked doc into the map based on groupTypeName  
          let stageDocs: Document[];
          if (this.traceabilityMap.has(attachedDoc.groupTypeName)) {
            stageDocs = this.traceabilityMap.get(attachedDoc.groupTypeName);
          }else {
            stageDocs = new Array<Document>();
          }  
          let itemIndex = stageDocs.findIndex(item => item.id == attachedDoc.id);
          if(itemIndex != -1) {
            stageDocs.push(attachedDoc);
          }
          console.log('   Adding ---> ' + (attachedDoc.groupName));
          console.log('Backup Docs Docs Array '.concat(JSON.stringify(stageDocs)));
          this.traceabilityMap.set(attachedDoc.groupTypeName, stageDocs);
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
    this.docImportOn = false;
    this.docEditOn = false;
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
      this.currentDocument.updationTimestamp = this.getUpdationDate();
      this._documentService.setDocumentStatus(this.currentDocument).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
      );
    }
  }

    /**
   * Show the details of the document as well as trigger the 
   * @param id  - the document id
   */
  showNotificationDetails(sessionId: string, scrollFlag:boolean) {
    this.showDocuemntDetailsflag = true;
    for (const doc of this.documents) {
      if (doc.syncID == sessionId) {
        console.log('[DocumentsComponent] <show doc> <found> '.concat(sessionId));
        this.currentDocument = doc;
        console.log('[DocumentsComponent] <show doc> <changed curr doc> '.concat(this.currentDocument.syncID));
        const config: ScrollToConfigOptions = {
          target: "" + doc.id
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
      this.currentDocument.updationTimestamp = this.getUpdationDate();
      this._documentService.setDocumentStatus(this.currentDocument).subscribe(
        data =>  console.log('No issues'),
        error => console.log('Server Error'),
      );
    }
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
    if (item === this.getInternationalizedToken(this.FILTER_USER_ALL)) {
      this.documentFilter.owner = '';
    }else {
      this.documentFilter.owner = item;
    }
    this.showDocuemntDetailsflag = false;
    // log the data
    console.log ( 'Sort By User ' + this.filterSelectedUserChoice );

  }

    /**
   * Filter feed document data by user
   * @param item  - the user to filter by
   */
     filterByDateRangeTo(item: string) {
      this.filterDocDateTo = item;
      this.datesToFilter = DateUtils.getDatesBetween(
        DateUtils.getDateFromString(this.filterDocDateFrom), 
        DateUtils.getDateFromString(this.filterDocDateTo));

      // log the data
      console.log ( 'Sort By Date Range ' + this.datesToFilter );
  
    }

  /**
   * 
   * @param item - the document type to use when creating a new doc
   */
  setNewDocImportType(item){
    var docType : any;
    var reversedDocTypeName:string;
    var reversedNames: string[];

    this.newDocumentDynamicFieldDefinitions = new Array<DynamicFieldDefinition>();
    this.newDocumentDynamicFieldData = new Array<DynamicFieldData>();
    this.newDocumentDynamicFieldDataErrors = new Array<ApplicationErrorData>();

    console.log('PDF IMPORT - TYPE ' + item);
    //fetch the reverse value of the item; parsed out away from UI render and into the doc type
    // reversedDocTypeName = this._documentService.reverseInternationalizedNameStringToKeyString(item);
    reversedDocTypeName = LocaleUtils.fetchResourceKeyByValue(item);
    reversedNames = LocaleUtils.fetchAllResourceKeysByValue(item);
    console.log('PDF IMPORT - TYPE<reversed>' + reversedDocTypeName);
    console.log('PDF IMPORT - TYPE<reversedNames>' + JSON.stringify(reversedNames));

    this.documentDocDataPanelEnabledFlag = !(this.getInternationalizedToken(this.FILTER_IMPORT_DOCTYPE_ALL) == reversedDocTypeName);


    for(var row=0 ; row < this.currentUser.userGroups[0].allowedDocTypes.length; row++) {
      docType = this.currentUser.userGroups[0].allowedDocTypes[row];
      console.log('PDF IMPORT ---- TYPE<looking>' + JSON.stringify(docType.name));
      console.log('PDF IMPORT ---- TYPE<looking> <full>' + JSON.stringify(docType));
      
      
      if(reversedNames.length > 0){
        if (reversedNames.indexOf(docType.name) != -1){
          this.currectDocumentTypeForNewDoc = docType;
          //this.currectDocumentTypeForNewDocListValue = docType.value;
          console.log('PDF IMPORT ---- TYPE<found>' + JSON.stringify(docType));
          //
          // set the doc definitions for a new doc
          console.log('PDF IMPORT ---- getting doc definitions ' + JSON.stringify(docType.id));
          this.newDocumentDynamicFieldDefinitions = this._documentService.getDocDynamicDefinitionsByType(docType.id);
          this.newDocumentDynamicFieldData = this._documentService.getNewDocInfoDataByType(docType.id);
          this.newDocumentDynamicFieldDataErrors = this._documentService.getNewDocInfoDataErrors(docType.id);

          //
          // check for profile doc UI changes
          //
          if(this.currectDocumentTypeForNewDoc.documentDesignation == Document.TYPE_DESIGNATION_PROFILE) {
            this.newDocumentCreationLinkedDocsEnabledFlag = false;
            this.newDocumentCreationBackingDocsEnabledFlag = false;
          } else {
            this.newDocumentCreationLinkedDocsEnabledFlag = true;
            this.newDocumentCreationBackingDocsEnabledFlag = true;
          }
        }
      }

      /** 
      if( docType.name === reversedDocTypeName){
        this.currectDocumentTypeForNewDoc = docType;
        //this.currectDocumentTypeForNewDocListValue = docType.value;
        console.log('PDF IMPORT ---- TYPE<found>' + JSON.stringify(docType));
        //
        // set the doc definitions for a new doc
        console.log('PDF IMPORT ---- getting doc definitions ' + JSON.stringify(docType.id));
        this.newDocumentDynamicFieldDefinitions = this._documentService.getDocDynamicDefinitionsByType(docType.id);
        this.newDocumentDynamicFieldData = this._documentService.getNewDocInfoDataByType(docType.id);
        this.newDocumentDynamicFieldDataErrors = this._documentService.getNewDocInfoDataErrors(docType.id); 
      }
      */
    }

    console.log('PDF IMPORT ---- <doc definitions> ' + JSON.stringify(this.newDocumentDynamicFieldDefinitions));
    console.log('PDF IMPORT ---- <doc data> ' + JSON.stringify(this.newDocumentDynamicFieldData));
    console.log('PDF IMPORT ---- <doc error data> ' + JSON.stringify(this.newDocumentDynamicFieldDataErrors));
  }


  setEditDocImportType(item){
    var docType : any;
    var reversedDocTypeName:string;

    this.newDocumentDynamicFieldDefinitions = new Array<DynamicFieldDefinition>();
    this.newDocumentDynamicFieldData = new Array<DynamicFieldData>();
    this.newDocumentDynamicFieldDataErrors = new Array<ApplicationErrorData>();

    console.log('PDF IMPORT - TYPE ' + item);
    //fetch the reverse value of the item; parsed out away from UI render and into the doc type
    // reversedDocTypeName = this._documentService.reverseInternationalizedNameStringToKeyString(item);
    reversedDocTypeName = LocaleUtils.fetchResourceKeyByValue(item);
    console.log('PDF IMPORT - TYPE<reversed>' + reversedDocTypeName);


    for(var row=0 ; row < this.currentUser.userGroups[0].allowedDocTypes.length; row++) {
      docType = this.currentUser.userGroups[0].allowedDocTypes[row];
      console.log('PDF IMPORT ---- TYPE<looking>' + JSON.stringify(docType.name));
      console.log('PDF IMPORT ---- TYPE<looking> <full>' + JSON.stringify(docType));
      
      if( docType.name === reversedDocTypeName){
        this.currectDocumentTypeForNewDoc = docType;
        //this.currectDocumentTypeForNewDocListValue = docType.value;
        console.log('PDF IMPORT ---- TYPE<found>' + JSON.stringify(docType));
        //
        // set the doc definitions for a new doc
        console.log('PDF IMPORT ---- getting doc definitions ' + JSON.stringify(docType.id));
        this.newDocumentDynamicFieldDefinitions = this._documentService.getDocDynamicDefinitionsByType(docType.id);
        this.newDocumentDynamicFieldData = this._documentService.getNewDocInfoDataByType(docType.id);
        this.newDocumentDynamicFieldDataErrors = this._documentService.getNewDocInfoDataErrors(docType.id); 
        
      }
    }

    console.log('PDF IMPORT ---- <doc definitions> ' + JSON.stringify(this.newDocumentDynamicFieldDefinitions));
    console.log('PDF IMPORT ---- <doc data> ' + JSON.stringify(this.newDocumentDynamicFieldData));
    console.log('PDF IMPORT ---- <doc error data> ' + JSON.stringify(this.newDocumentDynamicFieldDataErrors));
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
    //item = this._documentService.reverseInternationalizeString(item);
    //item = this.getInternationalizedToken(item);
    if (item === this.getInternationalizedToken(this.FILTER_DOCTYPE_ALL)) {
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
  resetFilter(globalFlag : number) {
    this.filterSelectedUserChoice = this.getInternationalizedToken(this.FILTER_USER_ALL);
    this.filterSelectedSortChoice = this.getInternationalizedToken(this.SORT_DATE_ASCENDING);
    this.filterByDateSort(this.filterSelectedSortChoice);
    this.filterSelectedDocTypeChoice = this.getInternationalizedToken(this.FILTER_DOCTYPE_ALL);
    this.documentFilter.documentType = '';
    this.documentFilter.owner = '';
    this.documentFilter.partOfFeed = '';
    this.documentFilter.partOfTrace = true;
    this.documentFilter.lotFound = true;
    this.documentFilter.groupName  = {
      $or: this.getGroupFilterArray(''),
      };
    this.filterSelectedGroupName = '';
    // reset any doc side effects
    this.resetDocsforFilter();
    if(globalFlag === FilterFlags.RESET_FILTER_TAG_SEARCH){
      this.lotSearchOn = !this.lotSearchOn;
    }
    if(globalFlag === FilterFlags.RESET_FILTER_NEW_DOCUMENT){
      this.docImportOn = !this.docImportOn;
    }
    if(globalFlag === FilterFlags.RESET_FILTER_TRACE){
      this.documentFilter.partOfFeed = true;
    }
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
    if (tabName === 'fields') {
      return 'nav-link';
    }
    if (tabName === 'tags') {
      return 'nav-link';
    }
    if (tabName === 'notes') {
      return 'nav-link';
    }
    if (tabName === 'docpages') {
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
    this.resetFilter(FilterFlags.RESET_FILTER_GLOBAL);

    // event.stopPropagation();
    if (this.showTracebilityCard) {
      //
      // setup shadow document feed
      this.shadowFeedDocuments = JSON.parse(JSON.stringify(this.documents));

      this.getDocumentTraceById(id);
      
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

  formatDataInfoField(dataField : DynamicFieldData){
    var formatted = dataField.fieldDisplayNameValue + ": ";
    if(dataField.data != undefined) {
      formatted = formatted + dataField.data;
    }
    console.log("fromatted Doc Info Field value: " + formatted);
    return formatted;
  }

  toggleTracing() {
    this.showFilter = !this.showFilter;
    this.showTracebilityCard = !this.showTracebilityCard;
  }

  /**
   * Dismiss the Trace Panel and reset filters.
   */
  dismissTraceDocument(event: any) {
    this.traceabilityStageGridMapTemp = new Map<string, Group[]>();
    this.traceDocTriggerId = 0;
    this.showFilter = true;
    this.showTracebilityCard = false;
    this.resetFilter(FilterFlags.RESET_FILTER_TRACE);
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


  /**
   * Export the Document Trace as a PDF file
   * @param id - the id of the document chain being traced
   * @param event  - disregarded event
   */
  exportTraceDocument(id: number, event: any){
    console.log('A PDF Export for Doc ---> ' +id);
    this._documentService.downloadPDFTraceFile(id)
  }

    /**
   * Export the Document Trace GPS Data as CVS file
   * @param id - the id of the document chain being traced
   * @param event  - disregarded event
   */
  exportTraceGPSData(id: number, event: any){
    console.log('GPS (CVS) Export for Doc ---> ' +id);
    this._documentService.downloadCSVTraceGPSDataFile(id)
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

  splitGridIconName(str: string, index : number) {
    var result = '';
    var tempArray = str.split(' ', 2); 
    if(tempArray.length > index) {
      result = tempArray[index];
      if(result.length > 13){
        // add elipsis and remove the characters after 10
        result = result.substring(0,10).concat("...");
      }
    }
    return result;
  }

  getTraceColumnHeaderName(type : GroupType) {
    console.log('Header Data-Name Internationazlied ' + type.value + ' ' + this.getInternationalizedToken(type.value));
    return type.name;

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
        return this._documentService.getHeaderTraceabilityClassColor(lookupIndex).concat(' clickable icon-active; white-space: nowrap; width: 80px; overflow: hidden; text-overflow: ellipsis;');
      }else{
        return this._documentService.getHeaderTraceabilityClassColor(lookupIndex).concat(' clickable; white-space: nowrap; width: 80px; overflow: hidden; text-overflow: ellipsis;');
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

  getGridRowNamesforNextRowNoGaps(){
    let rowNames: Group[] = new Array<Group>();
    var foundRowStage: Boolean = false;
    //
    // for each stage
    for(let groupType of this.stages){
      //
      // For each row in this stage (i.e. for each row in this column)
      foundRowStage = false;
      for(let gridCell of this.traceabilityStageGridMap.get(groupType.name)){
        if (gridCell != null){
            if(this.checkIfTracedDocumentIsInGroup(gridCell)) {
              console.log('ADDING TRACE CELL ***** ' + JSON.stringify(gridCell));
              if(!this.traceabilityStageGridMapTemp.has(groupType.name)) {
                // add a new column array
                this.traceabilityStageGridMapTemp.set(groupType.name, new Array<Group>())
              }
              // add the new cell 
              if(this.traceabilityStageGridMapTemp.get(groupType.name).indexOf(gridCell) === -1){
                this.traceabilityStageGridMapTemp.get(groupType.name).push(gridCell); 
                rowNames.push(gridCell);
                foundRowStage = true;    
                break;
              }
            }
        }
      } // end for the set of cells in this column/stage
      if(foundRowStage == false){
        rowNames.push(null);
      }
    }
    // console.log('FLattened SINGLE ROW ***** ' + JSON.stringify(rowNames));
    return rowNames;
  }

  /**
   * 
   * @returns 
   */
  getAllGridRowNamesNoGaps() {
    //
    //
    this.traceabilityStageGridMapTemp = new Map<string, Group[]>();
    this.groupsForMap  = new Array<Group>();


    //
    //
    let allRows: GroupList[] = new Array<GroupList>();
    let maxRows: number = this.getMaxTraceGridRows();
    for(var row=0 ; row < maxRows; row++){
      let groups: Group[] = this.getGridRowNamesforNextRowNoGaps();
      this.groupsForMap = this.groupsForMap.concat(groups);
      // TODO
      let groupList: GroupList = new GroupList();
      groupList.subGroups = groups;
      allRows.push(groupList);
    }
    // console.log('FLattened Rows ***** ' + JSON.stringify(allRows));
    //this.groupsForMap = ArrayUtils.removeDuplicates(this.groupsForMap);

    return allRows;
  }

  /**
   * 
   * @returns 
   */
  getAllGridRowNamesOrgsForMap() {
    //
    //
    let allOrgsToMap : Group[] = new Array<Group>();
    let maxRows: number = this.getMaxTraceGridRows();
    for(var row=0 ; row < maxRows; row++){
      let groups: Group[] = this.getGridRowNamesforNextRowNoGaps();
      allOrgsToMap = allOrgsToMap.concat(groups);
    }

    var unique = allOrgsToMap.filter(function(elem, index, self) {
      return index === self.indexOf(elem);
    })
    return unique;
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
   * Get the highlight color for the feed docuemnt element
   * @param id - the document id to highlight
   */
  getBackgroundColorHighlight(id: number) {
    if (this.currentDocument == null) {
      return '#fff';
    }
    if (id === this.currentDocument.id && this.showDocuemntDetailsflag == true) {
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
          //
          // Check mostly documents available to the current user
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
            //
            // Check for the backup docs in this document <TODO> Fix it later
            if(doc.id > 0 
                && doc.partOfTrace
                && doc.owner != null){
                  for (const backupDoc of doc.attachedDocuments){
                      if(docType.name == backupDoc.type.name
                          && doc.groupName == gridCell.name){
                            uniqueDocs.set(docType.name, 1);
                            console.log('[Count Doc Types] <matched backup doc> --->' + ' [' + gridCell.name + '] ', JSON.stringify(backupDoc.id), backupDoc.type.value, backupDoc.owner, backupDoc.status);
                            console.log('[Count Doc Types] <matching backup doc> --->', backupDoc.type.value, backupDoc.owner, backupDoc.status);
                          }
                  }
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
          console.log("  <setTraceDocuments> Doc being looked for " + JSON.stringify(doc));
          if (this.tracedCurrDocument.find(tracedDoc => tracedDoc.id == doc.id)) {
            doc.partOfTrace = true;
            doc.lotFound = true;
            console.log("  <setTraceDocuments> FOUND ");
          }else {
            doc.partOfTrace = false;
            console.log("  <setTraceDocuments> *NOT* FOUND");
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

  executeDocInfoSearch(){
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
      if(this.isDocInfoInPresentInDocument(doc)){
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

  isDocInfoInPresentInDocument(doc: Document){

    for (const docInfo of doc.dynamicFieldData) {
      var currentDocInfoData: string = docInfo.data;
      console.log('Current Doc Info PRE' + currentDocInfoData);
      currentDocInfoData = currentDocInfoData.toUpperCase().trim();

      console.log('Current Doc Info POST' + currentDocInfoData);

      if(!this.isPartialMatch){
        if (this.tagInputSearch001 != '' && currentDocInfoData === this.tagInputSearch001.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch002 != '' && currentDocInfoData === this.tagInputSearch002.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch003 != '' && currentDocInfoData === this.tagInputSearch003.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch004 != '' && currentDocInfoData === this.tagInputSearch004.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch005 != '' && currentDocInfoData === this.tagInputSearch005.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch006 != '' && currentDocInfoData === this.tagInputSearch006.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch007 != '' && currentDocInfoData === this.tagInputSearch007.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch008 != '' && currentDocInfoData === this.tagInputSearch008.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch009 != '' && currentDocInfoData === this.tagInputSearch009.toUpperCase()) {
          return true;
        }
        if (this.tagInputSearch010 != '' && currentDocInfoData === this.tagInputSearch010.toUpperCase()) {
          return true;
        }
      }else{
        if (this.tagInputSearch001 != '' && currentDocInfoData.includes(this.tagInputSearch001.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch002 != '' && currentDocInfoData.includes(this.tagInputSearch002.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch003 != '' && currentDocInfoData.includes(this.tagInputSearch003.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch004 != '' && currentDocInfoData.includes(this.tagInputSearch004.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch005 != '' && currentDocInfoData.includes(this.tagInputSearch005.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch006 != '' && currentDocInfoData.includes(this.tagInputSearch006.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch007 != '' && currentDocInfoData.includes(this.tagInputSearch007.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch008 != '' && currentDocInfoData.includes(this.tagInputSearch008.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch009 != '' && currentDocInfoData.includes(this.tagInputSearch009.toUpperCase())) {
          return true;
        }
        if (this.tagInputSearch010 != '' && currentDocInfoData.includes(this.tagInputSearch010.toUpperCase())) {
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

  getCanSpecialRejectDoc(){
    if(this.currentDocument == null || this.isDocumentOwner()){
      return false;
    }
    if(this.currentDocument.status == 'ACCEPTED' && this.currentDocument.isLocked == false){
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

  /**
   * Check oif the current document can be edited
   */
  isCurrentDocEditable(){
    if(this.currentDocument == null){
      return false;
    }
    if((this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED')
        && this.docEditOn == true){
      if(this.isDocumentOwner()){
        if(!this.currentDocument.isLocked){
          return true;
        }
      }
    }
    return false;
  }

  canEditDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if((this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED')){
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

  getCanSaveDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if((this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED')
        && this.docEditOn){
          if(this.isDocumentOwner()){
            if(!this.currentDocument.isLocked){
              return true;
            }
        }
    }
    return false;
  }

  getCanCancelEditDoc(){
    if(this.currentDocument == null){
      return false;
    }
    if((this.currentDocument.status == 'DRAFT' || this.currentDocument.status == 'REJECTED')
        && this.docEditOn){
          if(this.isDocumentOwner()){
            if(!this.currentDocument.isLocked){
              return true;
            }
        }
    }
    return false;
  }

  acceptDocument(){
      this.currentDocument.status = 'ACCEPTED';
      this.currentDocument.updationTimestamp = this.getUpdationDate();
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
    if(this.docEditOn){
      this.onMultiPDFFileUpload('SUBMITTED');
    }else{
      this.currentDocument.updationTimestamp = this.getUpdationDate();
      this._documentService.setDocumentStatus(this.currentDocument).subscribe(
        data => {
          // set the updation server time
          this.currentDocument.updationServerTimestamp = data;
          console.log('No issues')
        },
        error => console.log('Server Error'),
      );
    }
    
  }

  rejectDocument(valueHeader, valueText){
        this.currentDocument.status = 'REJECTED';

        // set the data in the document
        var note:NoteData = new NoteData();
        note.note = valueHeader + AppGlobals.FORMATTING_DELIMITER + valueText;
        note.owner = JSON.parse(localStorage.getItem('user')).name;
        this.currentDocument.notes.push(note);
        this.currentDocument.updationTimestamp = this.getUpdationDate();
        this._documentService.setDocumentStatus(this.currentDocument).subscribe(
          data => { 
            // set the updation server time
            this.currentDocument.updationServerTimestamp = data;
            console.log('No issues')
        },
          error => console.log('Server Error'),
        );

        this._documentService.AddDocumentNote(note, this.currentDocument).subscribe(
          data =>  console.log('No issues'),
          error => console.log('Server Error'),
        );

  }

  isDocumentOwner(){
    if(localStorage.getItem('user') == null) {
      return false
    }
    if(LocaleUtils.ciEquals(this.currentDocument.owner, JSON.parse(localStorage.getItem('user')).name )){
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
      //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
      !(this.getInternationalizedToken(this.FILTER_IMPORT_DOCTYPE_ALL) == this.currectDocumentTypeForNewDocListValue)
      && this.currentDocumentFileList !== undefined
      && this.currentDocumentFileList !== null
      && this.currentDocumentFileList.length > 0;


    if(name == 'Browse'){
      // is the doc type chosen?

      if(isEnabled){
        return 'btn btn-warning btn-xs active';
      }else{
        return 'btn btn-warning btn-xs disabled';
      }
    }

    if(name == 'File Delete'){
      // is the doc type chosen?
      return 'btn btn-danger btn-xs active';
      //if(isEnabled){
      //  return 'btn btn-danger btn-xs active';
      //}else{
      //  return 'btn btn-danger btn-xs disabled';
      //}
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
          creationDate: this.getCreationDate(),
          updationDate: this.getUpdationDate(),
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
    if(!this.docEditOn){
      var currDate =  new Date().toISOString(); 
      var currDateArray = currDate.split('T');
      return currDateArray[0] + ' ' + currDateArray[1].substring(0,8);
    }else{
      return this.currentDocument.creationTimestamp;
    }
  }

  getCreationDate() {
    if(!this.docEditOn){
      //var currDate =  new Date().toISOString(); 
      //var currDateArray = currDate.split('T');
      //return currDateArray[0] + ' ' + currDateArray[1].substring(0,8);
      return DateUtils.getCurrentDateTime();
    }else{
      return this.currentDocument.creationTimestamp;
    }
  }

  getUpdationDate() {
    //var currDate =  new Date().toISOString(); 
    //var currDateArray = currDate.split('T');
    //return currDateArray[0] + ' ' + currDateArray[1].substring(0,8);
    return DateUtils.getCurrentDateTime();
  }

  getImportDocTypeName(){
    if(this.docEditOn){
      return this.currentDocument.type.value;
    }else{
      if(this.currectDocumentTypeForNewDoc == null){
        return 'Fishmeal Lot Traceability';
      }else{
        return this.currectDocumentTypeForNewDoc.value;
      }
    }
  }

  getImportDocTypeId(){
    if(this.docEditOn){
      return this.currentDocument.type.id;
    }else{
      if(this.currectDocumentTypeForNewDoc == null){
        return 0;
      }else{
        return this.currectDocumentTypeForNewDoc.id;
      }
    }
  }

  getImportDocTypeHexColorCode(){
    if(this.docEditOn){
      return this.currentDocument.type.hexColorCode;
    }else{
      if(this.currectDocumentTypeForNewDoc == null){
        return '#ffe250fb';
      }else{
        return this.currectDocumentTypeForNewDoc.hexColorCode;
      }
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
    //for (const recipient of this.currentDocumentRecipients) {
    //  result += ',' + recipient.id;
    //}
    if(this.currentDocumentRecipient.id >= 0) {
      result = '' + this.currentDocumentRecipient.id
    }
    //if(result != null){
    //  result = result.substr(1);
    //}
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
      result += AppGlobals.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLIT_STRING + tag.id;
    }
    if(result != null){
      result = result.substr(AppGlobals.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLIT_STRING.length);
    }
    console.log('getImportDocTagsCreate ---> : '.concat(JSON.stringify(result)));
    return result;
  }

  getDocDynamicInfoData(){
    var result:string = '';

    let i: number = 0;
    for (let docInfo of this.newDocumentDynamicFieldDefinitions) {
      console.log('Doc Info Data Elements ' + JSON.stringify(docInfo) + ' ' + this.newDocumentDynamicFieldData[i]);
      // create the string in the format of {doc info type id} delimter {data}
      result += AppGlobals.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLIT_STRING + docInfo.id 
              + AppGlobals.DOC_INFO_DATA_FORMATTING_DELIMITER
              + this.newDocumentDynamicFieldData[i].data;
          i++;
    }
    if(result != null){
      result = result.substr(AppGlobals.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLIT_STRING.length);
    }

    console.log('getDocDynamicInfoData ---> : '.concat(JSON.stringify(result)));
    return result;
    
  }

  getDocEditPageData(){
    var result:string = '';

    let i: number = 0;
    for (let page of this.currentDocument.pages) {
      console.log('Doc Pages ' + JSON.stringify(page));
      // create the string in the format of {doc info type id} delimter {data}
      result += ',' + page.id 
          i++;
    }
    if(result != null){
      result = result.substr(1);
    }

    console.log('getDocEditPageData ---> : '.concat(JSON.stringify(result)));
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
        if(doc.toRecipients != null && doc.toRecipients.length > 0) {
          if(doc.toRecipients.find(recipient => recipient.id == this.currentUser.id) && doc.owner != this.username){
            result.push(doc);
          }
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

  getTagListForDoc(doc: Document){

    return "";

  }

  /**
   * Get the min Data Info Field formated
   * @param doc - the document for which we get the data
   */
  getDataInfoList(doc: Document){
    var result : string = '';
    if(doc.dynamicFieldData.length > 0){
      result = doc.dynamicFieldData[0].fieldDisplayNameValue + ': ' + doc.dynamicFieldData[0].data
    }
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
      
      if(LocaleUtils.ciEquals(doc.owner, this.currentUser.name)){
        result.push(doc);
      }
    }
    return result;
  }

  getProfileDocs(){
    var result = new Array<Document>();
    if(this.documents == null){
      return result;
    }
    for(const doc of this.documents){
      
      if(LocaleUtils.ciEquals(doc.owner, this.currentUser.name)
          && doc.type.documentDesignation === Document.TYPE_DESIGNATION_PROFILE){
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

    //
    // adding to the file list
    this.selectedUploadPDFFiles.push(event.target.files[0]);
    this.currentDocumentFileList.push(event.target.files[0]);
    (<HTMLInputElement>document.getElementById("input-file-now")).value = "";
    console.log("PDF FIle Upload LIst" + JSON.stringify(this.selectedUploadPDFFiles));
    console.log(event);
  }

  /**
   * Load multiple PDF files to the backend
   * @param docStatus  - the status of the docuemnt to attach when uploading it
   */
  onMultiPDFFileUpload(docStatus){
    var isEnabled = 
    //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type');
    !(this.getInternationalizedToken(this.FILTER_IMPORT_DOCTYPE_ALL) == this.currectDocumentTypeForNewDocListValue)

    if(isEnabled && this.newDocumentCreationFlag == true && this.validateNewDocumentSubmission() == true){
        //
        // start the transaction
        var fd = new FormData();

        //
        // Add the files first 
        for(const upFile of this.currentDocumentFileList) {
          fd.append('files', upFile);
        }
        // fd.append('file', this.selectedPDFFileToUpload);

        //
        // add the rest of teh data
        fd.append('userName', this.username);
        if(this.docEditOn){
          fd.append('docId', ''+this.currentDocument.id);
        }
        fd.append('creationDate', this.getCreationDate());
        fd.append('updationDate', this.getUpdationDate());
        fd.append('docTypeName', this.getImportDocTypeName());
        fd.append('docTypeId', ''+this.getImportDocTypeId());
        fd.append('docTypeHexColorCode', this.getImportDocTypeHexColorCode());
        fd.append('docRecipients', this.getRecipientsToCreate());
        fd.append('docLinkedDocs', this.getLinkedDocsToCreate());
        fd.append('docBackingDocs', this.getBackingDocsToCreate());
        fd.append('docImportDocTags', this.getImportDocTagsCreate());
        fd.append('docInfoDynamicData', this.getDocDynamicInfoData());
        if(this.docEditOn){
          fd.append('docExistingPageData', this.getDocEditPageData());
        }

        fd.append('docImportDocStatus', docStatus);
        
        console.log("PDF FIle Upload FROM DATA <status> ---> " + docStatus);

        console.log("PDF FIle Upload FROM DATA---> " + JSON.stringify(fd));
        this.startProgress();
          this._documentService.startUploadProcess();
          if(this.docEditOn){
            this._documentService.onFileUpdateUpload(this.selectedPDFFileToUpload, fd, this.slimLoader, this._documentService, this);
          }else{
            this._documentService.onFileUpload(this.selectedPDFFileToUpload, fd, this.slimLoader, this._documentService, this);
          }
    }
  }

  onPDFFileUpload(docStatus){

    var isEnabled = 
    //!(this._documentService.reverseInternationalizedNameStringToKeyString(this.currectDocumentTypeForNewDocListValue) == 'document_import_choose_doc_type')
    !(this.getInternationalizedToken(this.FILTER_IMPORT_DOCTYPE_ALL) == this.currectDocumentTypeForNewDocListValue)
    && this.selectedPDFFileToUpload !== undefined
    && this.selectedPDFFileToUpload !== null;

    if(isEnabled){
        var fd = new FormData();
        fd.append('file', this.selectedPDFFileToUpload);
        fd.append('userName', this.username);
        fd.append('creationDate', this.getCreationDate());
        fd.append('updationDate', this.getUpdationDate());
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
    this.selectedUploadPDFFiles = new Array<File>();

  }

  onPDFFileListRemoveChoices(){
    (<HTMLInputElement>document.getElementById("input-file-now")).value = "";
    this.selectedPDFFileToUpload = null;
    for(const fileName of this.currentDocumentFileList){
      console.log("PDF FIle Upload shadow:List" + JSON.stringify(fileName));
      var index = 0
      //for(const pdfFile of this.selectedUploadPDFFiles){
      //  if(fileName === pdfFile.name) {
      //    this.selectedUploadPDFFiles.splice(index, 1);
      //    break;
      //  }else {
      //    index++;
      //  }
      //}
    }
  }

  getAllSelectedPDFFiles() {
    return this.selectedUploadPDFFiles;
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

  processCancelDocEdit(){
    this.currentDocument.pages = JSON.parse(JSON.stringify(this.currentRevertDocumentPages));
    this.toggleEditDocButton();
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

  getAttachedDocumentPages(parentDocId : number, attachedDocId : number){
    var document = this.getDocumentByDocId(parentDocId);
    if(document){
      for (const doc of document.attachedDocuments) {
        if (doc.id === attachedDocId) {
          return doc.pages;;
        }
      }
      return [];
      
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

  getAttachmentDocumentPagesLength(doc : Document){
    if(doc){
      return doc.pages.length;
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
    var backupDocHackFlag: boolean = false;

    console.log("Doc Feed Mini-Card <before> --> " +  JSON.stringify(document));
    console.log("Doc Feed Mini-Card <before> --> " + document.owner + ": " + document.type.documentDesignation + ": " + document.groupName + ": " + document.type.value + ": " + document.status);
    
    //
    // search the array of main docs
    foundDocs = this.documents.filter(function(value, index, arr){
      return (document.groupName == value.groupName
        && value.type.value == document.type.value
        && value.id > 0
        && value.partOfTrace);
    });

    if(foundDocs.length > 0){
      foundDoc = foundDocs[0];
      console.log("Doc Feed Mini-Card <found> --> " + foundDoc.type.documentDesignation + ": " + foundDoc.groupName + ": " + foundDoc.type.value + ": " + foundDoc.status);
    }
    
    //
    // If not found search the array of profile docs
    if(foundDocs.length == 0){
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
      }
    }

    if(foundDocs.length > 0){
        foundDoc = foundDocs[0];
        console.log("Doc Feed Mini-Card <found PROFILE> --> " + foundDoc.type.documentDesignation + ": " + foundDoc.groupName + ": " + foundDoc.type.value + ": " + foundDoc.status);
    }
      
    //
    // If not found search the backup docs
    if(foundDocs.length == 0){
      //
      // <TODO> Temp fix for backup docs being part of the search
      foundDocs = this.documents.filter(function(value, index, arr){
        return (document.groupName == value.groupName
          && value.id > 0
          && value.partOfTrace);
      });
      if(foundDocs.length > 0){
        //
        // Go through all found docs to see if there is any in the backup docs that has the specific doc
        for (const tempDoc of foundDocs){
          for (const backupDoc of tempDoc.attachedDocuments){
              if(document.type.name == backupDoc.type.name){
                foundDoc = backupDoc;
                backupDocHackFlag = true;
              }
          }
        }
      }
    } 

    //
    // If not found then we give up
    if(foundDocs.length == 0){
      foundDoc = null;
    }

      // if we have found a document 
    if(foundDoc != null){
      // gets its status
      if(foundDoc.status == Document.STATUS_ACCEPTED || backupDocHackFlag == true){
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

  NotificationToaster(){
    this.toasterService.Error("Error Clicked");
  }



  getCurrentTraceDocumentId(){
    console.log('Trace Document ID '.concat('' + this.tracedCurrDocument[0].id));
    return this.tracedCurrDocument[0].id
  }

  getCurrentDocuments(){
    return this.documents;
  }

  trackDocBySessionID(index : number, item: any): string {
    return item.syncID;
  }



  isDocInfoRequired(definition: DynamicFieldDefinition){

    console.log('Doc Data Info for <docdata> this docuemnt ' + JSON.stringify(this.newDocumentDynamicFieldData));

    if(definition.isRequired == true){
      return 'Required';
    }else{
      return 'Optional';
    }
  }

  getDateFilterRangeStyle() {
    if(!this.isFilterDateRangeOn) {
      return {"pointer-events": "none", "opacity": "0.4"};
    } else {
      return "";
    }
  }

  getInputTypeClass(definition: DynamicFieldDefinition){
    if(definition.fieldTypeId === DynamicFieldType.NUMERIC_TYPE){
      return "number";
    }else 
    if(definition.fieldTypeId === DynamicFieldType.ALPHANUMERIC_TYPE){
      return "text";
    }else 
    if(definition.fieldTypeId === DynamicFieldType.DATE_TYPE){
      return "date";
    }
    if(definition.fieldTypeId === DynamicFieldType.EXPIRY_DATE_TYPE){
      return "date";
    }
  }

  getMaxInputLengthClass(definition: DynamicFieldDefinition){
    return definition.maxLength;
  }

  getDocDataFieldCurrentLength(index : number){
    if(this.newDocumentDynamicFieldData[index].data == null){
      return 0;
    }
    return this.newDocumentDynamicFieldData[index].data.length
  }

  getCharacterInputLentghFeedback(definition: DynamicFieldDefinition, index : number){
    if(definition.fieldTypeId === DynamicFieldType.DATE_TYPE){
      return "";
    }
    if(this.newDocumentDynamicFieldData[index].data == null){
      return "0/" + this.getMaxInputLengthClass(definition);
    }else{
      return this.newDocumentDynamicFieldData[index].data.length + "/" + this.getMaxInputLengthClass(definition);
    }
  }

  validateNewDocumentSubmission(){
    var isValid: boolean = true;

    //
    // Check if required documents are submitted
    let i: number = 0;
    for (let docInfo of this.newDocumentDynamicFieldDefinitions) {
      console.log('Doc Info Data Elements ' + JSON.stringify(docInfo) + ' ' + this.newDocumentDynamicFieldData[i]);
      // create the string in the format of {doc info type id} delimter {data}
      if(docInfo.isRequired && (this.newDocumentDynamicFieldData[i].data == null || this.newDocumentDynamicFieldData[i].data.length == 0)){
        isValid = false;
        var errorData : ApplicationErrorData = new ApplicationErrorData();
        errorData.isError = true;
        errorData.message = ApplicationErrorData.REQUIRED_FIELD_MESSAGE;
        this.newDocumentDynamicFieldDataErrors[i] = errorData;
      }
      i++;
    }

    //
    // if not valid reject
    if(!isValid){
      this.newDocumentValidationErrorToaster()
    }

    return isValid;

  }

  newDocumentValidationErrorToaster(){
    this.toasterService.Error("Please fix input errors before submitting the document.");
  }

  getDocInfoItemClassValue(index :number){
    if(this.newDocumentDynamicFieldDataErrors[index] == null){
      return "form-group row";
    }

    if(!this.newDocumentDynamicFieldDataErrors[index].isError){
      return "form-group row";
    }else{
      return "form-group row has-error";
    }
  }

  isDocFieldValid(index : number){
    if(this.newDocumentDynamicFieldDataErrors[index] == null){
      return true;
    }
    return this.newDocumentDynamicFieldDataErrors[index].isError;
  }

  getDocFieldErrorMessage(index : number){
    if(this.newDocumentDynamicFieldDataErrors[index] == null){
      return "";
    }
    return this.newDocumentDynamicFieldDataErrors[index].message
  }

  /**
   * 
   * @param doc - the document from which the page is being deleted
   * @param page  - the specific page being deleted
   */
  deletePage(doc: Document, page : Page){
    this._documentService.deleteDocPage(doc, page.id).subscribe(
      data =>  {
        console.log('No issues [DELETE PAGE');
      },
      error => console.log('Server Error'),
    );
    const index = doc.pages.indexOf(page);
    if (index > -1) {
      doc.pages.splice(index, 1);
    }

    //this.toasterService.Error("Page <clicked> " + page.pageNumber);
  }

  getFilterFlag_RESET_FILTER_GLOBAL(){
    return FilterFlags.RESET_FILTER_GLOBAL
  }

  getFilterFlag_RESET_FILTER_NEW_DOCUMENT(){
    return FilterFlags.RESET_FILTER_NEW_DOCUMENT
  }
  
  getFilterFlag_RESET_FILTER_EDIT_DOCUMENT(){
    return FilterFlags.RESET_FILTER_EDIT_DOCUMENT
  }

  getFilterFlag_RESET_FILTER_TRACE(){
    return FilterFlags.RESET_FILTER_TRACE
  }

  getFilterFlag_RESET_FILTER_TAG_SEARCH(){
    return FilterFlags.RESET_FILTER_TAG_SEARCH
  }

  toggleCreateNewDocButton(){
    this.docImportOn = !this.docImportOn;
    // reset the import page data
    this.currentDocumentRecipients = new Array<User>();
    this.currentDocumentRecipient = new User();
    this.currentDocumentLinks = new Array<Document>();
    this.currentDocumentBacking = this.getProfileDocs(); //new Array<Document>();
    this.currentDocumenTags = new Array<DocumentTag>();
    // this.currectDocumentTypeForNewDocListValue = "-- Choose Doc Type";
    this.currectDocumentTypeForNewDocListValue = this.getInternationalizedToken(this.FILTER_IMPORT_DOCTYPE_ALL);
    // reset field data
    this.newDocumentDynamicFieldDefinitions = new Array<DynamicFieldDefinition>();
    this.newDocumentDynamicFieldData = new Array<DynamicFieldData>();
    // reset file data
    this.selectedUploadPDFFiles = new Array<File>();
    //
    // disable doc highlight
    this.showDocuemntDetailsflag = false;
    this.currentDocumentToShow = new Document();

    //
    // disable the other buttons
    this.documentDocDataPanelEnabledFlag = false;
  }

  toggleEditDocButton(){
    this.docEditOn = !this.docEditOn;

    if(!this.docEditOn){
        // reset the import page data
        this.currentDocumentRecipients = new Array<User>();
        this.currentDocumentRecipient = new User();
        this.currentDocumentLinks = new Array<Document>();
        this.currentDocumentBacking = this.getProfileDocs(); //new Array<Document>();
        this.currentDocumenTags = new Array<DocumentTag>();
        this.currectDocumentTypeForNewDocListValue = "-- Choose Doc Type";
        this.documentEditionLinkedDocsEnabledFlag = true;
        this.documentEditionBackingDocsEnabledFlag = true;
        // reset field data
        this.newDocumentDynamicFieldDefinitions = new Array<DynamicFieldDefinition>();
        this.newDocumentDynamicFieldData = new Array<DynamicFieldData>();
        // reset file data
        this.selectedUploadPDFFiles = new Array<File>();
        // this.currentDocument.pages = [...this.currentRevertDocumentPages];
        //this.currentDocument.pages = JSON.parse(JSON.stringify(this.currentRevertDocumentPages));
        this.showDocDetailsPagePreview = false;
        this.currentDocumentToShow = new Document();
    }else{
        // reset the import page data
        this.currentDocumentRecipients = this.getAllSelectedDocEditRecipients();
        this.currentDocumentRecipient = this.getSelectedDocEditRecipient();
        this.currentDocumentLinks = this.getAllSelectedDocEditLinkedDocs();
        this.currentDocumentBacking = this.getAllSelectedDocEditProfileDocs();
        this.currentDocumenTags = this.getAllSelectedDocEditTags();
        this.currectDocumentTypeForNewDocListValue = this.getInternationalizedToken(this.currentDocument.type.name);
        this.documentDocDataPanelEnabledFlag = true;
        if(this.currentDocument.type.documentDesignation == Document.TYPE_DESIGNATION_PROFILE) {
          this.documentEditionLinkedDocsEnabledFlag = false;
          this.documentEditionBackingDocsEnabledFlag = false;
        }
        // reset field data
        this.newDocumentDynamicFieldDefinitions = this._documentService.getDocDynamicDefinitionsByType(this.currentDocument.type.id);
        this.newDocumentDynamicFieldDataErrors = this._documentService.getNewDocInfoDataErrors(this.currentDocument.type.id); 
        this.newDocumentDynamicFieldData = this._documentService.getNewDocInfoDataByType(this.currentDocument.type.id);
        // set the data
        
        for (let docInfo of this.currentDocument.dynamicFieldData) {
          let i: number = 0;
          for (let docInfoTemp of this.newDocumentDynamicFieldData) {
              if(docInfo.fieldDisplayNameValue == docInfoTemp.fieldDisplayNameValue){
                // set it
                this.newDocumentDynamicFieldData[i].data = docInfo.data;
                this.newDocumentDynamicFieldData[i].id = docInfo.id;
              }
              i++;
            }
        }
        // reset file data 
        this.selectedUploadPDFFiles = new Array<File>();
        // set page data
        //this.currentRevertDocumentPages = [...this.currentDocument.pages];
        this.currentRevertDocumentPages = JSON.parse(JSON.stringify(this.currentDocument.pages));
        this.showDocDetailsPagePreview = true;
    }
    

    //
    // disable the other buttons
  }

  toggleLotSearchButton(){
    this.lotSearchOn = !this.lotSearchOn;
    // reset the import page data
    this.resetDocsforFilter();
    //
    // disable doc highlight
    this.showDocuemntDetailsflag == false;
  }

  isCreateNewDocDisabled(){
    if(this.docEditOn == true || this.lotSearchOn == true){
      return true;
    }else{
      return false;
    }
  }

  isEditDocDisabled(){
    if(this.docImportOn == true || this.lotSearchOn == true || this.showDocuemntDetailsflag == false || this.currentDocument.isLocked){
      return true;
    }else{
      return false;
    }
  }

  isLotSearchDisabled(){
    if(this.docImportOn == true || this.docEditOn == true){
      return true;
    }else{
      return false;
    }
  }

  getDocumentDocInfoItemId(doc: Document){
    if(doc.dynamicFieldData.length > 0){
      return ' - ' + doc.dynamicFieldData[0].data;
    }else{
      return "";
    }
  }

  /******************************************
   * Edit Doc Selections and Data
   */


  getAllRecipients(){
    var result = new Array<User>();
    if(this.users == null){
      return result;
    }
    for(const user of this.users){
      if(user.name != this.currentUser.name){
            result.push(user);
      }
    }
    return result;
  }

  getAllRecipientsForCurrentUser(){
    var result = new Array<User>();
    if(this.recipients == null){
      return result;
    }
    for(const user of this.recipients){
      if(user.name != this.currentUser.name){
            result.push(user);
      }
    }
    return result;
  }

  getAllRecipientsForDocEdit(){
    var result = new Array<User>();
    if(this.users == null){
      return result;
    }
    if(this.currentDocument.id > 0){
      return this.currentDocument.toRecipients;
    }
    for(const user of this.users){
      if(user.name != this.currentUser.name){
            result.push(user);
      }
    }
    return result;
  }

  getAllSelectedDocEditRecipients(){
    var result = new Array<User>();
    if(this.currentDocument.toRecipients == null || this.currentDocument.toRecipients.length ==0){
      return result;
    }
    for(const recipient of this.currentDocument.toRecipients){
      for(const user of this.users){
        if(user.name == recipient.name){
              result.push(user);
        }
      }
    }
    return result;
  }

  getSelectedDocEditRecipient(){
    var result = new User();
    if(this.currentDocument.toRecipients == null || this.currentDocument.toRecipients.length ==0){
      return result;
    } else {
      return this.currentDocument.toRecipients[0];
    }
  }

  getAllDocEditLinkedDocs(){
    return this.getDocsToLink();
  }

  getAllSelectedDocEditLinkedDocs(){
    var result = new Array<Document>();
    if(this.currentDocument.linkedDocuments == null || this.currentDocument.linkedDocuments.length == 0){
      return result;
    }
    for(const doc of this.currentDocument.linkedDocuments){
      for(const linkedDoc of this.documents){
        if(doc.id == linkedDoc.id){
              result.push(linkedDoc);
        }
      }
    }
    return result;
  }

  getAllDocEditProfileDocs(){
    return this.getDocsToBackup();
  }

  getAllSelectedDocEditProfileDocs(){
    var result = new Array<Document>();
    if(this.currentDocument.attachedDocuments == null || this.currentDocument.attachedDocuments.length == 0){
      return result;
    }
    for(const doc of this.currentDocument.attachedDocuments){
      for(const profileDoc of this.documents){
        if(doc.id == profileDoc.id){
              result.push(profileDoc);
        }
      }
    }
    return result;
  }


  getAllDocEditTags(){
    return this.getTagsToLink();
  }

  getAllSelectedDocEditTags(){
    var result = new Array<DocumentTag>();
    if(this.currentDocument.tags == null || this.currentDocument.tags.length == 0){
      return result;
    }
    for(const tag of this.currentDocument.tags){
      for(const docTag of this.importTagsList){
        if(docTag.id == tag.id){
              result.push(docTag);
        }
      }
    }
    return result;
  }

  recollatePages(){
    for(var i=0 ; i < this.currentDocument.pages.length; i++) {
      this.currentDocument.pages[i].pageNumber = i+1;
    }
  }

  private onRemoveModel (args) {
    //Here, this.playlists contains the elements reordered
    let [type, el, container, source, item, sourceModel, sourceIndex] = args;
    console.log('<removeModel> removed type: ' + JSON.stringify(type));
    console.log('<removeModel> removed el: ' + JSON.stringify(el));
    console.log('<removeModel> removed container: ' + JSON.stringify(container));
    console.log('<removeModel> removed source: ' + JSON.stringify(source));
    console.log('<removeModel> removed item: ' + JSON.stringify(item));
    console.log('<removeModel> removed sourceModel: ' + JSON.stringify(sourceModel));
    console.log('<removeModel> removed sourceIndex: ' + JSON.stringify(sourceIndex));
  }

  //
  // Hide the docuemnt Preview from the application
  //
  //
  toggleDocumentPagePreview() {
    this.showDocDetailsPagePreview = !this.showDocDetailsPagePreview;
  }

  //
  //
  //
  // 
  getShowDocsDateSortButtonIcon() {
    if(this.showDocsDateSortButtonIconIsUp) {
      return "ion-ios-arrow-up";
    } else {
      return "ion-ios-arrow-down";
    }
  }

  filterDocsOnDateSort(event: any){
    // toggle the flag
    this.showDocsDateSortButtonIconIsUp = !this.showDocsDateSortButtonIconIsUp;
    if ( this.showDocsDateSortButtonIconIsUp) {
      this.sortDateAscending = true;
    } else {
      this.sortDateAscending = false;
    }
    console.log('Sort By Date ' + this.filterSelectedSortChoice);
    //this.showDocuemntDetailsflag = false;
  }


  isDocumentPagePreviewOn() {
    return this.showDocDetailsPagePreview
  }

  isDocumentPagePreviewOnWithPages() {
    if(this.currentDocumentToShow != null && this.currentDocumentToShow.pages != null) {
      if (this.currentDocumentToShow.pages.length > 0) {
        return true
      }
    }
    return false;
  }

  getDocumentPreviewType() {
    if (this.currentDocumentToShow.id <= 0) {
      return "";
    }
    console.log('<Document Preview > type designation: ' + JSON.stringify(this.currentDocumentToShow.type.documentDesignation));
    if (this.currentDocumentToShow.type.documentDesignation === Document.TYPE_DESIGNATION_PROFILE) {
      return this.getInternationalizedToken('document.preview.pages.doc.type.backup'); 
    } else {
      return this.getInternationalizedToken('document.preview.pages.doc.type.linked'); 
    }
  }

  //
  // Show the docuemnt preview in the application
  //
  //

  showDocumentPreview(doc : Document) {
    this.currentDocumentToShow = doc;
    /** 
    console.log('<Backup Doc Preview> Start-->');
    if( this.showBackigDocDetailsCountBefore < this.currentDocumentBacking.length){
        this.showBackigDocDetailsCountBefore++
    }else {
      this.showBackigDocDetailsCountBefore--;
      this.showBackigDocDetails = false;
      return;
    }
    //
    //
    if(doc.attachedDocuments.length > 0) {
      this.showBackigDocDetails = true
      this.currentDocumentToShow = doc;
    }else{
      this.showBackigDocDetails = false;
    }
    */
  }

  getCurrentDocumentToShow() {
    if (this.currentDocumentToShow === null){
      return new Document();
    }else {
      return this.currentDocumentToShow;
    }
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }

  private getCurrentUserName() : string {
    return localStorage.getItem('username');
  }

  private getCurrentUser() : User {
    return JSON.parse(localStorage.getItem('user'));
  }

  private limitRecipientCount() {
    this.currentDocumentRecipients.splice(0, 1);
  }

  private recipientClickHandler(ev){
    console.log('CTRL pressed during click:', ev.ctrlKey);
    if(ev.ctrlKey == true) {
      this.currentDocumentRecipient = new User();
    }
  }


  //
  //
  // Mapping
  //
  //
  private toggleMapTraceData(){
    // toggle the map
    this.traceMapOnFlag = !this.traceMapOnFlag;
    if(this.traceMapOnFlag == true) {
      this.traceMap.off();
      this.traceMap.remove();
      //this.initializeMap();
      setTimeout(() => {
        this.traceMap.invalidateSize();
      });
    }
  }

  private initializeMap2(){
      //
      // Mappin Initialization
      let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
      const self = this;

      L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
      this.traceMap = L.map(el).setView([42.392564, 11.446314], 13);
      
      L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        subdomains: ['a','b','c']
      }).addTo(this.traceMap);

      for(const group of this.groupsForMap){
        if(group == null) {
          continue;
        } else {
          var gpsCoordinates = group.gpsCoordinates.split(",");
          var x: number = +gpsCoordinates[0];
          var y: number = +gpsCoordinates[1];

          //L.marker([13.522695, 100.274005]).addTo(this.traceMap)

          L.marker([x, y]).addTo(this.traceMap)
        .bindPopup('Farm 20<br> Location Unknown.')
        .openPopup();
        }
      }
  }

  private initializeMapBackup(){
    //
    // Mappin Initialization
    var locations = [
      [11.8166, 122.0942],
      [11.9804, 121.9189],
      [10.7202, 122.5621],
      [11.3889, 122.6277],
      [10.5929, 122.6325]
    ];
    var locationNames = [
      ["LOCATION_1"],
      ["LOCATION_2"],
      ["LOCATION_3"],
      ["LOCATION_4"],
      ["LOCATION_5"]
    ];
    
    let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
      const self = this;

      L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
      this.traceMap = L.map(el).setView([11.8166, 122.0942], 13);
      
      L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        subdomains: ['a','b','c']
      }).addTo(this.traceMap);
    
    for (var i = 0; i < locations.length; i++) {
      var marker = L.marker([locations[i][0], locations[i][1]])
        .bindPopup(locationNames[i][0])
        .addTo(this.traceMap);
    }
}


private initializeMap(){
  //
  // reload

  //this.traceMap.off();
  //this.traceMap.remove();
  //
  // Mappin Initialization
  let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
  L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
  console.log('<initializeMap> Starting...');
  console.log('<initializeMap> organizations: ' 
            + "<" + this.groupsForMap.length + ">" + JSON.stringify(this.groupsForMap));

  if(this.groupsForMap.length > 0) {
    //
    // add the markers
    var firstFlag : boolean = false;
    for (var i = 0; i < this.groupsForMap.length; i++) {
      console.log('<initializeMap> iteration: <' + i + ">");
      if(this.groupsForMap[i] != null){
        //
        // initialize the map
        if (firstFlag == false) {
          var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
          var x: number = +gpsCoordinates[0];
          var y: number = +gpsCoordinates[1];
          console.log('<initializeMap> setting startup view...' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
          this.traceMap = L.map(el).setView([x, y], 13);
          firstFlag = true;
          //
          //
          L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
              subdomains: ['a','b','c', 'd']
            }).addTo(this.traceMap);
            console.log('<initializeMap> Title Layer...');
        }
        console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
        gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
        x = +gpsCoordinates[0];
        y = +gpsCoordinates[1];
        var marker = L.marker([x, y])
          .bindPopup(this.groupsForMap[i].legalBusinessName)
          .addTo(this.traceMap);
      }
    }
    console.log('<initializeMap> Done...');
    this.refreshMap() 
  }
}

  refreshMap() {
    console.log('<initializeMap> Invalidating Map...');
    setTimeout(this.traceMap.invalidateSize.bind(this.traceMap), 500);

  }

  private initializeMapNgx(){
    this.mapOptions = {
      layers: [
          L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: '...' })
      ],
      zoom: 5,
      center: L.latLng([ 46.879966, -121.726909 ])
  };
}

  onResize(event) {
    console.log("<Resize Event> for Modal Map Dialog" + event);
  }

  onMapReady(map: any): void {
    console.log("<onMapReady> for Modal Map Dialog" + event);
    setTimeout(() => {
      map.invalidateSize();
    });
  }

  handleEvent(event: CustomEvent) {
    console.log(`${event.name} has been click on img ${event.imageIndex + 1}`);

    switch (event.name) {
      case 'print':
        console.log('run print logic');
        break;
    }
  }
  handleIndexChangeEvent(imageIndex: number) {
    console.log("Clicked on img" + imageIndex);
    this.currentPageIndex  = imageIndex + 1;
  }

  getMaxDocsDate() {
    if(this.documents != null) {
        if(this.documents.length > 0) {
          return DateUtils.getDateAsString(DateUtils.getDateFromString(this.documents[this.documents.length - 1].updationTimestamp));
        } else {
          return "";
        }
    } else {
      return ""
    }
  }

  getMinDocsDate() {
    if(this.documents != null) {
        if(this.documents.length > 0) {
          return DateUtils.getDateAsString(DateUtils.getDateFromString(this.documents[0].updationTimestamp));
        } else {
          return "";
        }
    } else {
      return ""
    }
  }

  getDocumentPageImages() {
    this.viewWidgetPages = new Array<string>();

    if(this.pageViewWidgeSource == PageViewerSource.DOC_CREATION_PREVIEW || this.pageViewWidgeSource == PageViewerSource.DOC_EDIT_PREVIEW) {
      if(this.isDocumentPagePreviewOnWithPages()) {
        if(this.getCurrentDocumentToShow() == null) {
          // do nothing
        } else {
          for (var i = 0; i < this.getCurrentDocumentToShow().pages.length; i++) {
            this.viewWidgetPages.push(
              this.serverURI + "/document/page?doc_id=" + this.getCurrentDocumentToShow().pages[i].id
            )
            console.log("Page URL: " + this.serverURI + "/document/page?doc_id=" + this.getCurrentDocumentToShow().pages[i].id);
          }
        return this.viewWidgetPages;
        }
      }
    }

    if(this.pageViewWidgeSource == PageViewerSource.DOC_FEED_DETAILS) {
      if(this.currentDocument == null){
        return this.viewWidgetPages;
      }
      for (var i = 0; i < this.currentDocument.pages.length; i++) {
        this.viewWidgetPages.push(
          this.serverURI + "/document/page?doc_id=" + this.currentDocument.pages[i].id
        )
        console.log("Page URL: " + this.serverURI + "/document/page?doc_id=" + this.currentDocument.pages[i].id);
      }
    }
    
    if(this.pageViewWidgeSource == PageViewerSource.DOC_LINKED_DOC_TAB_PREVIEW) {
      if(this.currentDocument == null){
        return this.viewWidgetPages;
      }
      if(this.getDocumentPages(this.viewWidgetCurrentDocumentId) == null) {
        return this.viewWidgetPages;
      }
      for (var i = 0; i < this.getDocumentPages(this.viewWidgetCurrentDocumentId).length; i++) {
        this.viewWidgetPages.push(
          this.serverURI + "/document/page?doc_id=" + this.getDocumentPages(this.viewWidgetCurrentDocumentId)[i].id
        )
        console.log("Page URL: " + this.serverURI + "/document/page?doc_id=" + this.getDocumentPages(this.viewWidgetCurrentDocumentId)[i].id);
      }
    }

    if(this.pageViewWidgeSource == PageViewerSource.DOC_BACKING_DOC_TAB_PREVIEW) {
      if(this.currentDocument == null){
        return this.viewWidgetPages;
      }
      if(this.getDocumentPages(this.viewWidgetCurrentDocumentId) == null) {
        return this.viewWidgetPages;
      }
      for (var i = 0; i < this.getAttachedDocumentPages(this.currentDocument.id, this.viewWidgetCurrentDocumentId).length; i++) {
        this.viewWidgetPages.push(
          this.serverURI + "/document/page?doc_id=" + this.getAttachedDocumentPages(this.currentDocument.id, this.viewWidgetCurrentDocumentId)[i].id
        )
        console.log("Page URL: " + this.serverURI + "/document/page?doc_id=" + this.getAttachedDocumentPages(this.currentDocument.id, this.viewWidgetCurrentDocumentId)[i].id);
      }
    }

    return this.viewWidgetPages;

    //return ['http://localhost:8080/WWFShrimpProject/api_v2/document/page?doc_id=14130'
    //,'http://localhost:8080/WWFShrimpProject/api_v2/document/page?doc_id=14128'
    //, 'http://localhost:8080/WWFShrimpProject/api_v2/document/page?doc_id=14129'];
  }

  /**
   * Return the 0-based image index of the images for 
   * pages being currently viewd in a Modal Popup lightbox widget
   * @returns - the 0-based starting index of the images
   */
  getDocumentPageImagesStartIndex() {
    //this.currentPageIndex = this.pageViewWidgetIndex - 1;
    return (this.pageViewWidgetIndex - 1);
  }

  /**
   * Will provide the index of the page clicked in teh thumbnail gallery on Document Details panel.
   * @param pageNumber - the clicked page index number (1-based)
   */
  clickedDocPageIndex(pageNumber: number, source: number, docId : number) {
    this.pageViewWidgetIndex = pageNumber;
    this.currentPageIndex = pageNumber; 
    this.pageViewWidgeSource = source;
    this.viewWidgetCurrentDocumentId = docId;
    console.log("<thumbnail gallery> Clicked on page image " + pageNumber + " with source " + this.pageViewWidgeSource + " and doc id: " + docId);
    
  }

  getNumberOfPagesCurrentDoc() {

    if(this.pageViewWidgeSource == PageViewerSource.DOC_CREATION_PREVIEW || this.pageViewWidgeSource == PageViewerSource.DOC_EDIT_PREVIEW) {
      if(this.isDocumentPagePreviewOnWithPages()) {
        if(this.getCurrentDocumentToShow() == null) {
          // do nothing
        } else {
          return this.getCurrentDocumentToShow().pages.length
        }
      }
    }
    
    if(this.pageViewWidgeSource == PageViewerSource.DOC_FEED_DETAILS) {
      if(this.currentDocument == null) {
        return 0;
      }
      return this.currentDocument.pages.length
    }

    if(this.pageViewWidgeSource == PageViewerSource.DOC_LINKED_DOC_TAB_PREVIEW) {
      if(this.getDocumentPages(this.viewWidgetCurrentDocumentId) == null) {
        return 0;
      }
      return this.getDocumentPages(this.viewWidgetCurrentDocumentId).length;
    }

    if(this.pageViewWidgeSource == PageViewerSource.DOC_BACKING_DOC_TAB_PREVIEW) {
      if(this.getDocumentPages(this.viewWidgetCurrentDocumentId) == null) {
        return 0;
      }
      return this.getAttachedDocumentPages(this.currentDocument.id, this.viewWidgetCurrentDocumentId).length;
    }
      
    return null;
    
  }

  getCurrentPageIndex() {
    return this.currentPageIndex;
  }

  getCurrentPageDesignation() {
    return "Page (" + this.getCurrentPageIndex() + "/" + this.getNumberOfPagesCurrentDoc() + ")";
  }

  navigateToTraceMap() {
    console.log('[Leaflet] <re-routing> '.concat('/pages/leafletMaps/'));
    //
    // Initialize th trace Map Data
    //
    localStorage.setItem(DocumentsComponent.SESSION_STORAGE_KEY_MAP_GROUPS, JSON.stringify(this.groupsForMap));

    console.log('[Leaflet] <mapping data> <transfer>'.concat(JSON.stringify(this.groupsForMap)));

    this.router.navigate(['/pages/leafletMaps/']);
  }
}
