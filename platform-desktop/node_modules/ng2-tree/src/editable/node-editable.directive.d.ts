import { ElementRef, OnInit, EventEmitter, Renderer } from '@angular/core';
import { NodeEditableEvent } from './editable.events';
export declare class NodeEditableDirective implements OnInit {
    private renderer;
    private elementRef;
    nodeValue: string;
    valueChanged: EventEmitter<NodeEditableEvent>;
    constructor(renderer: Renderer, elementRef: ElementRef);
    ngOnInit(): void;
    applyNewValue(newNodeValue: string): void;
    applyNewValueByLoosingFocus(newNodeValue: string): void;
    cancelEditing(): void;
}
