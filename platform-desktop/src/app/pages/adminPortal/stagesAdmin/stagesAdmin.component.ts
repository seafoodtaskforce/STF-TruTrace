import { Component } from '@angular/core';

import {DataLoadService} from '../dataLoad.service';
import { DocumentService } from './../../documents/document.service';
import { OrganizationStage } from './../../../models/OrganizationStage';
import { LookupEntity } from '../../../models/LookupEntity';
import { User } from '../../../models/User';
import { AppResource } from '../../../models/AppResource';

import { ToasterService } from '../../../toaster-service.service';
import { LocaleUtils } from '../../../utils/locale.utils';
//
// Drag and Drop
import { DragulaService } from 'ng2-dragula/ng2-dragula';
import { Subscription } from 'rxjs/Subscription';

@Component({
  selector: 'stages-admin-table',
  templateUrl: './stagesAdminTable.html',
  styleUrls: ['./stagesAdmin.scss'],
})
export class StagesAdminTable {


    /**
     * Static global variables
     */
    public static readonly EMPTY_STAGE_LABEL = "Stage ";
    public static readonly LEFT_LANGAUGE_COLUMN = 0;
    public static readonly RIGHT_LANGAUGE_COLUMN = 1;
    public static readonly NUMBER_OF_DEFAULT_VISIBLE_STAGES = 5;
    public static readonly HEADER_TEXT_PARSING_TOKEN = ' ';
    public static readonly STAGE_KEY_RESOURCE_PREFIX = 'stage.';
    public static readonly STAGE_LOCAL_RESOURCE_KEY = 'stage.key';
    public static readonly STAGE_LOCAL_RESOURCE_KEY_SEPARATOR = '.';

    public static readonly STAGE_FIELD_CHARACTER_LIMIT = 21;

    /**
     * Member variables local
     */

     // the specific supported languages
    languages:LookupEntity[];
    selectedLanguageChoices:string[]=['en', 'vi', 'th'];

    //
    // Full Data 
    allStagesLanguageMapped: Map<string, OrganizationStage[]> = new Map<string, OrganizationStage[]>();

    // number of stages that can be seen
    numberOfVisibleStages: number = StagesAdminTable.NUMBER_OF_DEFAULT_VISIBLE_STAGES;
    // The stage data for column 1
    stageHeadersLanguage1:string[]  = new Array<string>();
    // The stage data for column 2
    stageHeadersLanguage2:string[]  = new Array<string>();

    //
    // Stages from server
    organizationStages: OrganizationStage[]  = new Array<OrganizationStage>(); 

    //
    // Content tracking
    hasContentChangedFlag:boolean = false;


    //
    // selections for stage names

    isAutoTranslationOn: boolean = false;
    areStagesLocked: boolean = false;
    previewIsOn: boolean = true;

    //
    // Drag and Drop
    public groups: Array<any> = [
      {
        name: 'Group A',
        items: [{name: 'Item A'},{name: 'Item B'},{name: 'Item C'},{name: 'Item D'}]
      },
      {
        name: 'Group B',
        items: [{name: 'Item 1'},{name: 'Item 2'},{name: 'Item 3'},{name: 'Item 4'}]
      }
    ];
    permissionsSourceOptions : any = {
      copy : true
    }


    /**
     * Initialization of the component
     */
    ngOnInit() {
      this.loggedInName = localStorage.getItem('username');
      //
      // Init server data
      
      this.getAvailableLanguages();
      this.getAllStages();
      
    }

    ngAfterViewInit() {
      

    }
    loggedInName :string;

  /**
   * Contruction of the object. Mainly service and inner data initializtion
   * @param userService 
   * @param _documentService 
   */
  constructor(protected _dataService : DataLoadService, protected _documentService: DocumentService,
              protected _toasterService:ToasterService, private dragulaService: DragulaService) {
                //
                // SET UP DRAG/DROP

                
  }

  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  /**
   * Add a new Row of stages
   */
  addStageRow(){

    // 
    // fill in the rest of the data for the stages
    let index = this.allStagesLanguageMapped.get(this.languages[0].name).length;
    for (let language of this.languages) {
      console.log('[Stages Admin (Admin)] CONVERSION: Language '.concat(language.value));
      this.addNewStage(language.name, index);
    }
    this.hasContentChangedFlag = true;
  }


  /**
   * Cycles through the preview on/off panel visibility
   */
  togglePreviewStages(){
    this.previewIsOn = !this.previewIsOn;
  }


  /**
   * Event handler triggered when user types text into any of the stage text boxes
   * This routine collates and saves the data entered.
   * @param event - the key event which contains the key pressed
   * @param col_index - the specific language index (langauge column)
   * @param row_index  - the specific stage index
   */
  stageEventHandler(event: any, col_index: number, row_index:number){
    var stageValue:OrganizationStage;


    //
    // Check if we allow this
    if(StagesAdminTable.STAGE_FIELD_CHARACTER_LIMIT < event.target.value.length){
      this._toasterService.Warning("You have reached the character limit for a stage name.");
      console.log("[Stages Admin (Admin)] Limit has been reached: ");
    }

    //
    // 
    this.hasContentChangedFlag = true;

    console.log("[Stages Admin (Admin)] Stage Keyup Event: ", event.target.value, event, event.keyCode, event.keyIdentifier);

    var tempArray: Array<OrganizationStage> = this.allStagesLanguageMapped.get(this.languages[col_index].name);
    console.log("[Stages Admin (Admin)] Stage Keyup Event: ",  JSON.stringify(tempArray));
    tempArray[row_index].value = event.target.value;
    this.allStagesLanguageMapped.set(this.languages[col_index].name, tempArray);
    stageValue = this.allStagesLanguageMapped.get(this.languages[col_index].name)[row_index];
    console.log('[Stages Admin (Admin)] Stage Keyup Event: ' + stageValue);
    /**
    if(col_index == StagesAdminTable.LEFT_LANGAUGE_COLUMN){
      this.stageHeadersLanguage1[row_index] = event.target.value;
    }else{
      this.stageHeadersLanguage2[row_index] = event.target.value;
    }
 */
    //
    // change the value in the resourcemap
    let resource1:AppResource = new AppResource();
    resource1.key = stageValue.name;
    resource1.value = stageValue.value;
    resource1.locale = this.languages[col_index].name;
    console.log('[Stages Admin (Admin)] - Editing Resource <entity> ' + JSON.stringify(resource1));
    LocaleUtils.addResourceToResourceMap(resource1);

    

    // check if threshold has been reached
    if(event.target.value.length == 25){

    }
  }

  /**
   * Get the CSS Styling color for the headers in thew preview
   */
  getHeaderTraceabilityClassColor(header:string) {
    console.log('STAGE HEADER ---> : ' + header);
    if(header.length > 0 ){
      return 'shape-gray';
    }else{
      return 'shape-white';
    }
  }

  /**
   * Sets the langauge from the drop down to relfect the current lanaguage 
   * chosen foe the column specificed by index (0 - based)
   * 
   * @param index  - the index of the language table
   * @param event  - the event that hold sthe language chosen
   */
  setChosenLanguage(index: number, event: any){
    console.log('[Stages Admin (Admin)] Select Language <event>: ', event, event.target.value);
    console.log('[Stages Admin (Admin)] Select Language : '.concat(event.target.value).concat(' ').concat(this.getLanguageKey(event.target.value)));
    this.selectedLanguageChoices[index] = this.getLanguageKey(event.target.value);
    //
    // get the correct data for the header preview
    var tempArray: Array<OrganizationStage> = this.allStagesLanguageMapped.get(this.selectedLanguageChoices[index]);
    if(index==0){
    // stages and headers

      for (var _j = 0; _j < this.numberOfVisibleStages; _j++) {
        this.stageHeadersLanguage1[_j]=this.allStagesLanguageMapped.get(this.getLanguageKey(event.target.value))[_j].value;
        //tempArray[_j].value = this.stageHeadersLanguage1[_j];
      }
    }else{
      for (var _j = 0; _j < this.numberOfVisibleStages; _j++) {
        this.stageHeadersLanguage2[_j]=this.allStagesLanguageMapped.get(this.getLanguageKey(event.target.value))[_j].value;
        //tempArray[_j].value = this.stageHeadersLanguage1[_j];
      }
    }
    console.log('[Stages Admin (Admin)] Select Language : <all> '.concat(JSON.stringify(this.allStagesLanguageMapped)));
    console.log('[Stages Admin (Admin)] Select Language : <choice>'.concat(JSON.stringify(event.target.value)));
    //this.allStagesLanguageMapped.set(this.selectedLanguageChoices[index], tempArray);
    
  }

  /**
   * Get the text that the user has placed
   * @param langauge - the langauge being used
   * @param index - the stage index (column)
   */
  getHeaders(columnLanguageIndex: number){
    console.log('STAGE PREVIEW ---> : '.concat(this.languages[columnLanguageIndex].name + ' ' + columnLanguageIndex + ' ').concat(JSON.stringify(this.allStagesLanguageMapped.get(this.languages[columnLanguageIndex].name))));
    // return this.stagesMap.get(this.languages[columnLanguageIndex]);

    return this.allStagesLanguageMapped.get(this.selectedLanguageChoices[columnLanguageIndex]);
  }

  /**
   * Get the text that the user has placed
   * @param langauge - the langauge being used
   * @param index - the stage index (column)
   */
   getSparseHeaders(columnLanguageIndex: number){
    if(this.allStagesLanguageMapped == null){
      return new Array<OrganizationStage>();
    }
     if(this.allStagesLanguageMapped.get(this.selectedLanguageChoices[columnLanguageIndex]) == null) {
       return new Array<OrganizationStage>();
     }
    console.log('STAGE PREVIEW ---> : '.concat(this.languages[columnLanguageIndex].name + ' ' + columnLanguageIndex + ' ').concat(JSON.stringify(this.allStagesLanguageMapped.get(this.languages[columnLanguageIndex].name))));
    // return this.stagesMap.get(this.languages[columnLanguageIndex]);

    var filtered = this.allStagesLanguageMapped.get(this.selectedLanguageChoices[columnLanguageIndex]).filter(function (el) {
      return (el != null && el.value.length != 0);
    });
    return filtered;
  }

  /**
   * Will return the place holder for the stage input box
   * @param language  - the language column name
   * @param index  - the index of the stage
   */
  getStagePlaceHolder(language:string, index: number){
    // TODO Needs to be internatioanlized
    return StagesAdminTable.EMPTY_STAGE_LABEL.concat(''+(index+1));
  }

  /**
   * This will split out the header text and return the leftmost string
   * @param header  - the header string as input by the user
   */
  getHeaderTextTop(header:string){
    // parse with spaces and get the first string
    let splitted:string[] = header.split(StagesAdminTable.HEADER_TEXT_PARSING_TOKEN);
    return splitted[0];

  }

  /**
   * This will split out the header text and return the righmost string
   * If there is no righmost string (i.e. no split) then return empty string ''
   * @param header  - the header string as input by the user
   */
  getHeaderTextBottom(header:string){
    // parse with spaces and get the second string
    // if now change then return empty
    let splitted:string[] =header.split(StagesAdminTable.HEADER_TEXT_PARSING_TOKEN);
    if(splitted.length > 1){
      return splitted[1];
    }else{
      return '';
    }
  }

  /**
   * Gets the current stage input data values from memory
   * @param languageIndex  - which language is requested
   * @param row  - which stage is requested
   */
  getStageColumnText(languageIndex:number, row:number){
    console.log('STAGE COLUMN---> : '.concat(' ' + languageIndex + ':' + row));
    //console.log('STAGE COLUMN---> : '.concat(' ' + languageIndex + ':' + row).concat(JSON.stringify(this.allStagesLanguageMapped.get(this.allStagesLanguageMapped[languageIndex])[row])));
    return this.allStagesLanguageMapped.get(this.selectedLanguageChoices[languageIndex])[row].value;
  }

  /**
   * Check if the stage edit functionality is locked or not
   */
  isLocked(){
    return this.areStagesLocked;
  }

  /**
   * 
   * @param event Event triggered when the user toggles the checkbox for locking stages
   */
  stagesLockedEventHandler(event: any){
    this.areStagesLocked = !this.areStagesLocked;
    console.log('SELECT LOCKED EDIT ---> : '.concat(JSON.stringify(event.target)));
    console.log('SELECT LOCKED EDIT ---> : '.concat(JSON.stringify(this.areStagesLocked )));
  }

  showDeleteStageButton(index:number){
    if(!this.isLocked()){
      return true;
    }else{
      return false;
    }
  }

  deleteStage(index:number){
    // remove the stage data
    console.log('[Stages Admin (Admin)] DELETING STAGE#  ---> : '.concat(''+index));

     for(let i=0; i< this.languages.length; i++){
        // remove from stages
        let stage:OrganizationStage = this.allStagesLanguageMapped.get(this.selectedLanguageChoices[i])[index];
        this.allStagesLanguageMapped.get(this.selectedLanguageChoices[i]).splice(index, 1);

        // remove from resources
        //
        let resource:AppResource = LocaleUtils.fetchResourceFromResourceMap(stage.name , this.selectedLanguageChoices[i]);
        console.log('[Stages Admin (Admin)] - Removing Resource <entity> ' + JSON.stringify(resource));
        LocaleUtils.removeResourceFromResourceMap(resource);
        if(resource.id > 0 ){
          this.deleteResource(resource);
        }
        //
        // realign the key generator
    }
    // reduce the number of stages
    this.numberOfVisibleStages--;
    //
    // 
    this.hasContentChangedFlag = true;
  }

  getStageCount(){
    return this.allStagesLanguageMapped.get(this.selectedLanguageChoices[0]);
  }

  getAllStages(){
    this._dataService.getAllStages().subscribe(
      data => { 
        this.organizationStages = data;
        console.log('[Stages Admin (Admin)] GET ALL STAGES RESTFUL '.concat(JSON.stringify(this.organizationStages)));
        console.log('[Stages Admin (Admin)] NUMBER OF STAGES '.concat('' +this.organizationStages.length));
        // init the key
        this.initStageResourceKey(this.organizationStages);
        // 
        // fill in the rest of the data for the stages
        for (let language of this.languages) {
          console.log('[Stages Admin (Admin)] CONVERSION: Language '.concat(language.value));
          const clonedStages  = Object.assign([], this.organizationStages);
          this.convertStages(language.name, clonedStages);
        };
        console.log('[Stages Admin (Admin)] CONVERSION: <done> ');
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

  getLanguageKey(value:string){
    for (let language of this.languages) {
      if(language.value === value){
        return language.name;
      }
    }
    return null;
  }

  getNextStageResourceKey(orgID : number){
    let tempStages: OrganizationStage[]  = this.allStagesLanguageMapped.get(this.languages[0].name);
    if(tempStages === null || tempStages.length == 0){
      localStorage.setItem(StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY, '0');
      return StagesAdminTable.STAGE_KEY_RESOURCE_PREFIX + orgID + StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY_SEPARATOR + 0;
      
    }
    // extract the suffix value from the latest key
    let keySuffix: number = +localStorage.getItem(StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY);
    keySuffix++;
    localStorage.setItem(StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY, '' + keySuffix);
    return StagesAdminTable.STAGE_KEY_RESOURCE_PREFIX + orgID + StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY_SEPARATOR + keySuffix;
  }

  initStageResourceKey(tempStages: OrganizationStage[]){
    if(tempStages === null || tempStages.length == 0){
      localStorage.setItem(StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY, '0');
      return;
    }
    let max:number = 0;
    for(const stage of tempStages){ 
      // extract the number from the name
      let key:string = stage.name.split('.')[2];
      console.log('[Stages Admin (Admin)] SEARCHING MAX <key> ' + key);
      if(+key > max){
        max = +key;
      }
    }
    localStorage.setItem(StagesAdminTable.STAGE_LOCAL_RESOURCE_KEY, '' + max);
    console.log('[Stages Admin (Admin)] MAX <key> ' + max);
  }

  /**
   * Convert the stage data into internationalized set of columns
   * @param language -  the specific langauge column to process
   * @param stages -  the stages to internationalize
   */
  convertStages(language:string, stages: OrganizationStage[]){
    var value:string;
    

    // init the map if necessary
    if(stages.length == 0){
      console.log('[Stages Admin (Admin)] - The Input Stages are <empty> visble stages: '.concat(''+this.numberOfVisibleStages));
      this.allStagesLanguageMapped.set(language,new Array<OrganizationStage>());
    }else{
      this.allStagesLanguageMapped.set(language,stages);
    }

    let limit:number;
    if(stages.length >= this.numberOfVisibleStages){
      limit = stages.length;
    }else{
      limit = this.numberOfVisibleStages;
    }

    for (var _j = 0; _j < limit; _j++) {
      
      if(_j >= stages.length){
        //
        // There is no stage - so add it
        this.addNewStage(language, _j);
        console.log('[Stages Admin (Admin)] - Empty Stage Added ');
      }else{
        //
        // Map the value to inner list
        value = LocaleUtils.getInternationalizedString(stages[_j].name, language);
        if(value === LocaleUtils.defaultValueForMissingKey){
          value = '';
        }
        let clonedStage = OrganizationStage.clone(stages[_j]);
        clonedStage.value = value;
        // add to the map
        this.allStagesLanguageMapped.get(language)[_j] = clonedStage;
        console.log('[Stages Admin (Admin)] - Existing Stage Added ');
      }
    }
    console.log('[Stages Admin (Admin)] - DocType Admin Array FULL MAPPED ===== '.concat(language+'  ').concat(JSON.stringify(this.allStagesLanguageMapped.get(language)))); 
  }


  /**
   * Create a new (temporary) stage with data
   * @param language - the language under which the stage is being stored
   */
  addNewStage(language:string, index:number){
    let tempStage: OrganizationStage = new OrganizationStage();
    console.log('[Stages Admin (Admin)] - Adding New Stage --- ] '.concat(JSON.stringify(this.allStagesLanguageMapped.get(language))));

    let user: User;
    user = JSON.parse(localStorage.getItem('user'));

    //
    // initialize the new stage
    if(this.languages[0].name === language){
      tempStage.name = this.getNextStageResourceKey(user.userGroups[0].organizationId);
      console.log('[Stages Admin (Admin)] - Using <new> KEY: ' + tempStage.name);
    }else{
      tempStage.name = this.allStagesLanguageMapped.get(this.languages[0].name)[index].name;
      console.log('[Stages Admin (Admin)] - Using <existing> KEY: ' + tempStage.name);
    }
    
    tempStage.value = '';
    tempStage.orderIndex = index;
    tempStage.orgID = user.userGroups[0].organizationId;
    //
    // Add the stage to existing stage array
    console.log('[Stages Admin (Admin)] - Adding New Stage Data --- ] '.concat(JSON.stringify(tempStage)));
    console.log('[Stages Admin (Admin)] - Adding New Stage ALL Data --- ] '.concat(JSON.stringify(this.allStagesLanguageMapped.get(language))));
    this.allStagesLanguageMapped.get(language).push(tempStage);
    console.log('[Stages Admin (Admin)] - Added New Stage ALL Data --- ] '.concat(JSON.stringify(this.allStagesLanguageMapped.get(language))));

    //
    // Add to resources
    console.log('[Stages Admin (Admin)] - Adding resource <language> ' + language);
    let resource1:AppResource = new AppResource();
    resource1.key = tempStage.name;
    resource1.value = tempStage.value;
    resource1.locale = language;
    console.log('[Stages Admin (Admin)] - Adding resource <entity> ' + JSON.stringify(resource1));
    LocaleUtils.addResourceToResourceMap(resource1);
    console.log('[Stages Admin (Admin)] - ADDED resource: ' + JSON.stringify(resource1));

    // increase the number of stages
    let increaseVisibleStages:boolean = (this.numberOfVisibleStages < index);
    if(increaseVisibleStages){
      this.numberOfVisibleStages++;
    }
  }

  saveStageEdits(){
    this.saveStageData();
    this.saveStageResources();

    //
    // reset the changed content flag
    this.hasContentChangedFlag = false;
  }

  getMaxStageCharSize(){
    return StagesAdminTable.STAGE_FIELD_CHARACTER_LIMIT;
  }

  /****************************************************************************
   * Back-End Methods
   */


  /**
   * 
   */
  saveStageResources(){

    for (let language of this.languages) {
        // <TODO> Add the other possible values
        let stages: OrganizationStage[] = this.allStagesLanguageMapped.get(language.name);
        for (let stage of stages) {
          let resource:AppResource = new AppResource();
          resource.key = stage.name;
          resource.value = stage.value;
          resource.locale = language.name;
          this.saveResource(resource);
        }
    }
  }   

  /**
  * Save Stage data to the server and get the properly updated data with id (for creation)
  */
  saveStageData(){
    let stages: OrganizationStage[] = this.allStagesLanguageMapped.get(this.languages[0].name);
    //
    // Save the stages
    this._dataService.createNewStage(stages).subscribe(
      data =>  {
        console.log('No issues');
        console.log('[Stages Admin (Admin)] - Saved Stage Data <return> --- ] '.concat(JSON.stringify(data)));
        this.allStagesLanguageMapped.set(this.languages[0].name, data);
        this._toasterService.Success("Stage Changes Were Saved Succesfully.");
      },
      error => {
        console.log('Server Error');
        this._toasterService.Error("Unable to Save Changes. Please Try Again.");
      }
    );
  }

  /**
   * 
   * @param resource 
   */
  saveResource(resource: AppResource) {
    this._dataService.createNewResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

    /**
   * 
   * @param resource 
   */
  deleteResource(resource: AppResource) {
    this._dataService.deleteResource(resource).subscribe(
      data =>  console.log('No issues'),
      error => console.log('Server Error'),
    );
  }

  isSaveButtonEnabled(){
    return this.hasContentChangedFlag;
  }

}
