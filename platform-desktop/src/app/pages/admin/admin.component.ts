import { Component } from '@angular/core';
import { DataTablesService } from './dataTables.service';

@Component({
  selector: 'admin',
  templateUrl: './dataTables.html',
  styleUrls: ['./dataTables.scss']
})
export class AdminComponent {

    data;
    filterQuery = "";
    rowsOnPage = 10;
    sortBy = "email";
    sortOrder = "asc";

    constructor(private service: DataTablesService) {
    this.service.getData().then((data) => {
      this.data = data;
    });
  }

    toInt(num: string) {
        return +num;
    }

    sortByWordLength = (a: any) => {
        return a.city.length;
    }
  
}

/** 
@Component({
  selector: 'admin',
  template: `<strong>Admin Content Here</strong>`,
})
export class AdminComponent {
  constructor() {}
}
*/