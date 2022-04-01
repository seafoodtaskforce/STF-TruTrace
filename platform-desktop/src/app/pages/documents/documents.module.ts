import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgaModule } from '../../theme/nga.module';

import { DocumentsComponent } from './documents.component';
import { routing } from './documents.routing';

import { InterComponentDataService } from "../../utils/inter.component.data.service";
import { DocumentService } from './document.service';
import {SlimLoadingBarModule} from 'ng2-slim-loading-bar';


// Pipes
import { OrderModule } from 'ngx-order-pipe';
import { FilterPipeModule } from 'ngx-filter-pipe';
import { DatePipe } from '@angular/common';

//
// Multiselect
import { AngularMultiSelectModule } from 'angular4-multiselect-dropdown/angular4-multiselect-dropdown';

//
// Tag Admin
import { DataLoadService } from '../adminPortal/dataLoad.service';
import { Ng2SmartTableModule } from 'ng2-smart-table';

// Poppers
import {NgxPopperModule} from 'ngx-popper';
import { ToasterService } from '../../toaster-service.service';

//
// New Lightbox with rotation
import { NgxGalleryModule } from 'ngx-gallery';

//
// Context Menu
import { ContextMenuModule } from '../../../../node_modules/angular2-contextmenu';

/**
 * Drag and Drop
 */
import { DragulaModule , DragulaService} from 'ng2-dragula/ng2-dragula';

/**
 * Mapping
 */
import { LeafletMaps } from './leafletMaps/leafletMaps.component';



/**
 * Image Viewing
 */
 import { ImageViewerModule } from "ngx-image-viewer";


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgaModule,
    routing,
    OrderModule,
    FilterPipeModule,
    SlimLoadingBarModule.forRoot(),
    AngularMultiSelectModule,
    Ng2SmartTableModule,
    NgxGalleryModule,
    ContextMenuModule,
    DragulaModule,
    ImageViewerModule.forRoot(),
    
    ],
  declarations: [
    DocumentsComponent,
    LeafletMaps,
  ],
  providers: [
    DocumentService,
    DatePipe,
    DataLoadService,
    ToasterService,
    DragulaService
    //InterComponentDataService
  ],
})
export class DocumentsModule {}
