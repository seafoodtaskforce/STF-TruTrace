import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgaModule } from '../../theme/nga.module';
import { Ng2SmartTableModule } from 'ng2-smart-table';
import { DataTableModule } from "angular2-datatable";
import { HttpModule } from "@angular/http";
import { HotTable, HotTableModule } from 'ng2-handsontable';

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
import { NotificationAdminTable } from './notificationAdmin/notificationAdmin.component';
import { StagesAdminTable } from './stagesAdmin';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgaModule,
    routing,
    Ng2SmartTableModule,
    DataTableModule,
    HttpModule,
    HotTableModule
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
    StagesAdminTable,
    NotificationAdminTable
  ],
  providers: [
    DataLoadService
  ]
})
export class AdminPortalModule {
}
