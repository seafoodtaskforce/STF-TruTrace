import {Injectable} from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import {BaThemeConfigProvider, colorHelper} from '../../../theme';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { Observable } from 'rxjs/Observable';
import { Document } from '../../../models/document';

// import global data
import * as AppGlobals from '../../../config/globals';

@Injectable()
export class TrafficChartService {

  constructor(private _baConfig: BaThemeConfigProvider, private http: Http) {
  }

  documents = new Array<Document>();
  docTypeDitribution = new Array(0, 0, 0 , 0, 0);

  readonly ALL_DOC_URL: string = this.getServerURI().concat('/document/fetchall');

  getData() {
    let dashboardColors = this._baConfig.get().colors.dashboard;
    return [
      {
        value: this.docTypeDitribution[0].toString(),
        color: dashboardColors.mcpdDocColor,
        highlight: colorHelper.shade(dashboardColors.mcpdDocColor, 15),
        label: 'Bill of Lading',
        percentage: 20,
        order: 1,
      }, {
        value: 3,
        color: dashboardColors.captainStatementDocColor,
        highlight: colorHelper.shade(dashboardColors.captainStatementDocColor, 15),
        label: 'Vessel documents',
        percentage: 8,
        order: 4,
      }, {
        value: 10,
        color: dashboardColors.feedLotSheetDocColor,
        highlight: colorHelper.shade(dashboardColors.feedLotSheetDocColor, 15),
        label: 'Feed Documents',
        percentage: 28,
        order: 3,
      }, {
        value: 5,
        color: dashboardColors.fishingLogBookDocColor,
        highlight: colorHelper.shade(dashboardColors.fishingLogBookDocColor, 15),
        label: 'Farm Documents',
        percentage: 14,
        order: 2,
      }, {
        value: 7,
        color: dashboardColors.fishmealLotTraceabilityDocColor,
        highlight: colorHelper.shade(dashboardColors.fishmealLotTraceabilityDocColor, 15),
        label: 'Fishmeal Documents',
        percentage: 20,
        order: 0,
      },
    ];
  }

  getAllDocuments(): Observable<Document[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    return this.http.get(this.ALL_DOC_URL, options)
        .map( (res: Response) => {
          this.documents = res.json();
          return res.json(); 
        })
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  getServerURI() {
    return AppGlobals.SERVER_URI;
  }


}
