import { ElementRef, EventEmitter, OnChanges, SimpleChange } from '@angular/core';
import { NgUploaderService } from '../services/ngx-uploader';
import { NgUploaderOptions, UploadedFile, UploadRejected } from '../classes/index';
export declare class NgFileSelectDirective implements OnChanges {
    el: ElementRef;
    uploader: NgUploaderService;
    options: NgUploaderOptions;
    events: EventEmitter<any>;
    onUpload: EventEmitter<any>;
    onPreviewData: EventEmitter<any>;
    onUploadRejected: EventEmitter<UploadRejected>;
    beforeUpload: EventEmitter<UploadedFile>;
    files: File[];
    constructor(el: ElementRef, uploader: NgUploaderService);
    ngOnChanges(changes: {
        [propName: string]: SimpleChange;
    }): void;
    onChange(): void;
}
