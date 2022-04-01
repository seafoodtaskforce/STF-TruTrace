import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';

import { routing }       from './pages.routing';
import { NgaModule } from '../theme/nga.module';
import { AppTranslationModule } from '../app.translation.module';

import { Pages } from './pages.component';
import {SlimLoadingBarModule} from 'ng2-slim-loading-bar';

/**
 * Mapping
 */
import { LeafletMaps } from './leafletMaps/leafletMaps.component';

@NgModule({
  imports: [
    CommonModule, 
    AppTranslationModule, 
    NgaModule, 
    routing,
    SlimLoadingBarModule.forRoot()
  ],
  declarations: [
    Pages,
    LeafletMaps,
    ],
  
})
export class PagesModule {
}
