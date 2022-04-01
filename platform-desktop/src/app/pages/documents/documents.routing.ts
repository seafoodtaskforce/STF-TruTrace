import { Routes, RouterModule } from '@angular/router';

import { DocumentsComponent } from './documents.component';
import { LeafletMaps } from './leafletMaps/leafletMaps.component';

const routes: Routes = [
  {
    path: '',
    component: DocumentsComponent, pathMatch: 'full',
    children: [
      { path: 'leafletMaps', component: LeafletMaps },
    ] 
  }
];

export const routing = RouterModule.forChild(routes);