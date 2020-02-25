import { Routes, RouterModule } from '@angular/router';

import { Tables } from './tables.component';
import { SmartTables } from './smartTables.component';

// noinspection TypeScriptValidateTypes
const routes: Routes = [
  {
    path: '',
    component: SmartTables,
  }
]

export const routing = RouterModule.forChild(routes);
