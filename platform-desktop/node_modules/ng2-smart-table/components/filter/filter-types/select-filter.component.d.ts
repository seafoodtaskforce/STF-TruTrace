import { OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { DefaultFilter } from './default-filter';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/debounceTime';
export declare class SelectFilterComponent extends DefaultFilter implements OnInit {
    inputControl: FormControl;
    constructor();
    ngOnInit(): void;
}
