import {Component} from '@angular/core';

import {FeedService} from './feed.service';
import { Document } from '../../../models/document';

@Component({
  selector: 'feed',
  templateUrl: './feed.html',
  styleUrls: ['./feed.scss']
})
export class Feed {

  flag = false;
  currentDocument:Document;

  public feed:Array<Object>;
  public documents:Array<Document>;

  constructor(private _feedService:FeedService) {
  }

  ngOnInit() {
    this._loadFeed();
    this.getAllDocuments();
  }

  expandMessage (message){
    message.expanded = !message.expanded;
  }

  private _loadFeed() {
    this.feed = this._feedService.getData();
  }

  getAllDocuments(){
    this._feedService.getAllDocuments().subscribe(
      data => this.documents = data,
      error => console.log('Server Error'),
    );
  }

  showDetails(id:number){
    this.flag = true;
    for(let doc of this.documents){
      if(doc.id === id){
        this.currentDocument = doc;
      }
    }
  }
}
