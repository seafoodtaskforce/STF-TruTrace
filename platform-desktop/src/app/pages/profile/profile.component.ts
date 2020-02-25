import { Component } from '@angular/core';
import { NgUploaderOptions } from 'ngx-uploader';
import { GlobalState } from '../../global.state';
import { DocumentService } from '../documents/document.service';
import { User } from '../../models/user';

// session management
// import { LocalStorageService } from '../../../../node_modules/angular2-localstorage/LocalStorageEmitter';
// import { LocalStorage, SessionStorage } from '../../../../node_modules/angular2-localstorage/WebStorage';

@Component({
  selector: 'profile',
  templateUrl: './profile.html',
})
export class ProfileComponent {
  defaultPicture = 'assets/img/theme/no-photo.png';
  username: string;
  profile: any = {
    picture: this._documentService.getServerURI()
      .concat('/user/profileimage?user_name=')
      .concat(this.username ),
    // picture: 'assets/img/app/profile/thai_man.png',
  };
  uploaderOptions: NgUploaderOptions = {
    // url: 'http://website.com/upload'
    url: this._documentService.getServerURI().concat('/user/profileimage'),
    allowedExtensions: ['jpg', 'png'],
    data: { userName: this.username },
    // data: { userName: this.username },

  };

  fileUploaderOptions: NgUploaderOptions = {
    // url: 'http://website.com/upload'
    url: '',
  };
  
  constructor(private _state: GlobalState, private _documentService: DocumentService) {
      this.username = localStorage.getItem('username');
      console.log('[Profile ctor] Set state '.concat(this.username));
      this.profile = {
        picture: this._documentService.getServerURI()
          .concat('/user/profileimage?user_name=')
          .concat(this.username ),
        // picture: 'assets/img/app/profile/thai_man.png',
      };

      this.uploaderOptions = {
        // url: 'http://website.com/upload'
        url: this._documentService.getServerURI().concat('/user/profileimage'),
        allowedExtensions: ['jpg', 'png'],
        data: { userName: this.username },
        // data: { userName: this.username },
    
      };
  }

  ngOnInit() {
  }
}
