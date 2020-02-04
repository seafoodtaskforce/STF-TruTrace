"use strict";
import { AfterViewChecked, Component, Input, Output, EventEmitter, OnInit, ViewChild, forwardRef, AfterViewInit, ElementRef } from "@angular/core";
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from "@angular/forms";

import { CtrCompleter } from "../directives/ctr-completer";
import { CompleterData } from "../services/completer-data";
import { CompleterService } from "../services/completer-service";
import { CompleterItem } from "./completer-item";
import { MAX_CHARS, MIN_SEARCH_LENGTH, PAUSE, TEXT_SEARCHING, TEXT_NORESULTS } from "../globals";


import "rxjs/add/operator/catch";

const noop = () => { };

const COMPLETER_CONTROL_VALUE_ACCESSOR = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => CompleterCmp),
    multi: true
};


@Component({
    selector: "ng2-completer",
    template: `
        <div class="completer-holder" ctrCompleter>
            <input #ctrInput [attr.id]="inputId.length > 0 ? inputId : null" type="search" class="completer-input" ctrInput [ngClass]="inputClass" [(ngModel)]="searchStr" (ngModelChange)="onChange($event)" [attr.name]="inputName" [placeholder]="placeholder"
                [attr.maxlength]="maxChars" [tabindex]="fieldTabindex" [disabled]="disableInput" [clearSelected]="clearSelected" [overrideSuggested]="overrideSuggested" 
                [fillHighlighted]="fillHighlighted" (blur)="onBlur()" (focus)="onFocus()" autocomplete="off" autocorrect="off" autocapitalize="off" />

            <div class="completer-dropdown-holder" *ctrList="dataService; minSearchLength: minSearchLength; pause: pause; autoMatch: autoMatch; let items = results; let searchActive = searching; let isInitialized = searchInitialized;">
                <div class="completer-dropdown" ctrDropdown *ngIf="isInitialized">
                    <div *ngIf="searchActive && displaySearching" class="completer-searching">{{textSearching}}</div>
                    <div *ngIf="!searchActive && (!items || items.length === 0)" class="completer-no-results">{{textNoResults}}</div>
                    <div class="completer-row-wrapper" *ngFor="let item of items; let rowIndex=index">
                        <div class="completer-row" [ctrRow]="rowIndex" [dataItem]="item">
                            <div *ngIf="item.image || item.image === ''" class="completer-image-holder">
                                <img *ngIf="item.image != ''" src="{{item.image}}" class="completer-image" />
                                <div *ngIf="item.image === ''" class="completer-image-default"></div>
                            </div>
                            <div class="completer-item-text" [ngClass]="{'completer-item-text-image': item.image || item.image === '' }">
                                <completer-list-item class="completer-title" [text]="item.title" [matchClass]="matchClass" [searchStr]="searchStr" [type]="'title'"></completer-list-item>
                                <completer-list-item *ngIf="item.description && item.description != ''" class="completer-description" [text]="item.description"
                                    [matchClass]="matchClass" [searchStr]="searchStr" [type]="'description'">
                                </completer-list-item>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    styles: [`
    .completer-dropdown {
        border-color: #ececec;
        border-width: 1px;
        border-style: solid;
        border-radius: 2px;
        width: 250px;
        padding: 6px;
        cursor: pointer;
        z-index: 9999;
        position: absolute;
        margin-top: -6px;
        background-color: #ffffff;
    }

    .completer-row {
        padding: 5px;
        color: #000000;
        margin-bottom: 4px;
        clear: both;
        display: inline-block;
        width: 103%;
    }

    .completer-selected-row {
        background-color: lightblue;
        color: #ffffff;
    }

    .completer-description {
        font-size: 14px;
    }

    .completer-image-default {
        width: 16px; 
        height: 16px;
        background-image: url("demo/res/img/default.png");
    }

    .completer-image-holder {
        float: left;
        width: 10%;
    }
    .completer-item-text-image {
        float: right;
        width: 90%;
    }
    `],
    providers: [COMPLETER_CONTROL_VALUE_ACCESSOR]
})
export class CompleterCmp implements OnInit, ControlValueAccessor, AfterViewChecked, AfterViewInit {
    @Input() public dataService: CompleterData;
    @Input() public datasource: CompleterData | string | Array<any>;
    @Input() public inputName = "";
    @Input() public inputId: string = "";
    @Input() public pause = PAUSE;
    @Input() public minSearchLength = MIN_SEARCH_LENGTH;
    @Input() public maxChars = MAX_CHARS;
    @Input() public overrideSuggested = false;
    @Input() public clearSelected = false;
    @Input() public fillHighlighted = true;
    @Input() public placeholder = "";
    @Input() public matchClass: string;
    @Input() public textSearching = TEXT_SEARCHING;
    @Input() public textNoResults = TEXT_NORESULTS;
    @Input() public fieldTabindex: number;
    @Input() public autoMatch = false;
    @Input() public disableInput = false;
    @Input() public inputClass: string;
    @Input() public autofocus = false;

    @Output() public selected = new EventEmitter<CompleterItem>();
    @Output() public highlighted = new EventEmitter<CompleterItem>();
    @Output() public blur = new EventEmitter<void>();
    @Output("focus") public focusEvent = new EventEmitter<void>();

    @ViewChild(CtrCompleter) public completer: CtrCompleter;
    @ViewChild("ctrInput") public ctrInput: ElementRef;

    public searchStr = "";
    public control = new FormControl("");

    private displaySearching = true;
    private _onTouchedCallback: () => void = noop;
    private _onChangeCallback: (_: any) => void = noop;
    private _focus: boolean = false;

    constructor(private completerService: CompleterService) { }

    get value(): any { return this.searchStr; };

    set value(v: any) {
        if (v !== this.searchStr) {
            this.searchStr = v;
        }
        // Propagate the change in any case
        this._onChangeCallback(v);
    }

    public ngAfterViewInit() {
        if (this.autofocus) {
            this._focus = true;
        }
    }

    public ngAfterViewChecked(): void {
        if (this._focus) {
            this.ctrInput.nativeElement.focus();
            this._focus = false;
        }
    }

    public onTouched() {
        this._onTouchedCallback();
    }

    public writeValue(value: any) {
        this.searchStr = value;
    }

    public registerOnChange(fn: any) {
        this._onChangeCallback = fn;
    }

    public registerOnTouched(fn: any) {
        this._onTouchedCallback = fn;
    }

    public ngOnInit() {
        if (this.datasource) {
            if (this.datasource instanceof Array) {
                this.dataService = this.completerService.local(this.datasource);
            } else if (typeof (this.datasource) === "string") {
                this.dataService = this.completerService.remote(this.datasource);
            } else {
                this.dataService = this.datasource;
            }
        }
        this.completer.selected.subscribe((item: CompleterItem) => {
            let title = item ? item.title : "";
            this.selected.emit(item);
            this._onChangeCallback(title);
        });
        this.completer.highlighted.subscribe((item: CompleterItem) => {
            this.highlighted.emit(item);
        });

        if (this.textSearching === "false") {
            this.displaySearching = false;
        }
    }

    public onBlur() {
        this.blur.emit();
        this.onTouched();
    }

    public onFocus() {
        this.focusEvent.emit();
        this.onTouched();
    }

    public onChange(value: string) {
    this.value = value;
}

    public open(searchValue = "") {
    this.completer.search(searchValue);
}

    public close() {
    this.completer.clear();
}

    public focus(): void {
    if(this.ctrInput) {
        this.ctrInput.nativeElement.focus();
    } else {
        this._focus = true;
    }
}
}
