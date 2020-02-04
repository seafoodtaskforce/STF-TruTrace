var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { Component, Input, Output, EventEmitter, ElementRef } from '@angular/core';
import { Grid } from '../../../lib/grid';
var AddButtonComponent = (function () {
    function AddButtonComponent(ref) {
        this.ref = ref;
        this.create = new EventEmitter();
    }
    AddButtonComponent.prototype.ngAfterViewInit = function () {
        this.ref.nativeElement.classList.add('ng2-smart-actions-title', 'ng2-smart-actions-title-add');
    };
    AddButtonComponent.prototype.onAdd = function (event) {
        event.preventDefault();
        event.stopPropagation();
        if (this.grid.getSetting('mode') === 'external') {
            this.create.emit({
                source: this.source,
            });
        }
        else {
            this.grid.createFormShown = true;
        }
    };
    return AddButtonComponent;
}());
__decorate([
    Input(),
    __metadata("design:type", Grid)
], AddButtonComponent.prototype, "grid", void 0);
__decorate([
    Input(),
    __metadata("design:type", Object)
], AddButtonComponent.prototype, "source", void 0);
__decorate([
    Output(),
    __metadata("design:type", Object)
], AddButtonComponent.prototype, "create", void 0);
AddButtonComponent = __decorate([
    Component({
        selector: '[ng2-st-add-button]',
        template: "\n    <a *ngIf=\"grid.getSetting('actions.add')\" href=\"#\" class=\"ng2-smart-action ng2-smart-action-add-add\"\n        [innerHTML]=\"grid.getSetting('add.addButtonContent')\" (click)=\"onAdd($event)\"></a>\n  ",
    }),
    __metadata("design:paramtypes", [ElementRef])
], AddButtonComponent);
export { AddButtonComponent };
//# sourceMappingURL=add-button.component.js.map