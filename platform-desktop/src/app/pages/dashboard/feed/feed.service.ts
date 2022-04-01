import {Injectable} from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { Document } from '../../../models/document';

// import global data
import * as AppGlobals from '../../../config/globals';
import { ServerUtils } from 'app/utils/server.utils';

@Injectable()
export class FeedService {
  readonly ALL_DOC_URL: string = this.getServerURI().concat('/document/fetchall');

  constructor(private http : Http){

  }

  getAllDocuments(): Observable<Document[]> {
    return this.http.get(this.ALL_DOC_URL)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  private _data = [
    {
      type: 'text-message',
      author: 'Piotr',
      surname: 'Paweska',
      header: 'Posted new message',
      text: 'MCPD',
      time: 'Date',
      ago: 'HGDUY-HGJD-UYIDU-HGDU',
      expanded: true,
    }, {
      type: 'video-message',
      author: 'Andrey',
      surname: 'Hrabouski',
      header: 'Added new video',
      text: '"Vader and Me"',
      preview: 'app/feed/vader-and-me-preview.png',
      link: 'https://www.youtube.com/watch?v=IfcpzBbbamk',
      time: 'Today 9:30 pm',
      ago: '3 hrs ago',
      expanded: true,
    }, {
      type: 'image-message',
      author: 'Vlad',
      surname: 'Lugovsky',
      header: 'Added new image',
      text: '"My little kitten"',
      preview: 'app/feed/my-little-kitten.png',
      link: 'http://api.ning.com/files/DtcI2O2Ry7A7VhVxeiWfGU9WkHcMy4WSTWZ79oxJq*h0iXvVGndfD7CIYy-Ax-UAFCBCdqXI4GCBw3FOLKTTjQc*2cmpdOXJ/1082127884.jpeg',
      time: 'Today 2:20 pm',
      ago: '10 hrs ago',
      expanded: true,
    }, {
      type: 'text-message',
      author: 'Nasta',
      surname: 'Linnie',
      header: 'Posted new message',
      text: 'Haha lol',
      time: '11.11.2015',
      ago: '2 days ago',
      expanded: true,
    }, {
      type: 'geo-message',
      author: 'Nick',
      surname: 'Cat',
      header: 'Posted location',
      text: '"New York, USA"',
      preview: 'app/feed/new-york-location.png',
      link: 'https://www.google.by/maps/place/New+York,+NY,+USA/@40.7201111,-73.9893872,14z',
      time: '11.11.2015',
      ago: '2 days ago',
      expanded: true,
    }, {
      type: 'text-message',
      author: 'Vlad',
      surname: 'Lugovsky',
      header: 'Posted new message',
      text: "First snake: I hope I'm not poisonous. Second snake: Why? First snake: Because I bit my lip!",
      time: '12.11.2015',
      ago: '3 days ago',
      expanded: true,
    }, {
      type: 'text-message',
      author: 'Andrey',
      surname: 'Hrabouski',
      header: 'Posted new message',
      text: 'How do you smuggle an elephant across the border? Put a slice of bread on each side, and call him "lunch".',
      time: '14.11.2015',
      ago: '5 days ago',
      expanded: true,
    }, {
      type: 'text-message',
      author: 'Nasta',
      surname: 'Linnie',
      header: 'Posted new message',
      text: 'When your hammer is C++, everything begins to look like a thumb.',
      time: '14.11.2015',
      ago: '5 days ago',
      expanded: true,
    }, {
      type: 'text-message',
      author: 'Alexander',
      surname: 'Demeshko',
      header: 'Posted new message',
      text: '“I mean, they say you die twice. One time when you stop breathing and a second time, a bit later on, when somebody says your name for the last time." ©',
      time: '15.11.2015',
      ago: '6 days ago',
      expanded: true,
    }, {
      type: 'image-message',
      author: 'Nick',
      surname: 'Cat',
      header: 'Posted photo',
      text: '"Protein Heroes"',
      preview: 'app/feed/genom.png',
      link: 'https://dribbble.com/shots/2504810-Protein-Heroes',
      time: '16.11.2015',
      ago: '7 days ago',
      expanded: true,
    },
    {
      type: 'text-message',
      author: 'Kostya',
      surname: 'Danovsky',
      header: 'Posted new message',
      text: 'Why did the CoffeeScript developer keep getting lost? Because he couldn\'t find his source without a map',
      time: '18.11.2015',
      ago: '9 days ago',
      expanded: true,
    }
  ];

  getData() {
    return this._data;
  }

  getServerURI() {
    // return AppGlobals.SERVER_URI;
    return localStorage.getItem(ServerUtils.BACK_END_SERVER_URL)
  }
}
