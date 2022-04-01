import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgaModule } from '../../theme/nga.module';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { DataTableModule } from "angular2-datatable";
import { HttpModule } from "@angular/http";
import { HotTable, HotTableModule } from 'ng2-handsontable';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { routing } from './tables.routing';
import { Tables } from './tables.component';
import { SmartTables } from './smartTables.component';


import { DataLoadService } from './dataLoad.service';

// Admin
import { OrganizationAdminTable } from './organizationAdmin/organizationAdmin.component';
import { OrganizationTypeAdminTable } from './organizationTypeAdmin/organizationTypeAdmin.component';
import { UserAdminTable } from './userAdmin/userAdmin.component';
import { DocTypeAdminTable } from './docTypeAdmin/docTypeAdmin.component';
import { OrgUserAdminTable } from './orgUserAdmin/orgUseradmin.component';
import { TagAdminTable } from './tagAdmin/tagAdmin.component';
import { ResourceAdminTable } from './resourceAdmin/resourceAdminTable';
import { DocExportAdminTable } from './docExportAdmin/docExportAdminTable';

import { CKEditorModule } from 'ng2-ckeditor';

import { NotificationAdminTable } from './notificationAdmin/notificationAdmin.component';
import { DynamicFieldAdminTable } from './dynamicFieldAdmin/dynamicFieldAdmin.component';
import { StagesAdminTable } from './stagesAdmin';
import { Ckeditor } from './resourceAdmin/editor/ckeditor.component';
/**
 * Drag and Drop
 */
import { DragulaModule , DragulaService} from 'ng2-dragula/ng2-dragula';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgaModule,
    NgbModule.forRoot(),
    routing,
    Ng2SmartTableModule,
    CKEditorModule,
    DataTableModule,
    HttpModule,
    HotTableModule,
    DragulaModule
  ],
  declarations: [
    Tables,
    SmartTables,
    // Admin
    UserAdminTable,
    OrganizationAdminTable,
    OrganizationTypeAdminTable,
    DocTypeAdminTable,
    OrgUserAdminTable,
    TagAdminTable,
    ResourceAdminTable,
    DocExportAdminTable, 
    StagesAdminTable,
    NotificationAdminTable,
    DynamicFieldAdminTable,
    Ckeditor,

  ],
  providers: [
    DataLoadService,
    DragulaService
  ]
})
export class AdminPortalModule {
}
