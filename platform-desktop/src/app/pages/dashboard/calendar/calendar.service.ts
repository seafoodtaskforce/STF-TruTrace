import {Injectable} from '@angular/core';
import {BaThemeConfigProvider} from '../../../theme';

@Injectable()
export class CalendarService {

  constructor(private _baConfig:BaThemeConfigProvider) {
  }

  getData() {

    let dashboardColors = this._baConfig.get().colors.dashboard;
    return {
      header: {
        left: 'prev,next today',
        center: 'title',
        right: 'month,agendaWeek,agendaDay'
      },
      defaultDate: new Date(),
      selectable: true,
      selectHelper: true,
      editable: true,
      eventLimit: true,
      events: [
        {
          title: 'All Day Event',
          start: '2017-09-01',
          color: dashboardColors.silverTree
        },
        {
          title: 'Long Event',
          start: '2017-09-07',
          end: '2017-09-10',
          color: dashboardColors.blueStone
        },
        {
          title: 'Dinner',
          start: '2017-09-14T20:00:00',
          color: dashboardColors.surfieGreen
        },
        {
          title: 'Birthday Party',
          start: '2017-09-01T07:00:00',
          color: dashboardColors.gossip
        }
      ]
    };
  }
}
