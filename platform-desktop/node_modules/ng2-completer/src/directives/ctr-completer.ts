import { Directive, EventEmitter, OnInit, Output } from "@angular/core";

import { CompleterItem } from "../components/completer-item";

export interface CompleterList {
    search(term: string): void;
    clear(): void;
}

export interface CompleterDropdown {
    clear(): void;
    selectCurrent(): void;
    nextRow(): void;
    prevRow(): void;
}

@Directive({
    selector: "[ctrCompleter]",
})
export class CtrCompleter implements OnInit {
    @Output() public selected = new EventEmitter<CompleterItem>();
    @Output() public highlighted = new EventEmitter<CompleterItem>();

    private list: CompleterList;
    private dropdown: CompleterDropdown;
    private _hasHighlited = false;
    private hasSelected = false;
    private _cancelBlur = false;

    constructor() { }

    public ngOnInit() {
        //
    }

    public registerList(list: CompleterList) {
        this.list = list;
    }

    public registerDropdown(dropdown: CompleterDropdown) {
        this.dropdown = dropdown;
    }

    public onHighlighted(item: CompleterItem) {
        this.highlighted.emit(item);
        this._hasHighlited = !!item;
    }

    public onSelected(item: CompleterItem) {
        this.selected.emit(item);
        if (item) {
            this.hasSelected = true;
        }
        this.clear();
    }

    public search(term: string) {
        if (this.hasSelected) {
            this.selected.emit(null);
            this.hasSelected = false;
        }
        if (this.list) {
            this.list.search(term);
        }
    }

    public clear() {
        if (this.dropdown) {
            this.dropdown.clear();
        }
        if (this.list) {
            this.list.clear();
        }
        this._hasHighlited = false;
    }

    public selectCurrent() {
        if (this.dropdown) {
            this.dropdown.selectCurrent();
        }
    }

    public nextRow() {
        if (this.dropdown) {
            this.dropdown.nextRow();
        }
    }

    public prevRow() {
        if (this.dropdown) {
            this.dropdown.prevRow();
        }
    }

    public hasHighlited() {
        return this._hasHighlited;
    }

    public cancelBlur(cancel: boolean) {
        this._cancelBlur = cancel;
    }

    public isCancelBlur() {
        return this._cancelBlur;
    }
}
