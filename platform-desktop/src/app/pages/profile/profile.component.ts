import { Component } from '@angular/core';
import { ApplicationErrorData } from 'app/models/applicationErrorData';
import { User } from 'app/models/user';
import { NgUploaderOptions } from 'ngx-uploader';
import { GlobalState } from '../../global.state';
import { DocumentService } from '../documents/document.service';

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
  loggedInUser: User;
  profile: any = {
    picture: this._documentService.getServerURI()
      .concat('/user/profileimage?user_name=')
      .concat(this.username ),
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
  // Profile Data
  firstNameField:string;

  // errors
  newDocumentDynamicFieldDataErrors : ApplicationErrorData[] = new Array<ApplicationErrorData>();

  constructor(private _state: GlobalState, private _documentService: DocumentService) {
      this.username = localStorage.getItem('username');
      console.log('[Profile ctor] Set state '.concat(this.username));
      this.profile = {
        picture: this._documentService.getServerURI()
          .concat('/user/profileimage?user_name=')
          .concat(this.username ),
      };

      this.uploaderOptions = {
        // url: 'http://website.com/upload'
        url: this._documentService.getServerURI().concat('/user/profileimage'),
        allowedExtensions: ['jpg', 'png'],
        data: { userName: this.username },
    
      };
      this.loggedInUser =  JSON.parse(localStorage.getItem('user'))
  }

  


  ngOnInit() {
  }

  isProfileFieldValid(fieldname :string, fieldValue: string) {
    if(fieldname === 'First Name'){
      
    }
  }
}
