import { Component } from '@angular/core';

import { GlobalState } from '../../../global.state';
import { DocumentService } from '../../../pages/documents/document.service';

// session management
// import { LocalStorageService } from '../../../../../node_modules/angular2-localstorage/LocalStorageEmitter';
// import { LocalStorage, SessionStorage } from '../../../../../node_modules/angular2-localstorage/WebStorage';

@Component({
  selector: 'ba-page-top',
  templateUrl: './baPageTop.html',
  styleUrls: ['./baPageTop.scss']
})
export class BaPageTop {

  public isScrolled: boolean = false;
  public isMenuCollapsed: boolean = false;
  serverURI: string;
  //@SessionStorage() username: string;
  loggedName: string;
  language:string = 'English';

  constructor(private _state: GlobalState, private _documentService: DocumentService) {
    this._state.subscribe('menu.isCollapsed', (isCollapsed) => {
      this.isMenuCollapsed = isCollapsed;
    });
    this.loggedName = localStorage.getItem('username');
    /** 
    if (this.username === '') {
      this.loggedName = this._state.user.name;
    }else if (this._state.user !== null) {
      if (this.username !== this._state.user.name) {
        this.loggedName = this._state.user.name;
      }
    }else {
      this.loggedName = this.username;
    }
    */
    // this.username = this._state.user.name;
    this.serverURI = this._documentService.getServerURI();
    console.log('[Toppage ctor] Session Username is: '.concat(this.loggedName));
  }

  public toggleMenu() {
    this.isMenuCollapsed = !this.isMenuCollapsed;
    this._state.notifyDataChanged('menu.isCollapsed', this.isMenuCollapsed);
    return false;
  }

  public scrolledChanged(isScrolled) {
    this.isScrolled = isScrolled;
  }

  public signOut() {
    console.log('Signing out...');
    localStorage.clear();
  }

  public getLanguage(){
    return this._documentService.getLanguage();
  }

  public changeLanguage(language){
    this._documentService.switchLanguage(language);

  }

  public getCurrentFlag(){
    return this.getFlag(this.getLanguage());
  }

  public isCurrentFlag(language){
    if(this.getLanguage() === language){
      return true;
    }else{
      return false;
    }
  }

  public getFlag(language){
    if(language === 'English'){
      return "flag-icon flag-icon-us flag-icon-squared";
    }

    if(language === 'Thai'){
      return "flag-icon flag-icon-th flag-icon-squared";
    }

    if(language === 'Vietnamese'){
      return "flag-icon flag-icon-vi flag-icon-squared";
    }

    if(language === 'Bahasa'){
      return "flag-icon flag-icon-in flag-icon-squared";
    }
  }

   /**
   * Get the highlight color for the specific hightlight for the menu
   * @param id - the document id to highlight
   */
  public getBackgroundColorHighlight(language: string) {
    if (this.isCurrentFlag(language)) {
      return '#5dd2ff';
    }else {
      return '#fff';
    }
  }
}
