
import { Injectable } from '@angular/core';
import { Router, CanLoad, Route, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate } from '@angular/router';
import { User } from '../models/user';
// import global data
import * as AppGlobals from '../config/globals';
import { Observable } from 'rxjs';
import { DocumentsComponent } from 'app/pages/documents/documents.component';


@Injectable()
export class AuthGuardService implements CanLoad, CanActivate, CanDeactivate<DocumentsComponent>  {
  canDeactivate(component: DocumentsComponent, currentRoute: ActivatedRouteSnapshot, currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot): boolean | Observable<boolean> | Promise<boolean> {
    //console.log('[AuthGuardService] <canDeactivate> <DocumentsComponent> show detail flag: '.concat(''+component.showDocuemntDetailsflag));
    //component.showDocuemntDetailsflag = false;
    return true;
  }

  canActivate(route: ActivatedRouteSnapshot,
              state: RouterStateSnapshot
              ): Observable<boolean>|Promise<boolean>|boolean {

    let user: User = null;
    //determine whether you want to load the module
    //return true or false
    user =  JSON.parse(localStorage.getItem('user'));
    //
    // Are we logged in?
    if(user == null) {
      this.router.navigate([AppGlobals.LOGIN_PAGE_ROUTE]);
    }
    return true;
  }

  constructor(private router: Router) {
  }
 
  canLoad(route: Route): boolean {
    let user: User = null;
    let url: string = route.path;
    
    //determine whether you want to load the module
    //return true or false
    user =  JSON.parse(localStorage.getItem('user'));
    //
    // Are we logged in?
    if(user == null) {
      return false;
    }
    console.log('[Auth Guard Service] User Data '.concat(localStorage.getItem('username')));
    //
    // 
    if (url==AppGlobals.ADMIN_PAGE_ROUTE){
      if(user.roles[0].id != AppGlobals.ROLE_ADMIN 
            && user.roles[0].id != AppGlobals.ROLE_SUPER_ADMIN
            && user.roles[0].id != AppGlobals.ROLE_ORG_ADMIN
            && user.roles[0].id != AppGlobals.ROLE_MATRIX_ADMIN){
        alert('You are not authorised to visit this page');
        return false;
      }
    }
    return true; 
  }
} 