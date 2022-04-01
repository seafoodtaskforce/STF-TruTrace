import { Routes, RouterModule }  from '@angular/router';
import { Pages } from './pages.component';
import { ModuleWithProviders } from '@angular/core';
// noinspection TypeScriptValidateTypes

// export function loadChildren(path) { return System.import(path); };

// import global data
import * as AppGlobals from '../config/globals';

import { AuthGuardService } from '../utils/auth.guard.service';
import { DocumentsComponent } from './documents/documents.component';
import { LeafletMaps } from './leafletMaps/leafletMaps.component';

export const routes: Routes = [
  { path: '',  
    redirectTo: AppGlobals.LOGIN_PAGE_ROUTE, 
    pathMatch: 'full'  
  },
  {
    path: AppGlobals.LOGIN_PAGE_ROUTE,
    loadChildren: 'app/pages/login/login.module#LoginModule',
  },
  {
    path: AppGlobals.REGISTER_PAGE_ROUTE,
    loadChildren: 'app/pages/register/register.module#RegisterModule',
  },
  {
    path: AppGlobals.PAGES_ROUTE,
    component: Pages, 
    children: [
      { path: '', redirectTo: AppGlobals.LOGIN_PAGE_ROUTE, pathMatch: 'full' },
      { path: 'leafletMaps', component: LeafletMaps },
      { path: AppGlobals.PROFILE_PAGE_ROUTE, loadChildren: './profile/profile.module#ProfileModule'
            , canLoad:[AuthGuardService], canActivate:[AuthGuardService]  },
      { path: AppGlobals.ADMIN_PAGE_ROUTE,  loadChildren: './adminPortal/adminPortal.module#AdminPortalModule'
            , canLoad:[AuthGuardService], canActivate:[AuthGuardService]  },
      { path: AppGlobals.DOCUMENTS_PAGE_ROUTE, 
        loadChildren: './documents/documents.module#DocumentsModule', 
        canLoad:[AuthGuardService], 
        canActivate:[AuthGuardService], 
        canDeactivate: [AuthGuardService] , pathMatch: 'full'
      },
      
    ],
  },
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
