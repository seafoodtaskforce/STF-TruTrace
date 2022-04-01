import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'ba-card',
  templateUrl: './baCard.html',
})
export class BaCard {
  @Input() cardVisible:boolean = true;
  @Input() cardTitle:String;
  @Input() baCardClass:String;
  @Input() cardType:String;
  @Input() showButton:boolean = false;
  @Input() showButtonTitle:String;
  @Input() showDocsDateSortButton:boolean = false;
  @Input() showDocsDateSortButtonTitle:String;
  @Input() showDocsDateSortButtonIcon:String = "ion-ios-arrow-up";
  @Input() showDocsFilterButton:boolean = false;
  @Input() showDocsFilterButtonTitle:String;
  @Input() showAllDocTraceFilterButton:boolean = false;
  @Input() showAllDocTraceFilterButtonTitle:String;

  //
  // Trace Buttons
  //
  @Input() exportTraceDocsWindowEventButtonVisible:boolean = false;
  @Input() exportTraceDocsWindowEventButtonTitle:String = "Export Trace"
  @Input() showTraceMapWindowEventButtonVisible:boolean = false;
  @Input() showTraceMapWindowEventButtonTitle:String = "Show Trace Map";
  @Input() exportTraceGPSDataWindowEventButtonVisible:boolean = false;
  @Input() exportTraceGPSDataWindowEventButtonTitle:String = "Export GPS Data";
  //
  // Map Buttons
  //
  @Input() mapShowOrganizationPathToggleWindowEventButtonVisible:boolean = false;
  @Input() mapShowOrganizationPathToggleWindowEventButtonTitle:String = "Toggle Org Paths"
  @Input() mapShowOrganizationPopupToggleWindowEventButtonVisible:boolean = false;
  @Input() mapShowOrganizationPopupToggleWindowButtonTitle:String = "Toggle Org Descriptions";
  @Input() mapShowDocumentToggleWindowEventButtonVisible:boolean = false;
  @Input() mapShowDocumentToggleWindowEventButtonTitle:String = "Toggle Documents";
  @Input() mapShowDocInfoToggleWindowEventButtonVisible:boolean = false;
  @Input() mapShowDocInfoToggleWindowEventButtonTitle:String = "Toggle Doc Info";
  @Input() mapShowDocPathToggleWindowEventButtonVisible:boolean = false;
  @Input() mapShowDocPathToggleWindowEventButtonTitle:String = "Toggle Doc Paths";

  //
  // General Window Events
  //
  @Output() dismissWindowEvent = new EventEmitter();
  //
  // Filter Docs Events
  //
  @Output() filterDocsWindowEvent = new EventEmitter();
  @Output() filterDocsDateSortsWindowEvent = new EventEmitter();
  //
  // Trace Events
  //
  @Output() filterTraceDocsWindowEvent = new EventEmitter();
  @Output() exportTraceDocsWindowEvent = new EventEmitter();
  @Output() showTraceMapWindowEvent = new EventEmitter();
  @Output() exportTraceGPSDataWindowEvent = new EventEmitter();
  //
  // Mapping Events
  //
  @Output() mapShowOrganizationPathToggleWindowEvent = new EventEmitter();
  @Output() mapShowOrganizationPopupToggleWindowEvent = new EventEmitter();
  @Output() mapShowDocumentToggleWindowEvent = new EventEmitter();
  @Output() mapShowDocInfoToggleWindowEvent = new EventEmitter();
  @Output() mapShowDocPathToggleWindowEvent = new EventEmitter();



  dismissWindow(event: any) {
    this.cardVisible = false;
    this.dismissWindowEvent.emit();
  }

  filterDocsDateSortsWindow(event: any) {
    this.filterDocsDateSortsWindowEvent.emit();
  }

  filterDocsWindow(event: any) {
    this.filterDocsWindowEvent.emit();
  }

  //
  // Trace Windows
  //
  filterTraceDocsWindow(event: any) {
    this.filterTraceDocsWindowEvent.emit();
  }

  exportTraceDocsWindow(event: any) {
    this.exportTraceDocsWindowEvent.emit();
  }

  showTraceMapWindow(event: any) {
    this.showTraceMapWindowEvent.emit();
  }

  exportTraceGPSDataWindow(event: any) {
    this.exportTraceGPSDataWindowEvent.emit();
  }

  //
  // Mapping Windows
  //
  mapShowOrganizationPathToggleWindow(event: any) {
    this.mapShowOrganizationPathToggleWindowEvent.emit();
  }


  mapShowOrganizationPopupToggleWindow(event: any) {
    this.mapShowOrganizationPopupToggleWindowEvent.emit();
  }

  mapShowDocumentToggleWindow(event: any) {
    this.mapShowDocumentToggleWindowEvent.emit();
  }

  mapShowDocInfoToggleWindow(event: any) {
    this.mapShowDocInfoToggleWindowEvent.emit();
  }

  mapShowDocPathToggleWindow(event: any) {
    this.mapShowDocPathToggleWindowEvent.emit();
  }
}
