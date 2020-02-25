import { Component } from '@angular/core';
import { Routes } from '@angular/router';

import { BaMenuService } from '../theme';
import { PAGES_MENU } from './pages.menu';

import { User } from '../models/user';

// import global data
import * as AppGlobals from '../config/globals';
import { LocaleUtils } from '../utils/LocaleUtils';

@Component({
  selector: 'pages',
  template: `
    <ba-sidebar></ba-sidebar>
    <ba-page-top></ba-page-top>
    <div class="al-main">
      <div class="al-content">
        <ba-content-top></ba-content-top>
        <router-outlet></router-outlet>
      </div>
    </div>
    <footer class="al-footer clearfix">
      <div class="al-footer-main clearfix">
        <div class="al-copy">&copy; <a href="http://republicsystems.com" translate>{{'RepublicSystems'}}</a> 2019 - {{getAppVersion()}}</div>
      </div>
    </footer>
    <ba-back-top position="200"></ba-back-top>
    `,
})
export class Pages {

  currentUser : User;

  constructor(private _menuService: BaMenuService) {
  }

  ngOnInit() {
    this.currentUser = JSON.parse(localStorage.getItem('user'));
    if(this.currentUser.roles[0].name === 'Super Admin'){
      if(PAGES_MENU[0].children.length < 4){
        PAGES_MENU[0].children.push({
          path: 'admin',  // path for our page
          data: { // custom menu declaration
            menu: {
              title: 'Admin', // menu title
              icon: 'ion-gear-a', // menu icon
              pathMatch: 'prefix', // use it if item children not displayed in menu
              selected: false,
              expanded: false,
              order: 0,
            },
          },
        })
      }
      
    }else{
      if(PAGES_MENU[0].children.length >= 4){
        // remove the menu
        PAGES_MENU[0].children.pop()
      }
    }
    this._menuService.updateMenuByRoutes(<Routes>PAGES_MENU);
  }

  getAppVersion(){
    return AppGlobals.APPLICATION_VERSION;
  }
}
