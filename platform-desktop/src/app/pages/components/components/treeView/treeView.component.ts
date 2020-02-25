import {Component} from '@angular/core';
import {TreeModel} from 'ng2-tree';

@Component({
  selector: 'tree-view',
  templateUrl: './treeView.html',
})

export class TreeView {

  tree: TreeModel = {
    value: 'Organization A',
    children: [
      {
        value: 'Group 1',
        children: [
          {value: 'User 1'},
          {value: 'User 2'},
          {
            value: 'Sub Group 1',
            children: [
              {value: 'User 3'},
              {value: 'User 4'},
              {value: 'User 5'},
            ]
          }
        ]
      },
      {
        value: 'Group 2',
        children: [
          {value: 'User 4'},
          {value: 'User 5'},
          {value: 'User 6'},
        ]
      }
    ]
  };

  constructor() {
  }

}
