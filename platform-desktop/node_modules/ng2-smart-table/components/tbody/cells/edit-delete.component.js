var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Grid } from '../../../lib/grid';
import { Row } from '../../../lib/data-set/row';
var TbodyEditDeleteComponent = (function () {
    function TbodyEditDeleteComponent() {
        this.edit = new EventEmitter();
        this.delete = new EventEmitter();
        this.editRowSelect = new EventEmitter();
    }
    TbodyEditDeleteComponent.prototype.onEdit = function (event) {
        event.preventDefault();
        event.stopPropagation();
        this.editRowSelect.emit(this.row);
        if (this.grid.getSetting('mode') === 'external') {
            this.edit.emit({
                data: this.row.getData(),
                source: this.source,
            });
        }
        else {
            this.grid.edit(this.row);
        }
    };
    TbodyEditDeleteComponent.prototype.onDelete = function (event) {
        event.preventDefault();
        event.stopPropagation();
        if (this.grid.getSetting('mode') === 'external') {
            this.delete.emit({
                data: this.row.getData(),
                source: this.source,
            });
        }
        else {
            this.grid.delete(this.row, this.deleteConfirm);
        }
    };
    return TbodyEditDeleteComponent;
}());
__decorate([
    Input(),
    __metadata("design:type", Grid)
], TbodyEditDeleteComponent.prototype, "grid", void 0);
__decorate([
    Input(),
    __metadata("design:type", Row)
], TbodyEditDeleteComponent.prototype, "row", void 0);
__decorate([
    Input(),
    __metadata("design:type", Object)
], TbodyEditDeleteComponent.prototype, "source", void 0);
__decorate([
    Input(),
    __metadata("design:type", EventEmitter)
], TbodyEditDeleteComponent.prototype, "deleteConfirm", void 0);
__decorate([
    Input(),
    __metadata("design:type", EventEmitter)
], TbodyEditDeleteComponent.prototype, "editConfirm", void 0);
__decorate([
    Output(),
    __metadata("design:type", Object)
], TbodyEditDeleteComponent.prototype, "edit", void 0);
__decorate([
    Output(),
    __metadata("design:type", Object)
], TbodyEditDeleteComponent.prototype, "delete", void 0);
__decorate([
    Output(),
    __metadata("design:type", Object)
], TbodyEditDeleteComponent.prototype, "editRowSelect", void 0);
TbodyEditDeleteComponent = __decorate([
    Component({
        selector: 'ng2-st-tbody-edit-delete',
        template: "\n    <a href=\"#\" *ngIf=\"grid.getSetting('actions.edit')\" class=\"ng2-smart-action ng2-smart-action-edit-edit\"\n        [innerHTML]=\"grid.getSetting('edit.editButtonContent')\" (click)=\"onEdit($event)\"></a>\n    <a href=\"#\" *ngIf=\"grid.getSetting('actions.delete')\" class=\"ng2-smart-action ng2-smart-action-delete-delete\"\n        [innerHTML]=\"grid.getSetting('delete.deleteButtonContent')\" (click)=\"onDelete($event)\"></a>\n  ",
    })
], TbodyEditDeleteComponent);
export { TbodyEditDeleteComponent };
//# sourceMappingURL=edit-delete.component.js.map