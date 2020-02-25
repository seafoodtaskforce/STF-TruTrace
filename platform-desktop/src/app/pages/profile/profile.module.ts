import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgaModule } from '../../theme/nga.module';

import { ProfileComponent } from './profile.component';
import { routing } from './profile.routing';
import { DocumentService } from '../documents/document.service';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgaModule,
    routing,
  ],
  declarations: [
    ProfileComponent,
  ],
  providers: [
    DocumentService,
  ],
})
export class ProfileModule {}
