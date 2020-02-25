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
  @Input() showDocsFilterButton:boolean = false;
  @Input() showDocsFilterButtonTitle:String;
  @Input() showAllDocTraceFilterButton:boolean = false;
  @Input() showAllDocTraceFilterButtonTitle:String;

  @Output() dismissWindowEvent = new EventEmitter();
  @Output() filterDocsWindowEvent = new EventEmitter();
  @Output() filterTraceDocsWindowEvent = new EventEmitter();
  


  dismissWindow(event: any) {
    this.cardVisible = false;
    this.dismissWindowEvent.emit();
  }

  filterDocsWindow(event: any) {
    this.filterDocsWindowEvent.emit();
  }

  filterTraceDocsWindow(event: any) {
    this.filterTraceDocsWindowEvent.emit();
  }
  




}
