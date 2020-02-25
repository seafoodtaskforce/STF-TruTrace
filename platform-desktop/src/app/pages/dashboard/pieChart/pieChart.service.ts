import {Injectable} from '@angular/core';
import {BaThemeConfigProvider, colorHelper} from '../../../theme';

@Injectable()
export class PieChartService {

  constructor(private _baConfig:BaThemeConfigProvider) {
  }

  getData() {
    let pieColor = this._baConfig.get().colors.custom.dashboardPieChart;
    return [
      {
        color: pieColor,
        description: 'New Submitted Docs',
        stats: '7',
        icon: 'person',
      },
      {
        color: pieColor,
        description: 'Added Traces',
        stats: '17',
        icon: 'refresh',
      }
    ];
  }
}
