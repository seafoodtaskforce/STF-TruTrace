import { Component } from '@angular/core';

import { TrafficChartService } from './trafficChart.service';
import {BaThemeConfigProvider, colorHelper} from '../../../theme';
import * as Chart from 'chart.js';
import { Document } from '../../../models/document';

import { Injectable } from '@angular/core';

@Component({
  selector: 'traffic-chart',
  templateUrl: './trafficChart.html',
  styleUrls: ['./trafficChart.scss']
})

// TODO: move chart.js to it's own component
@Injectable()
export class TrafficChart {

  public doughnutData: Array<Object>;
  documents: Array<Document>;
  currNumberOfDocuments: number= 0;
  docTypeDitribution = new Array(0, 0, 0 , 0, 0);

  constructor(private _trafficChartService: TrafficChartService,
        private _baConfig: BaThemeConfigProvider ) {
    // this.doughnutData = _trafficChartService.getData();
  }

  ngAfterViewInit() {
    // this._loadDoughnutCharts();
  }

  ngOnInit() {
    this.getAllDocuments();
    
  }

  private _loadDoughnutCharts() {
    let el = jQuery('.chart-area').get(0) as HTMLCanvasElement;
    new Chart(el.getContext('2d')).Doughnut(this.doughnutData, {
      segmentShowStroke: false,
      percentageInnerCutout : 64,
      responsive: true
    });
  }

  getData() {
    let dashboardColors = this._baConfig.get().colors.dashboard;
    return [
      {
        value: this.docTypeDitribution[0].toString(),
        color: dashboardColors.mcpdDocColor,
        highlight: colorHelper.shade(dashboardColors.mcpdDocColor, 15),
        label: 'Bill of Lading',
        percentage: Math.floor((this.docTypeDitribution[0] / this.currNumberOfDocuments) * 100).toString(),
        order: 1,
      }, {
        value: this.docTypeDitribution[1].toString(),
        color: dashboardColors.captainStatementDocColor,
        highlight: colorHelper.shade(dashboardColors.captainStatementDocColor, 15),
        label: 'Vessel documents',
        percentage: Math.floor((this.docTypeDitribution[1] / this.currNumberOfDocuments) * 100).toString(),
        order: 4,
      }, {
        value: this.docTypeDitribution[2].toString(),
        color: dashboardColors.feedLotSheetDocColor,
        highlight: colorHelper.shade(dashboardColors.feedLotSheetDocColor, 15),
        label: 'Feed Documents',
        percentage: Math.floor((this.docTypeDitribution[2] / this.currNumberOfDocuments) * 100).toString(),
        order: 3,
      }, {
        value: this.docTypeDitribution[3].toString(),
        color: dashboardColors.fishingLogBookDocColor,
        highlight: colorHelper.shade(dashboardColors.fishingLogBookDocColor, 15),
        label: 'Farm Documents',
        percentage: Math.floor((this.docTypeDitribution[3] / this.currNumberOfDocuments) * 100).toString(),
        order: 2,
      }, {
        value: this.docTypeDitribution[4].toString(),
        color: dashboardColors.fishmealLotTraceabilityDocColor,
        highlight: colorHelper.shade(dashboardColors.fishmealLotTraceabilityDocColor, 15),
        label: 'Fishmeal Documents',
        percentage: Math.floor((this.docTypeDitribution[4] / this.currNumberOfDocuments) * 100).toString(),
        order: 0,
      },
    ];
  }

  getAllDocuments() {
    this._trafficChartService.getAllDocuments().subscribe(
      data => {
        this.documents = data;
        this.currNumberOfDocuments = data.length;
        for (let i: number = 0; i < this.documents.length; i++) {
          if (this.documents[i].documentType === 'Bill of Lading') {
            this.docTypeDitribution[0]++;
          }
          if (this.documents[i].documentType === 'Vessel documents') {
            this.docTypeDitribution[1]++;
          }
          if (this.documents[i].documentType === 'Feed Documents') {
            this.docTypeDitribution[2]++;
          }
          if (this.documents[i].documentType === 'Farm Documents') {
            this.docTypeDitribution[3]++;
          }
          if (this.documents[i].documentType === 'Fishmeal Documents') {
            this.docTypeDitribution[4]++;
          }
       }
       this.doughnutData = this.getData();
       this._loadDoughnutCharts();
      },
      error => console.log('Server Error'),
      // fill in the break donw of data
    );
  }

  
}
