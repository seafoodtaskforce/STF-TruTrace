import { EventEmitter, Provider } from '@angular/core';
import { NgUploaderOptions } from '../classes/ng-uploader-options.class';
export declare class NgUploaderService {
    _queue: any[];
    _emitter: EventEmitter<any>;
    _previewEmitter: EventEmitter<any>;
    _beforeEmitter: EventEmitter<any>;
    opts: NgUploaderOptions;
    constructor();
    setOptions(opts: NgUploaderOptions): void;
    uploadFilesInQueue(): void;
    uploadFile(file: File): void;
    addFilesToQueue(files: File[]): void;
    createFileUrl(file: File): void;
    removeFileFromQueue(i: number): void;
    clearQueue(): void;
    getQueueSize(): number;
    inQueue(file: any): boolean;
    generateRandomIndex(): string;
    humanizeBytes(bytes: number): string;
}
export declare const NgUploaderServiceProvider: Provider;
