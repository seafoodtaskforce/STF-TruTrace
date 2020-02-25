import { NgModule, ApplicationRef, NO_ERRORS_SCHEMA} from '@angular/core';
import {SlimLoadingBarModule} from 'ng2-slim-loading-bar';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ScrollToModule } from '@nicky-lenaers/ngx-scroll-to';
import { Ng2SmartTableModule } from 'ng2-smart-table';

import { SimpleTimer } from 'ng2-simple-timer';
/*
 * Platform and Environment providers/directives/pipes
 */
import { routing } from './app.routing'; 

// App is our top level component
import { App } from './app.component';
import { AppState, InternalStateType } from './app.service';
import { GlobalState } from './global.state';
import { NgaModule } from './theme/nga.module';
import { PagesModule } from './pages/pages.module';

//
// Multiselect
import { AngularMultiSelectModule } from 'angular4-multiselect-dropdown/angular4-multiselect-dropdown';

// TOASTER
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToasterService } from './toaster-service.service';

// Application wide providers
const APP_PROVIDERS = [
  AppState, 
  GlobalState,
  SimpleTimer,
  ToasterService
];


export type StoreType = {
  state: InternalStateType,
  restoreInputValues: () => void,
  disposeOldHosts: () => void
};

/**
 * `AppModule` is the main entry point into Angular2's bootstraping process
 */
@NgModule({
  bootstrap: [App],
  declarations: [
    App
  ],
  imports: [ // import Angular's modules
  //AngularMultiSelectModule ,
    BrowserModule,
    HttpModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    NgaModule.forRoot(),
    NgbModule.forRoot(),
    ScrollToModule.forRoot(),
    PagesModule,
    routing,
    Ng2SmartTableModule,
    SlimLoadingBarModule.forRoot(),
    AngularMultiSelectModule,
    BrowserAnimationsModule, // required animations module
    
  ],
  providers: [ // expose our Services and Providers into Angular's dependency injection
    APP_PROVIDERS
  ],
  exports: [ScrollToModule]
})

export class AppModule {

  constructor(public appState: AppState) {
  }
}
