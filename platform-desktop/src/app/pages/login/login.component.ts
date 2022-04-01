import { Component, OnInit } from '@angular/core';
import { FormGroup, AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GlobalState } from '../../global.state';
import { Injectable } from '@angular/core';
import { LocaleUtils } from '../../utils/locale.utils';

// session management
// import { LocalStorageService } from '../../../../node_modules/angular2-localstorage/LocalStorageEmitter';
// import { LocalStorage, SessionStorage } from '../../../../node_modules/angular2-localstorage/WebStorage';

// REST Functionality
import { LoginService } from './login.service';

// Data Model
import { AuthCredentials } from '../../models/AuthCredentials';
import { User } from '../../models/user';


@Component({
  selector: 'login',
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})

@Injectable()
export class Login implements OnInit {

  credentials = new AuthCredentials();
  currUser: User;
  showLoginAlert: boolean= false;
  public form: FormGroup;
  public email: AbstractControl;
  public password: AbstractControl;
  public submitted: boolean = false;
  // @SessionStorage() username: string= '';

  constructor(fb: FormBuilder, private router: Router
              , private _state: GlobalState
            , private _loginService: LoginService) {

        this.form = fb.group({
            'email': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
            'password': ['', Validators.compose([Validators.required, Validators.minLength(3)])],
        });

        // session storage prep

        this.email = this.form.controls['email'];
        this.password = this.form.controls['password'];
        // console.log('[Login ctor] Session Username is: '.concat(this.username));

  }

  public onSubmit(values: Object): void {
    this.submitted = true;
    if (this.form.valid) {
      //
      // create temp user
      this._state.user = new User();
      this._state.user.id = 0;
      this._state.user.name = values['email'];
      //
      // get the credentials from the backend
      this.getCredentials(this._state.user, values['password']);
      //
      // 
      console.log(values);
      
    }
  }

  ngOnInit() {
  }

 getCredentials(user: User, inPassword: string) {
    this._loginService.getCredentials(user, inPassword)
      .subscribe( 
          (response) => {
            //Here you can map the response to a type.
            //this.credentials = <AuthCredentials>response.json();
            console.log('[Login getCredentials -- in getCredentials#1] POST RESTFUL '
            .concat(JSON.stringify(response.json())));
            //let foo: AuthCredentials = Object.assign(new AuthCredentials(), JSON.parse(response.json()));
            //console.log('[Login submit -- in getCredentials#2] POST RESTFUL '
            //.concat(JSON.stringify(foo)));
            this.credentials  = response.json();
            console.log('[Login getCredentials -- in getCredentials#2] POST RESTFUL '
            .concat(JSON.stringify(this.credentials.credentials.token)));

            this.currUser = response.json();
            // make the username lower case
            this.currUser.name = this.currUser.name.toLowerCase()
            console.log('[Login currentUser -- in getCredentials#3] POST RESTFUL '
            .concat(JSON.stringify(this.currUser)));
            
            if(this.credentials.credentials.token) {
              console.log('Logging in...');
              //
              // 
              this.showLoginAlert = false;
              localStorage.setItem('username', this.currUser.name);
              localStorage.setItem('user', JSON.stringify(this.currUser));
              //
              // Set the App Resources
              LocaleUtils.loadResourceMap(this.currUser.appResources);
              console.log('[Login currentUser] -- all APP RESOURCES: '.concat(JSON.stringify(this.currUser.appResources)));

              //
              // Navigate to the main page
              this.router.navigateByUrl('/pages/documents');
            }else {
              console.log('Try again');
              this.showLoginAlert = true;
              localStorage.setItem('username', '');
            }
          },
          (err) => {
            console.log('Server Error');
            this.showLoginAlert = true;
            localStorage.setItem('username', '');
          },
    );
  }
}

