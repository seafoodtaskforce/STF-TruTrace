import { Component } from '@angular/core';
import * as Handsontable from 'handsontable/dist/handsontable.full.js';
import { getAdvancedData } from './data';


@Component({
  selector: 'advanced-demo',
  templateUrl: './advanced-demo.html'
})
export class AdvancedDemoComponent {
   data:Array<any>;
   colHeaders:Array<string>;
   columns:Array<any>;
   options:any;

  constructor() {
    this.data = getAdvancedData();
    this.colHeaders = ['Stage 1', 'Stage 2', 'Stage 3', 'Stage 4', 'Stage 5'];
    this.columns = [
      {data: 0, type: 'text'},
      {data: 1, type: 'text'},
      {data: 2, type: 'text'},
      {data: 3, type: 'text'},
      {data: 4, type: 'text'},
    ];
    this.options = {
      height: 396,
      rowHeaders: true,
      stretchH: 'all',
      columnSorting: true,
      contextMenu: true,
      className: 'htCenter htMiddle',
      readOnly: true
    };
  }
}
