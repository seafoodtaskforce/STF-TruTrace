import {Component} from '@angular/core';
import {FormGroup, AbstractControl, FormBuilder, Validators} from '@angular/forms';
import {EmailValidator, EqualPasswordsValidator} from '../../theme/validators';
import { User } from 'app/models/user';
import { UserContact } from 'app/models/userContact';
import { PasswordCredentials } from 'app/models/passwordCredentials';
import { GlobalState } from 'app/global.state';
import { Router } from '@angular/router';
import { RegisterService } from './register.service';
import { DocumentService } from 'app/pages/documents/document.service';
import { Group } from 'app/models/group';

@Component({
  selector: 'register',
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class Register {

  public static readonly REGISTRATION_TYPE_INDIVIDUAL = "individual";
  public static readonly REGISTRATION_TYPE_ORGANIZATION = "organization";

  // The form envelope
  public formUser:FormGroup;
  public formOrganization:FormGroup;

  // the registration process
  public errorMessage : string = '';
  public isRegistering : boolean = false;
  public registrationProcessMessage : string = '';
  public registrationType : string = Register.REGISTRATION_TYPE_INDIVIDUAL;

  // Fields for the form Individual
  public firstName:AbstractControl;
  public lastName:AbstractControl;
  public username:AbstractControl;
  public email:AbstractControl;
  public organization:AbstractControl;
  public password:AbstractControl;
  public repeatPassword:AbstractControl;
  public passwords:FormGroup;

  // Fields for the form Organization
  public organizationFirstName:AbstractControl;
  public organizationLastName:AbstractControl;
  public organizationDisplayName:AbstractControl;
  public organizationEmail:AbstractControl;
  public organizationLegalName:AbstractControl;
  public organizationBusinessNumber:AbstractControl;
  public organizationGPS:AbstractControl;
  public organizationAddress:AbstractControl;
  public organizationStage:AbstractControl;


  // submitted form
  public submitted:boolean = false;

  constructor(fb: FormBuilder, private router: Router, private _state: GlobalState , 
    private _registrationService: RegisterService, private _documentService: DocumentService, ) {
    //
    // User Registration form
    // 
    this.formUser = fb.group({

      'firstName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'lastName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'username': ['', Validators.compose([Validators.required, Validators.minLength(6)])],
      'email': ['', Validators.compose([Validators.required, EmailValidator.validate])],
      'organization': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'passwords': fb.group({
        'password': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
        'repeatPassword': ['', Validators.compose([Validators.required, Validators.minLength(4)])]
      }, {validator: EqualPasswordsValidator.validate('password', 'repeatPassword')})
    });

    this.firstName = this.formUser.controls['firstName'];
    this.lastName = this.formUser.controls['lastName'];
    this.username = this.formUser.controls['username'];
    this.organization = this.formUser.controls['organization'];
    this.email = this.formUser.controls['email'];
    this.passwords = <FormGroup> this.formUser.controls['passwords'];
    this.password = this.passwords.controls['password'];
    this.repeatPassword = this.passwords.controls['repeatPassword'];

    //
    // Organization Registration Form
    //
    this.formOrganization = fb.group({

      'organizationFirstName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'organizationLastName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'organizationDisplayName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'organizationLegalName': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'organizationBusinessNumber': ['', Validators.compose([Validators.required, Validators.minLength(2)])],
      'organizationGPS': ['', Validators.compose([Validators.required, Validators.minLength(6)])],
      'organizationAddress': ['', Validators.compose([Validators.required, Validators.minLength(6)])],
      'organizationEmail': ['', Validators.compose([Validators.required, EmailValidator.validate])],
      'organizationStage': ['', Validators.compose([Validators.required, Validators.minLength(2)])]
    });

    this.organizationFirstName = this.formOrganization.controls['organizationFirstName'];
    this.organizationLastName = this.formOrganization.controls['organizationLastName'];
    this.organizationDisplayName = this.formOrganization.controls['organizationDisplayName'];
    this.organizationLegalName = this.formOrganization.controls['organizationLegalName'];
    this.organizationBusinessNumber = this.formOrganization.controls['organizationBusinessNumber'];
    this.organizationEmail = this.formOrganization.controls['organizationEmail'];
    this.organizationGPS = this.formOrganization.controls['organizationGPS'];
    this.organizationAddress = this.formOrganization.controls['organizationAddress'];
    this.organizationStage = this.formOrganization.controls['organizationStage'];
  }

  public onSubmit(values:Object):void {
    
    //
    // Submit User Resgitration
    //
    if(this.registrationType == Register.REGISTRATION_TYPE_INDIVIDUAL){
      if (this.formUser.valid) {
        // your code goes here
        this.isRegistering = true;
        this.submitted = true;
  
        //
        //
        // build user data
        //
        var user : User = new User();
        user.name = this.username.value;
        //
        // contact 
        var contact : UserContact = new UserContact();
        contact.emailAddress = this.email.value;
        contact.firstName = this.firstName.value;
        contact.lastName = this.lastName.value;
        user.contactInfo = contact;
        //
        // credentials
        var credentials: PasswordCredentials = new PasswordCredentials();
        credentials.username = this.username.value;
        credentials.password = this.password.value;
        user.credentials = credentials;
  
        console.log("New User <register>".concat(JSON.stringify(user)));
  
        //
        // message the process to the UI
        this.registrationProcessMessage = "Registering...";
  
        //
        // do the actual remote registration
        this.registerUser(user,this.organization.value);
  
  
        console.log(values);
      } else {
        this.isRegistering = false;
        console.log("Form is not Valid <register>");
      }
    }

    //
    // Submit Org Registration
    //
    if(this.registrationType == Register.REGISTRATION_TYPE_ORGANIZATION){
      if (this.formOrganization.valid) {
        this.isRegistering = true;
        this.submitted = true;

        var user : User = new User();
        user.name = this.organizationEmail.value;
        //
        // contact 
        var contact : UserContact = new UserContact();
        contact.emailAddress = this.organizationEmail.value;
        contact.firstName = this.organizationFirstName.value;
        contact.lastName = this.organizationLastName.value;
        user.contactInfo = contact;
        //
        // credentials
        var credentials: PasswordCredentials = new PasswordCredentials();
        credentials.username = this.username.value;
        credentials.password = this.organizationEmail.value;
        user.credentials = credentials;

        //
        // Build Org Data
        //
        var orgGroup : Group = new Group();
        orgGroup.name = this.organizationDisplayName.value;
        orgGroup.emailAddress =  this.organizationEmail.value;
        orgGroup.legalBusinessName = this.organizationLegalName.value;
        orgGroup.businessIDNumber = this.organizationBusinessNumber.value;
        orgGroup.gpsCoordinates = this.organizationGPS.value;
        orgGroup.businessAddress = this.organizationAddress.value;
        orgGroup.users.push(user);

        console.log("New Org <register>".concat(JSON.stringify(orgGroup)));
  
        //
        // message the process to the UI
        this.registrationProcessMessage = "Registering...";
  
        //
        // do the actual remote registration
        this.registerOrganization(orgGroup, this.organizationStage.value);
  
  
        console.log(values);
      } else {
        this.isRegistering = false;
        console.log("Form is not Valid <register>");
      }
    }
    
  }

  public getRegistrationMessage() {
    
  }

  public isDataValid() {
    var result : boolean = true;

    //
    // User Registration validation
    //
    if(this.isRegistrationForIndividual()){
      //
      // email validation
      if (!this.email.valid 
        && this.email.touched 
        && this.email.value.length > 0) {

        this.errorMessage = "Please check your email address.";
        result = false;
      }

      //
      // Password validation
      if (!this.passwords.valid 
            && (this.password.touched || this.repeatPassword.touched)
            && (this.password.value.length != 0 && this.repeatPassword.value.length !=0)) {
        this.errorMessage = "Passwords don't match.";
        result = false;
      }
    } // end user registration

    if(this.isRegistrationForOrganization()){
      //
      // email validation
      if (!this.organizationEmail.valid 
        && this.organizationEmail.touched 
        && this.organizationEmail.value.length > 0) {

        this.errorMessage = "Please check your email address.";
        result = false;
      }
    }
    

    return result;
  }

  /**
   * User registration method. It will send a backend call to resgiter the user to the given organization.
   * @param user  - the user to b registered
   * @param organization - the organization to which this user will be registered to 
   */
  registerUser(user: User, organization : string) {
    this._registrationService.registerUser(user, organization)
      .subscribe( 
          (response) => {
            //Here you can map the response to a type.
            //Here you can map the response to a type.
            if(response.status == 202) {
                
            } else {
              this.formUser.reset();
            }
            this.registrationProcessMessage = response.json();
            
          },
          (err) => {
            console.log('Server Error');
            this.registrationProcessMessage = "There was an error. Please try again.";

          },
    );
  }

    /**
   * Organization registration method. It will send a backend call to register the user for the given stage.
   * @param user  - the user to b registered
   * @param organization - the organization to which this user will be registered to 
   */
     registerOrganization(org: Group, stageName : string) {
      this._registrationService.registerOrgGroup(org, stageName)
        .subscribe( 
            (response) => {
              //Here you can map the response to a type.
              if(response.status == 202) {
                
              } else {
                this.formOrganization.reset();
              }
              this.registrationProcessMessage = response.json();
              
            },
            (err) => {
              console.log('Server Error');
              this.registrationProcessMessage = "There was an error. Please try again.";
  
            },
      );
    }

  isRegistrationForIndividual() {
    return Register.REGISTRATION_TYPE_INDIVIDUAL === this.registrationType;
  }

  isRegistrationForOrganization() {
    return Register.REGISTRATION_TYPE_ORGANIZATION === this.registrationType;
  }

  /** Internationalization */
  getInternationalizedToken(token: string){
    return this._documentService.internationalizeString(token);
  }

  setRegistrationType(type : string) {
    this.registrationType = type;
  }

  clearOrgRegistrationForm(){

  }
}
