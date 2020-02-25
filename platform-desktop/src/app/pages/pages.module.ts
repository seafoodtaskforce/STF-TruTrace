import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';

import { routing }       from './pages.routing';
import { NgaModule } from '../theme/nga.module';
import { AppTranslationModule } from '../app.translation.module';

import { Pages } from './pages.component';
import {SlimLoadingBarModule} from 'ng2-slim-loading-bar';


@NgModule({
  imports: [
    CommonModule, 
    AppTranslationModule, 
    NgaModule, 
    routing,
    SlimLoadingBarModule.forRoot(),
  ],
  declarations: [Pages]
})
export class PagesModule {
}
