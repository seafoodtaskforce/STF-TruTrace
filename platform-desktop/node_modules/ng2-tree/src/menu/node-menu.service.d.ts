import { ElementRef } from '@angular/core';
import { Subject, Observable } from 'rxjs/Rx';
import { NodeMenuEvent, NodeMenuAction } from './menu.events';
export declare class NodeMenuService {
    nodeMenuEvents$: Subject<NodeMenuEvent>;
    fireMenuEvent(sender: HTMLElement, action: NodeMenuAction): void;
    hideMenuStream(treeElementRef: ElementRef): Observable<any>;
    hideMenuForAllNodesExcept(treeElementRef: ElementRef): void;
}
