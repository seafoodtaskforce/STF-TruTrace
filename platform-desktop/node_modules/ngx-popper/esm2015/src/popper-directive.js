/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
import { Directive, ViewContainerRef, ComponentFactoryResolver, Input, Output, EventEmitter, Renderer2, ChangeDetectorRef, Inject, ElementRef } from '@angular/core';
import { Placements, Triggers } from './popper-model';
import { PopperContent } from './popper-content';
export class PopperController {
    /**
     * @param {?} viewContainerRef
     * @param {?} changeDetectorRef
     * @param {?} resolver
     * @param {?} elementRef
     * @param {?} renderer
     * @param {?=} popperDefaults
     */
    constructor(viewContainerRef, changeDetectorRef, resolver, elementRef, renderer, popperDefaults = {}) {
        this.viewContainerRef = viewContainerRef;
        this.changeDetectorRef = changeDetectorRef;
        this.resolver = resolver;
        this.elementRef = elementRef;
        this.renderer = renderer;
        this.popperDefaults = popperDefaults;
        this.popperContentClass = PopperContent;
        this.shown = false;
        this.subscriptions = [];
        this.eventListeners = [];
        this.globalEventListeners = [];
        this.hideTimeout = 0;
        this.timeoutAfterShow = 0;
        this.popperOnShown = new EventEmitter();
        this.popperOnHidden = new EventEmitter();
        this.popperOnUpdate = new EventEmitter();
        PopperController.baseOptions = Object.assign({}, PopperController.baseOptions, this.popperDefaults);
    }
    /**
     * @param {?} $event
     * @return {?}
     */
    hideOnClickOutsideHandler($event) {
        if (this.disabled || !this.hideOnClickOutside || $event.srcElement &&
            $event.srcElement === this.popperContent.elemRef.nativeElement ||
            this.popperContent.elemRef.nativeElement.contains($event.srcElement)) {
            return;
        }
        this.scheduledHide($event, this.hideTimeout);
    }
    /**
     * @param {?} $event
     * @return {?}
     */
    hideOnScrollHandler($event) {
        if (this.disabled || !this.hideOnScroll) {
            return;
        }
        this.scheduledHide($event, this.hideTimeout);
    }
    /**
     * @return {?}
     */
    applyTriggerListeners() {
        switch (this.showTrigger) {
            case Triggers.CLICK:
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'click', this.toggle.bind(this)));
                break;
            case Triggers.MOUSEDOWN:
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'mousedown', this.toggle.bind(this)));
                break;
            case Triggers.HOVER:
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'mouseenter', this.scheduledShow.bind(this, this.showDelay)));
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'touchend', this.scheduledHide.bind(this, null, this.hideTimeout)));
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'touchcancel', this.scheduledHide.bind(this, null, this.hideTimeout)));
                this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'mouseleave', this.scheduledHide.bind(this, null, this.hideTimeout)));
                break;
        }
        if (this.showTrigger !== Triggers.HOVER && this.hideOnMouseLeave) {
            this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'touchend', this.scheduledHide.bind(this, null, this.hideTimeout)));
            this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'touchcancel', this.scheduledHide.bind(this, null, this.hideTimeout)));
            this.eventListeners.push(this.renderer.listen(this.elementRef.nativeElement, 'mouseleave', this.scheduledHide.bind(this, null, this.hideTimeout)));
        }
    }
    /**
     * @param {?} target
     * @param {...?} sources
     * @return {?}
     */
    static assignDefined(target, ...sources) {
        for (const source of sources) {
            for (const key of Object.keys(source)) {
                /** @type {?} */
                const val = source[key];
                if (val !== undefined) {
                    target[key] = val;
                }
            }
        }
        return target;
    }
    /**
     * @return {?}
     */
    ngOnInit() {
        //Support legacy prop
        this.hideOnClickOutside = typeof this.hideOnClickOutside === 'undefined' ?
            this.closeOnClickOutside : this.hideOnClickOutside;
        if (typeof this.content === 'string') {
            /** @type {?} */
            const text = this.content;
            this.popperContent = this.constructContent();
            this.popperContent.text = text;
        }
        else {
            this.popperContent = this.content;
        }
        /** @type {?} */
        const popperRef = this.popperContent;
        popperRef.referenceObject = this.getRefElement();
        this.setContentProperties(popperRef);
        this.setDefaults();
        this.applyTriggerListeners();
        if (this.showOnStart) {
            this.scheduledShow();
        }
    }
    /**
     * @param {?} changes
     * @return {?}
     */
    ngOnChanges(changes) {
        if (changes['popperDisabled'] && changes['popperDisabled'].currentValue) {
            this.hide();
        }
        if (changes['content']
            && !changes['content'].firstChange
            && typeof changes['content'].currentValue === 'string') {
            this.popperContent.text = changes['content'].currentValue;
        }
        if (changes['applyClass']
            && !changes['applyClass'].firstChange
            && typeof changes['applyClass'].currentValue === 'string') {
            this.popperContent.popperOptions.applyClass = changes['applyClass'].currentValue;
        }
        if (changes['applyArrowClass']
            && !changes['applyArrowClass'].firstChange
            && typeof changes['applyArrowClass'].currentValue === 'string') {
            this.popperContent.popperOptions.applyArrowClass = changes['applyArrowClass'].currentValue;
        }
    }
    /**
     * @return {?}
     */
    ngOnDestroy() {
        this.subscriptions.forEach(sub => sub.unsubscribe && sub.unsubscribe());
        this.subscriptions.length = 0;
        this.clearEventListeners();
        this.clearGlobalEventListeners();
        clearTimeout(this.scheduledShowTimeout);
        clearTimeout(this.scheduledHideTimeout);
        this.popperContent && this.popperContent.clean();
    }
    /**
     * @return {?}
     */
    toggle() {
        if (this.disabled) {
            return;
        }
        this.shown ? this.scheduledHide(null, this.hideTimeout) : this.scheduledShow();
    }
    /**
     * @return {?}
     */
    show() {
        if (this.shown) {
            this.overrideHideTimeout();
            return;
        }
        this.shown = true;
        /** @type {?} */
        const popperRef = this.popperContent;
        /** @type {?} */
        const element = this.getRefElement();
        if (popperRef.referenceObject !== element) {
            popperRef.referenceObject = element;
        }
        this.setContentProperties(popperRef);
        popperRef.show();
        this.popperOnShown.emit(this);
        if (this.timeoutAfterShow > 0) {
            this.scheduledHide(null, this.timeoutAfterShow);
        }
        this.globalEventListeners.push(this.renderer.listen('document', 'touchend', this.hideOnClickOutsideHandler.bind(this)));
        this.globalEventListeners.push(this.renderer.listen('document', 'click', this.hideOnClickOutsideHandler.bind(this)));
        this.globalEventListeners.push(this.renderer.listen(this.getScrollParent(this.getRefElement()), 'scroll', this.hideOnScrollHandler.bind(this)));
    }
    /**
     * @return {?}
     */
    hide() {
        if (this.disabled) {
            return;
        }
        if (!this.shown) {
            this.overrideShowTimeout();
            return;
        }
        this.shown = false;
        if (this.popperContentRef) {
            this.popperContentRef.instance.hide();
        }
        else {
            this.popperContent.hide();
        }
        this.popperOnHidden.emit(this);
        this.clearGlobalEventListeners();
    }
    /**
     * @param {?=} delay
     * @return {?}
     */
    scheduledShow(delay = this.showDelay) {
        if (this.disabled) {
            return;
        }
        this.overrideHideTimeout();
        this.scheduledShowTimeout = setTimeout(() => {
            this.show();
            this.applyChanges();
        }, delay);
    }
    /**
     * @param {?=} $event
     * @param {?=} delay
     * @return {?}
     */
    scheduledHide($event = null, delay = this.hideTimeout) {
        if (this.disabled) {
            return;
        }
        this.overrideShowTimeout();
        this.scheduledHideTimeout = setTimeout(() => {
            /** @type {?} */
            const toElement = $event ? $event.toElement : null;
            /** @type {?} */
            const popperContentView = this.popperContent.popperViewRef ? this.popperContent.popperViewRef.nativeElement : false;
            if (!popperContentView || popperContentView === toElement || popperContentView.contains(toElement) || ((/** @type {?} */ (this.content))).isMouseOver) {
                return;
            }
            this.hide();
            this.applyChanges();
        }, delay);
    }
    /**
     * @return {?}
     */
    getRefElement() {
        return this.targetElement || this.viewContainerRef.element.nativeElement;
    }
    /**
     * @private
     * @return {?}
     */
    applyChanges() {
        this.changeDetectorRef.markForCheck();
        this.changeDetectorRef.detectChanges();
    }
    /**
     * @private
     * @return {?}
     */
    setDefaults() {
        this.showDelay = typeof this.showDelay === 'undefined' ? PopperController.baseOptions.showDelay : this.showDelay;
        this.showTrigger = typeof this.showTrigger === 'undefined' ? PopperController.baseOptions.trigger : this.showTrigger;
        this.hideOnClickOutside = typeof this.hideOnClickOutside === 'undefined' ? PopperController.baseOptions.hideOnClickOutside : this.hideOnClickOutside;
        this.hideOnScroll = typeof this.hideOnScroll === 'undefined' ? PopperController.baseOptions.hideOnScroll : this.hideOnScroll;
        this.hideOnMouseLeave = typeof this.hideOnMouseLeave === 'undefined' ? PopperController.baseOptions.hideOnMouseLeave : this.hideOnMouseLeave;
        this.ariaRole = typeof this.ariaRole === 'undefined' ? PopperController.baseOptions.ariaRole : this.ariaRole;
        this.ariaDescribe = typeof this.ariaDescribe === 'undefined' ? PopperController.baseOptions.ariaDescribe : this.ariaDescribe;
        this.styles = typeof this.styles === 'undefined' ? Object.assign({}, PopperController.baseOptions.styles) : this.styles;
    }
    /**
     * @private
     * @return {?}
     */
    clearEventListeners() {
        this.eventListeners.forEach(evt => {
            evt && typeof evt === 'function' && evt();
        });
        this.eventListeners.length = 0;
    }
    /**
     * @private
     * @return {?}
     */
    clearGlobalEventListeners() {
        this.globalEventListeners.forEach(evt => {
            evt && typeof evt === 'function' && evt();
        });
        this.globalEventListeners.length = 0;
    }
    /**
     * @private
     * @return {?}
     */
    overrideShowTimeout() {
        if (this.scheduledShowTimeout) {
            clearTimeout(this.scheduledShowTimeout);
            this.scheduledHideTimeout = 0;
        }
    }
    /**
     * @private
     * @return {?}
     */
    overrideHideTimeout() {
        if (this.scheduledHideTimeout) {
            clearTimeout(this.scheduledHideTimeout);
            this.scheduledHideTimeout = 0;
        }
    }
    /**
     * @private
     * @return {?}
     */
    constructContent() {
        /** @type {?} */
        const factory = this.resolver.resolveComponentFactory(this.popperContentClass);
        this.popperContentRef = this.viewContainerRef.createComponent(factory);
        return (/** @type {?} */ (this.popperContentRef.instance));
    }
    /**
     * @private
     * @param {?} popperRef
     * @return {?}
     */
    setContentProperties(popperRef) {
        popperRef.popperOptions = PopperController.assignDefined(popperRef.popperOptions, PopperController.baseOptions, {
            showDelay: this.showDelay,
            disableAnimation: this.disableAnimation,
            disableDefaultStyling: this.disableStyle,
            placement: this.placement,
            boundariesElement: this.boundariesElement,
            trigger: this.showTrigger,
            positionFixed: this.positionFixed,
            popperModifiers: this.popperModifiers,
            ariaDescribe: this.ariaDescribe,
            ariaRole: this.ariaRole,
            applyClass: this.applyClass,
            applyArrowClass: this.applyArrowClass,
            hideOnMouseLeave: this.hideOnMouseLeave,
            styles: this.styles,
            appendTo: this.appendTo,
            preventOverflow: this.preventOverflow,
        });
        popperRef.onUpdate = this.onPopperUpdate.bind(this);
        this.subscriptions.push(popperRef.onHidden.subscribe(this.hide.bind(this)));
    }
    /**
     * @private
     * @param {?} node
     * @return {?}
     */
    getScrollParent(node) {
        /** @type {?} */
        const isElement = node instanceof HTMLElement;
        /** @type {?} */
        const overflowY = isElement && window.getComputedStyle(node).overflowY;
        /** @type {?} */
        const isScrollable = overflowY !== 'visible' && overflowY !== 'hidden';
        if (!node) {
            return null;
        }
        else if (isScrollable && node.scrollHeight >= node.clientHeight) {
            return node;
        }
        return this.getScrollParent(node.parentNode) || document;
    }
    /**
     * @private
     * @param {?} event
     * @return {?}
     */
    onPopperUpdate(event) {
        this.popperOnUpdate.emit(event);
    }
}
PopperController.baseOptions = (/** @type {?} */ ({
    showDelay: 0,
    placement: Placements.Auto,
    hideOnClickOutside: true,
    hideOnMouseLeave: false,
    hideOnScroll: false,
    showTrigger: Triggers.HOVER,
    appendTo: undefined,
    ariaRole: 'popper',
    ariaDescribe: '',
    styles: {}
}));
PopperController.decorators = [
    { type: Directive, args: [{
                selector: '[popper]',
                exportAs: 'popper'
            },] }
];
/** @nocollapse */
PopperController.ctorParameters = () => [
    { type: ViewContainerRef },
    { type: ChangeDetectorRef },
    { type: ComponentFactoryResolver },
    { type: ElementRef },
    { type: Renderer2 },
    { type: undefined, decorators: [{ type: Inject, args: ['popperDefaults',] }] }
];
PopperController.propDecorators = {
    content: [{ type: Input, args: ['popper',] }],
    disabled: [{ type: Input, args: ['popperDisabled',] }],
    placement: [{ type: Input, args: ['popperPlacement',] }],
    showTrigger: [{ type: Input, args: ['popperTrigger',] }],
    targetElement: [{ type: Input, args: ['popperTarget',] }],
    showDelay: [{ type: Input, args: ['popperDelay',] }],
    hideTimeout: [{ type: Input, args: ['popperTimeout',] }],
    timeoutAfterShow: [{ type: Input, args: ['popperTimeoutAfterShow',] }],
    boundariesElement: [{ type: Input, args: ['popperBoundaries',] }],
    showOnStart: [{ type: Input, args: ['popperShowOnStart',] }],
    closeOnClickOutside: [{ type: Input, args: ['popperCloseOnClickOutside',] }],
    hideOnClickOutside: [{ type: Input, args: ['popperHideOnClickOutside',] }],
    hideOnScroll: [{ type: Input, args: ['popperHideOnScroll',] }],
    hideOnMouseLeave: [{ type: Input, args: ['popperHideOnMouseLeave',] }],
    positionFixed: [{ type: Input, args: ['popperPositionFixed',] }],
    popperModifiers: [{ type: Input, args: ['popperModifiers',] }],
    disableStyle: [{ type: Input, args: ['popperDisableStyle',] }],
    disableAnimation: [{ type: Input, args: ['popperDisableAnimation',] }],
    applyClass: [{ type: Input, args: ['popperApplyClass',] }],
    applyArrowClass: [{ type: Input, args: ['popperApplyArrowClass',] }],
    ariaDescribe: [{ type: Input, args: ['popperAriaDescribeBy',] }],
    ariaRole: [{ type: Input, args: ['popperAriaRole',] }],
    styles: [{ type: Input, args: ['popperStyles',] }],
    appendTo: [{ type: Input, args: ['popperAppendTo',] }],
    preventOverflow: [{ type: Input, args: ['popperPreventOverflow',] }],
    popperOnShown: [{ type: Output }],
    popperOnHidden: [{ type: Output }],
    popperOnUpdate: [{ type: Output }]
};
if (false) {
    /** @type {?} */
    PopperController.baseOptions;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.popperContentClass;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.popperContentRef;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.shown;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.scheduledShowTimeout;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.scheduledHideTimeout;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.subscriptions;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.eventListeners;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.globalEventListeners;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.popperContent;
    /** @type {?} */
    PopperController.prototype.content;
    /** @type {?} */
    PopperController.prototype.disabled;
    /** @type {?} */
    PopperController.prototype.placement;
    /** @type {?} */
    PopperController.prototype.showTrigger;
    /** @type {?} */
    PopperController.prototype.targetElement;
    /** @type {?} */
    PopperController.prototype.showDelay;
    /** @type {?} */
    PopperController.prototype.hideTimeout;
    /** @type {?} */
    PopperController.prototype.timeoutAfterShow;
    /** @type {?} */
    PopperController.prototype.boundariesElement;
    /** @type {?} */
    PopperController.prototype.showOnStart;
    /** @type {?} */
    PopperController.prototype.closeOnClickOutside;
    /** @type {?} */
    PopperController.prototype.hideOnClickOutside;
    /** @type {?} */
    PopperController.prototype.hideOnScroll;
    /** @type {?} */
    PopperController.prototype.hideOnMouseLeave;
    /** @type {?} */
    PopperController.prototype.positionFixed;
    /** @type {?} */
    PopperController.prototype.popperModifiers;
    /** @type {?} */
    PopperController.prototype.disableStyle;
    /** @type {?} */
    PopperController.prototype.disableAnimation;
    /** @type {?} */
    PopperController.prototype.applyClass;
    /** @type {?} */
    PopperController.prototype.applyArrowClass;
    /** @type {?} */
    PopperController.prototype.ariaDescribe;
    /** @type {?} */
    PopperController.prototype.ariaRole;
    /** @type {?} */
    PopperController.prototype.styles;
    /** @type {?} */
    PopperController.prototype.appendTo;
    /** @type {?} */
    PopperController.prototype.preventOverflow;
    /** @type {?} */
    PopperController.prototype.popperOnShown;
    /** @type {?} */
    PopperController.prototype.popperOnHidden;
    /** @type {?} */
    PopperController.prototype.popperOnUpdate;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.viewContainerRef;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.changeDetectorRef;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.resolver;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.elementRef;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.renderer;
    /**
     * @type {?}
     * @private
     */
    PopperController.prototype.popperDefaults;
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicG9wcGVyLWRpcmVjdGl2ZS5qcyIsInNvdXJjZVJvb3QiOiJuZzovL25neC1wb3BwZXIvIiwic291cmNlcyI6WyJzcmMvcG9wcGVyLWRpcmVjdGl2ZS50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7O0FBQUEsT0FBTyxFQUNMLFNBQVMsRUFFVCxnQkFBZ0IsRUFDaEIsd0JBQXdCLEVBQ3hCLEtBQUssRUFHTCxNQUFNLEVBRU4sWUFBWSxFQUFVLFNBQVMsRUFBRSxpQkFBaUIsRUFBRSxNQUFNLEVBQUUsVUFBVSxFQUN2RSxNQUFNLGVBQWUsQ0FBQztBQUN2QixPQUFPLEVBQVksVUFBVSxFQUFpQyxRQUFRLEVBQUMsTUFBTSxnQkFBZ0IsQ0FBQztBQUM5RixPQUFPLEVBQUMsYUFBYSxFQUFDLE1BQU0sa0JBQWtCLENBQUM7QUFNL0MsTUFBTSxPQUFPLGdCQUFnQjs7Ozs7Ozs7O0lBVzNCLFlBQW9CLGdCQUFrQyxFQUNsQyxpQkFBb0MsRUFDcEMsUUFBa0MsRUFDbEMsVUFBc0IsRUFDdEIsUUFBbUIsRUFDTyxpQkFBdUMsRUFBRTtRQUxuRSxxQkFBZ0IsR0FBaEIsZ0JBQWdCLENBQWtCO1FBQ2xDLHNCQUFpQixHQUFqQixpQkFBaUIsQ0FBbUI7UUFDcEMsYUFBUSxHQUFSLFFBQVEsQ0FBMEI7UUFDbEMsZUFBVSxHQUFWLFVBQVUsQ0FBWTtRQUN0QixhQUFRLEdBQVIsUUFBUSxDQUFXO1FBQ08sbUJBQWMsR0FBZCxjQUFjLENBQTJCO1FBZi9FLHVCQUFrQixHQUFHLGFBQWEsQ0FBQztRQUVuQyxVQUFLLEdBQVksS0FBSyxDQUFDO1FBR3ZCLGtCQUFhLEdBQVUsRUFBRSxDQUFDO1FBQzFCLG1CQUFjLEdBQVUsRUFBRSxDQUFDO1FBQzNCLHlCQUFvQixHQUFVLEVBQUUsQ0FBQztRQTRDekMsZ0JBQVcsR0FBVyxDQUFDLENBQUM7UUFHeEIscUJBQWdCLEdBQVcsQ0FBQyxDQUFDO1FBc0Q3QixrQkFBYSxHQUFtQyxJQUFJLFlBQVksRUFBb0IsQ0FBQztRQUdyRixt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QkFBdUI7MkJBRzdCLEtBQUssU0FBQyxzQkFBc0I7dUJBRzVCLEtBQUssU0FBQyxnQkFBZ0I7cUJBR3RCLEtBQUssU0FBQyxjQUFjO3VCQUdwQixLQUFLLFNBQUMsZ0JBQWdCOzhCQUd0QixLQUFLLFNBQUMsdUJBQXVCOzRCQUc3QixNQUFNOzZCQUdOLE1BQU07NkJBR04sTUFBTTs7OztJQTlGUCw2QkFXRTs7Ozs7SUE5QkYsOENBQTJDOzs7OztJQUMzQyw0Q0FBc0Q7Ozs7O0lBQ3RELGlDQUErQjs7Ozs7SUFDL0IsZ0RBQWtDOzs7OztJQUNsQyxnREFBa0M7Ozs7O0lBQ2xDLHlDQUFrQzs7Ozs7SUFDbEMsMENBQW1DOzs7OztJQUNuQyxnREFBeUM7Ozs7O0lBQ3pDLHlDQUFxQzs7SUF3QnJDLG1DQUNnQzs7SUFFaEMsb0NBQ2tCOztJQUVsQixxQ0FDcUI7O0lBRXJCLHVDQUNpQzs7SUFFakMseUNBQzJCOztJQUUzQixxQ0FDOEI7O0lBRTlCLHVDQUN3Qjs7SUFFeEIsNENBQzZCOztJQUU3Qiw2Q0FDMEI7O0lBRTFCLHVDQUNxQjs7SUFFckIsK0NBQzZCOztJQUU3Qiw4Q0FDd0M7O0lBRXhDLHdDQUNrQzs7SUFFbEMsNENBQ3NDOztJQUV0Qyx5Q0FDdUI7O0lBRXZCLDJDQUNvQjs7SUFFcEIsd0NBQ3NCOztJQUV0Qiw0Q0FDMEI7O0lBRTFCLHNDQUNtQjs7SUFFbkIsMkNBQ3dCOztJQUV4Qix3Q0FDaUM7O0lBRWpDLG9DQUM2Qjs7SUFFN0Isa0NBQzJCOztJQUUzQixvQ0FDaUI7O0lBRWpCLDJDQUN5Qjs7SUFFekIseUNBQ3FGOztJQUVyRiwwQ0FDc0Y7O0lBRXRGLDBDQUM0RDs7Ozs7SUF4R2hELDRDQUEwQzs7Ozs7SUFDMUMsNkNBQTRDOzs7OztJQUM1QyxvQ0FBMEM7Ozs7O0lBQzFDLHNDQUE4Qjs7Ozs7SUFDOUIsb0NBQTJCOzs7OztJQUMzQiwwQ0FBMkUiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQge1xyXG4gIERpcmVjdGl2ZSxcclxuICBDb21wb25lbnRSZWYsXHJcbiAgVmlld0NvbnRhaW5lclJlZixcclxuICBDb21wb25lbnRGYWN0b3J5UmVzb2x2ZXIsXHJcbiAgSW5wdXQsXHJcbiAgT25DaGFuZ2VzLFxyXG4gIFNpbXBsZUNoYW5nZSxcclxuICBPdXRwdXQsXHJcbiAgT25EZXN0cm95LFxyXG4gIEV2ZW50RW1pdHRlciwgT25Jbml0LCBSZW5kZXJlcjIsIENoYW5nZURldGVjdG9yUmVmLCBJbmplY3QsIEVsZW1lbnRSZWZcclxufSBmcm9tICdAYW5ndWxhci9jb3JlJztcclxuaW1wb3J0IHtQbGFjZW1lbnQsIFBsYWNlbWVudHMsIFBvcHBlckNvbnRlbnRPcHRpb25zLCBUcmlnZ2VyLCBUcmlnZ2Vyc30gZnJvbSAnLi9wb3BwZXItbW9kZWwnO1xyXG5pbXBvcnQge1BvcHBlckNvbnRlbnR9IGZyb20gJy4vcG9wcGVyLWNvbnRlbnQnO1xyXG5cclxuQERpcmVjdGl2ZSh7XHJcbiAgc2VsZWN0b3I6ICdbcG9wcGVyXScsXHJcbiAgZXhwb3J0QXM6ICdwb3BwZXInXHJcbn0pXHJcbmV4cG9ydCBjbGFzcyBQb3BwZXJDb250cm9sbGVyIGltcGxlbWVudHMgT25Jbml0LCBPbkRlc3Ryb3ksIE9uQ2hhbmdlcyB7XHJcbiAgcHJpdmF0ZSBwb3BwZXJDb250ZW50Q2xhc3MgPSBQb3BwZXJDb250ZW50O1xyXG4gIHByaXZhdGUgcG9wcGVyQ29udGVudFJlZjogQ29tcG9uZW50UmVmPFBvcHBlckNvbnRlbnQ+O1xyXG4gIHByaXZhdGUgc2hvd246IGJvb2xlYW4gPSBmYWxzZTtcclxuICBwcml2YXRlIHNjaGVkdWxlZFNob3dUaW1lb3V0OiBhbnk7XHJcbiAgcHJpdmF0ZSBzY2hlZHVsZWRIaWRlVGltZW91dDogYW55O1xyXG4gIHByaXZhdGUgc3Vic2NyaXB0aW9uczogYW55W10gPSBbXTtcclxuICBwcml2YXRlIGV2ZW50TGlzdGVuZXJzOiBhbnlbXSA9IFtdO1xyXG4gIHByaXZhdGUgZ2xvYmFsRXZlbnRMaXN0ZW5lcnM6IGFueVtdID0gW107XHJcbiAgcHJpdmF0ZSBwb3BwZXJDb250ZW50OiBQb3BwZXJDb250ZW50O1xyXG5cclxuICBjb25zdHJ1Y3Rvcihwcml2YXRlIHZpZXdDb250YWluZXJSZWY6IFZpZXdDb250YWluZXJSZWYsXHJcbiAgICAgICAgICAgICAgcHJpdmF0ZSBjaGFuZ2VEZXRlY3RvclJlZjogQ2hhbmdlRGV0ZWN0b3JSZWYsXHJcbiAgICAgICAgICAgICAgcHJpdmF0ZSByZXNvbHZlcjogQ29tcG9uZW50RmFjdG9yeVJlc29sdmVyLFxyXG4gICAgICAgICAgICAgIHByaXZhdGUgZWxlbWVudFJlZjogRWxlbWVudFJlZixcclxuICAgICAgICAgICAgICBwcml2YXRlIHJlbmRlcmVyOiBSZW5kZXJlcjIsXHJcbiAgICAgICAgICAgICAgQEluamVjdCgncG9wcGVyRGVmYXVsdHMnKSBwcml2YXRlIHBvcHBlckRlZmF1bHRzOiBQb3BwZXJDb250ZW50T3B0aW9ucyA9IHt9KSB7XHJcbiAgICBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zID0gey4uLlBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMsIC4uLnRoaXMucG9wcGVyRGVmYXVsdHN9O1xyXG4gIH1cclxuXHJcbiAgcHVibGljIHN0YXRpYyBiYXNlT3B0aW9uczogUG9wcGVyQ29udGVudE9wdGlvbnMgPSA8UG9wcGVyQ29udGVudE9wdGlvbnM+e1xyXG4gICAgc2hvd0RlbGF5OiAwLFxyXG4gICAgcGxhY2VtZW50OiBQbGFjZW1lbnRzLkF1dG8sXHJcbiAgICBoaWRlT25DbGlja091dHNpZGU6IHRydWUsXHJcbiAgICBoaWRlT25Nb3VzZUxlYXZlOiBmYWxzZSxcclxuICAgIGhpZGVPblNjcm9sbDogZmFsc2UsXHJcbiAgICBzaG93VHJpZ2dlcjogVHJpZ2dlcnMuSE9WRVIsXHJcbiAgICBhcHBlbmRUbzogdW5kZWZpbmVkLFxyXG4gICAgYXJpYVJvbGU6ICdwb3BwZXInLFxyXG4gICAgYXJpYURlc2NyaWJlOiAnJyxcclxuICAgIHN0eWxlczoge31cclxuICB9O1xyXG5cclxuICBASW5wdXQoJ3BvcHBlcicpXHJcbiAgY29udGVudDogc3RyaW5nIHwgUG9wcGVyQ29udGVudDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJEaXNhYmxlZCcpXHJcbiAgZGlzYWJsZWQ6IGJvb2xlYW47XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyUGxhY2VtZW50JylcclxuICBwbGFjZW1lbnQ6IFBsYWNlbWVudDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJUcmlnZ2VyJylcclxuICBzaG93VHJpZ2dlcjogVHJpZ2dlciB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJUYXJnZXQnKVxyXG4gIHRhcmdldEVsZW1lbnQ6IEhUTUxFbGVtZW50O1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckRlbGF5JylcclxuICBzaG93RGVsYXk6IG51bWJlciB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJUaW1lb3V0JylcclxuICBoaWRlVGltZW91dDogbnVtYmVyID0gMDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJUaW1lb3V0QWZ0ZXJTaG93JylcclxuICB0aW1lb3V0QWZ0ZXJTaG93OiBudW1iZXIgPSAwO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckJvdW5kYXJpZXMnKVxyXG4gIGJvdW5kYXJpZXNFbGVtZW50OiBzdHJpbmc7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyU2hvd09uU3RhcnQnKVxyXG4gIHNob3dPblN0YXJ0OiBib29sZWFuO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckNsb3NlT25DbGlja091dHNpZGUnKVxyXG4gIGNsb3NlT25DbGlja091dHNpZGU6IGJvb2xlYW47XHJcblxyXG4gIEBJbnB1dCgncG9wcGVySGlkZU9uQ2xpY2tPdXRzaWRlJylcclxuICBoaWRlT25DbGlja091dHNpZGU6IGJvb2xlYW4gfCB1bmRlZmluZWQ7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVySGlkZU9uU2Nyb2xsJylcclxuICBoaWRlT25TY3JvbGw6IGJvb2xlYW4gfCB1bmRlZmluZWQ7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVySGlkZU9uTW91c2VMZWF2ZScpXHJcbiAgaGlkZU9uTW91c2VMZWF2ZTogYm9vbGVhbiB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJQb3NpdGlvbkZpeGVkJylcclxuICBwb3NpdGlvbkZpeGVkOiBib29sZWFuO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlck1vZGlmaWVycycpXHJcbiAgcG9wcGVyTW9kaWZpZXJzOiB7fTtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJEaXNhYmxlU3R5bGUnKVxyXG4gIGRpc2FibGVTdHlsZTogYm9vbGVhbjtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJEaXNhYmxlQW5pbWF0aW9uJylcclxuICBkaXNhYmxlQW5pbWF0aW9uOiBib29sZWFuO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckFwcGx5Q2xhc3MnKVxyXG4gIGFwcGx5Q2xhc3M6IHN0cmluZztcclxuXHJcbiAgQElucHV0KCdwb3BwZXJBcHBseUFycm93Q2xhc3MnKVxyXG4gIGFwcGx5QXJyb3dDbGFzczogc3RyaW5nO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckFyaWFEZXNjcmliZUJ5JylcclxuICBhcmlhRGVzY3JpYmU6IHN0cmluZyB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJBcmlhUm9sZScpXHJcbiAgYXJpYVJvbGU6IHN0cmluZyB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJTdHlsZXMnKVxyXG4gIHN0eWxlczogT2JqZWN0IHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckFwcGVuZFRvJylcclxuICBhcHBlbmRUbzogc3RyaW5nO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclByZXZlbnRPdmVyZmxvdycpXHJcbiAgcHJldmVudE92ZXJmbG93OiBib29sZWFuO1xyXG5cclxuICBAT3V0cHV0KClcclxuICBwb3BwZXJPblNob3duOiBFdmVudEVtaXR0ZXI8UG9wcGVyQ29udHJvbGxlcj4gPSBuZXcgRXZlbnRFbWl0dGVyPFBvcHBlckNvbnRyb2xsZXI+KCk7XHJcblxyXG4gIEBPdXRwdXQoKVxyXG4gIHBvcHBlck9uSGlkZGVuOiBFdmVudEVtaXR0ZXI8UG9wcGVyQ29udHJvbGxlcj4gPSBuZXcgRXZlbnRFbWl0dGVyPFBvcHBlckNvbnRyb2xsZXI+KCk7XHJcblxyXG4gIEBPdXRwdXQoKVxyXG4gIHBvcHBlck9uVXBkYXRlOiBFdmVudEVtaXR0ZXI8YW55PiA9IG5ldyBFdmVudEVtaXR0ZXI8YW55PigpO1xyXG5cclxuICBoaWRlT25DbGlja091dHNpZGVIYW5kbGVyKCRldmVudDogTW91c2VFdmVudCk6IHZvaWQge1xyXG4gICAgaWYgKHRoaXMuZGlzYWJsZWQgfHwgIXRoaXMuaGlkZU9uQ2xpY2tPdXRzaWRlIHx8ICRldmVudC5zcmNFbGVtZW50ICYmXHJcbiAgICAgICRldmVudC5zcmNFbGVtZW50ID09PSB0aGlzLnBvcHBlckNvbnRlbnQuZWxlbVJlZi5uYXRpdmVFbGVtZW50IHx8XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudC5lbGVtUmVmLm5hdGl2ZUVsZW1lbnQuY29udGFpbnMoJGV2ZW50LnNyY0VsZW1lbnQpKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHRoaXMuc2NoZWR1bGVkSGlkZSgkZXZlbnQsIHRoaXMuaGlkZVRpbWVvdXQpO1xyXG4gIH1cclxuXHJcbiAgaGlkZU9uU2Nyb2xsSGFuZGxlcigkZXZlbnQ6IE1vdXNlRXZlbnQpOiB2b2lkIHtcclxuICAgIGlmICh0aGlzLmRpc2FibGVkIHx8ICF0aGlzLmhpZGVPblNjcm9sbCkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcbiAgICB0aGlzLnNjaGVkdWxlZEhpZGUoJGV2ZW50LCB0aGlzLmhpZGVUaW1lb3V0KTtcclxuICB9XHJcblxyXG4gIGFwcGx5VHJpZ2dlckxpc3RlbmVycygpIHtcclxuICAgIHN3aXRjaCAodGhpcy5zaG93VHJpZ2dlcikge1xyXG4gICAgICBjYXNlIFRyaWdnZXJzLkNMSUNLOlxyXG4gICAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ2NsaWNrJywgdGhpcy50b2dnbGUuYmluZCh0aGlzKSkpO1xyXG4gICAgICAgIGJyZWFrO1xyXG4gICAgICBjYXNlIFRyaWdnZXJzLk1PVVNFRE9XTjpcclxuICAgICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICdtb3VzZWRvd24nLCB0aGlzLnRvZ2dsZS5iaW5kKHRoaXMpKSk7XHJcbiAgICAgICAgYnJlYWs7XHJcbiAgICAgIGNhc2UgVHJpZ2dlcnMuSE9WRVI6XHJcbiAgICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAnbW91c2VlbnRlcicsIHRoaXMuc2NoZWR1bGVkU2hvdy5iaW5kKHRoaXMsIHRoaXMuc2hvd0RlbGF5KSkpO1xyXG4gICAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ3RvdWNoZW5kJywgdGhpcy5zY2hlZHVsZWRIaWRlLmJpbmQodGhpcywgbnVsbCwgdGhpcy5oaWRlVGltZW91dCkpKTtcclxuICAgICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICd0b3VjaGNhbmNlbCcsIHRoaXMuc2NoZWR1bGVkSGlkZS5iaW5kKHRoaXMsIG51bGwsIHRoaXMuaGlkZVRpbWVvdXQpKSk7XHJcbiAgICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAnbW91c2VsZWF2ZScsIHRoaXMuc2NoZWR1bGVkSGlkZS5iaW5kKHRoaXMsIG51bGwsIHRoaXMuaGlkZVRpbWVvdXQpKSk7XHJcbiAgICAgICAgYnJlYWs7XHJcbiAgICB9XHJcbiAgICBpZiAodGhpcy5zaG93VHJpZ2dlciAhPT0gVHJpZ2dlcnMuSE9WRVIgJiYgdGhpcy5oaWRlT25Nb3VzZUxlYXZlKSB7XHJcbiAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ3RvdWNoZW5kJywgdGhpcy5zY2hlZHVsZWRIaWRlLmJpbmQodGhpcywgbnVsbCwgdGhpcy5oaWRlVGltZW91dCkpKTtcclxuICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAndG91Y2hjYW5jZWwnLCB0aGlzLnNjaGVkdWxlZEhpZGUuYmluZCh0aGlzLCBudWxsLCB0aGlzLmhpZGVUaW1lb3V0KSkpO1xyXG4gICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICdtb3VzZWxlYXZlJywgdGhpcy5zY2hlZHVsZWRIaWRlLmJpbmQodGhpcywgbnVsbCwgdGhpcy5oaWRlVGltZW91dCkpKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHN0YXRpYyBhc3NpZ25EZWZpbmVkKHRhcmdldDogYW55LCAuLi5zb3VyY2VzOiBhbnlbXSkge1xyXG4gICAgZm9yIChjb25zdCBzb3VyY2Ugb2Ygc291cmNlcykge1xyXG4gICAgICBmb3IgKGNvbnN0IGtleSBvZiBPYmplY3Qua2V5cyhzb3VyY2UpKSB7XHJcbiAgICAgICAgY29uc3QgdmFsID0gc291cmNlW2tleV07XHJcbiAgICAgICAgaWYgKHZhbCAhPT0gdW5kZWZpbmVkKSB7XHJcbiAgICAgICAgICB0YXJnZXRba2V5XSA9IHZhbDtcclxuICAgICAgICB9XHJcbiAgICAgIH1cclxuICAgIH1cclxuICAgIHJldHVybiB0YXJnZXQ7XHJcbiAgfVxyXG5cclxuICBuZ09uSW5pdCgpIHtcclxuICAgIC8vU3VwcG9ydCBsZWdhY3kgcHJvcFxyXG4gICAgdGhpcy5oaWRlT25DbGlja091dHNpZGUgPSB0eXBlb2YgdGhpcy5oaWRlT25DbGlja091dHNpZGUgPT09ICd1bmRlZmluZWQnID9cclxuICAgICAgdGhpcy5jbG9zZU9uQ2xpY2tPdXRzaWRlIDogdGhpcy5oaWRlT25DbGlja091dHNpZGU7XHJcblxyXG4gICAgaWYgKHR5cGVvZiB0aGlzLmNvbnRlbnQgPT09ICdzdHJpbmcnKSB7XHJcbiAgICAgIGNvbnN0IHRleHQgPSB0aGlzLmNvbnRlbnQ7XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudCA9IHRoaXMuY29uc3RydWN0Q29udGVudCgpO1xyXG4gICAgICB0aGlzLnBvcHBlckNvbnRlbnQudGV4dCA9IHRleHQ7XHJcbiAgICB9XHJcbiAgICBlbHNlIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50ID0gdGhpcy5jb250ZW50O1xyXG4gICAgfVxyXG4gICAgY29uc3QgcG9wcGVyUmVmID0gdGhpcy5wb3BwZXJDb250ZW50O1xyXG4gICAgcG9wcGVyUmVmLnJlZmVyZW5jZU9iamVjdCA9IHRoaXMuZ2V0UmVmRWxlbWVudCgpO1xyXG4gICAgdGhpcy5zZXRDb250ZW50UHJvcGVydGllcyhwb3BwZXJSZWYpO1xyXG4gICAgdGhpcy5zZXREZWZhdWx0cygpO1xyXG4gICAgdGhpcy5hcHBseVRyaWdnZXJMaXN0ZW5lcnMoKTtcclxuICAgIGlmICh0aGlzLnNob3dPblN0YXJ0KSB7XHJcbiAgICAgIHRoaXMuc2NoZWR1bGVkU2hvdygpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgbmdPbkNoYW5nZXMoY2hhbmdlczogeyBbcHJvcGVydHlOYW1lOiBzdHJpbmddOiBTaW1wbGVDaGFuZ2UgfSkge1xyXG4gICAgaWYgKGNoYW5nZXNbJ3BvcHBlckRpc2FibGVkJ10gJiYgY2hhbmdlc1sncG9wcGVyRGlzYWJsZWQnXS5jdXJyZW50VmFsdWUpIHtcclxuICAgICAgdGhpcy5oaWRlKCk7XHJcbiAgICB9XHJcbiAgICBpZiAoY2hhbmdlc1snY29udGVudCddXHJcbiAgICAgICYmICFjaGFuZ2VzWydjb250ZW50J10uZmlyc3RDaGFuZ2VcclxuICAgICAgJiYgdHlwZW9mIGNoYW5nZXNbJ2NvbnRlbnQnXS5jdXJyZW50VmFsdWUgPT09ICdzdHJpbmcnKSB7XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudC50ZXh0ID0gY2hhbmdlc1snY29udGVudCddLmN1cnJlbnRWYWx1ZTtcclxuICAgIH1cclxuXHJcbiAgICBpZiAoY2hhbmdlc1snYXBwbHlDbGFzcyddXHJcbiAgICAgICYmICFjaGFuZ2VzWydhcHBseUNsYXNzJ10uZmlyc3RDaGFuZ2VcclxuICAgICAgJiYgdHlwZW9mIGNoYW5nZXNbJ2FwcGx5Q2xhc3MnXS5jdXJyZW50VmFsdWUgPT09ICdzdHJpbmcnKSB7XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudC5wb3BwZXJPcHRpb25zLmFwcGx5Q2xhc3MgPSBjaGFuZ2VzWydhcHBseUNsYXNzJ10uY3VycmVudFZhbHVlO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChjaGFuZ2VzWydhcHBseUFycm93Q2xhc3MnXVxyXG4gICAgICAmJiAhY2hhbmdlc1snYXBwbHlBcnJvd0NsYXNzJ10uZmlyc3RDaGFuZ2VcclxuICAgICAgJiYgdHlwZW9mIGNoYW5nZXNbJ2FwcGx5QXJyb3dDbGFzcyddLmN1cnJlbnRWYWx1ZSA9PT0gJ3N0cmluZycpIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50LnBvcHBlck9wdGlvbnMuYXBwbHlBcnJvd0NsYXNzID0gY2hhbmdlc1snYXBwbHlBcnJvd0NsYXNzJ10uY3VycmVudFZhbHVlO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgbmdPbkRlc3Ryb3koKSB7XHJcbiAgICB0aGlzLnN1YnNjcmlwdGlvbnMuZm9yRWFjaChzdWIgPT4gc3ViLnVuc3Vic2NyaWJlICYmIHN1Yi51bnN1YnNjcmliZSgpKTtcclxuICAgIHRoaXMuc3Vic2NyaXB0aW9ucy5sZW5ndGggPSAwO1xyXG4gICAgdGhpcy5jbGVhckV2ZW50TGlzdGVuZXJzKCk7XHJcbiAgICB0aGlzLmNsZWFyR2xvYmFsRXZlbnRMaXN0ZW5lcnMoKTtcclxuICAgIGNsZWFyVGltZW91dCh0aGlzLnNjaGVkdWxlZFNob3dUaW1lb3V0KTtcclxuICAgIGNsZWFyVGltZW91dCh0aGlzLnNjaGVkdWxlZEhpZGVUaW1lb3V0KTtcclxuICAgIHRoaXMucG9wcGVyQ29udGVudCAmJiB0aGlzLnBvcHBlckNvbnRlbnQuY2xlYW4oKTtcclxuICB9XHJcblxyXG4gIHRvZ2dsZSgpIHtcclxuICAgIGlmICh0aGlzLmRpc2FibGVkKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHRoaXMuc2hvd24gPyB0aGlzLnNjaGVkdWxlZEhpZGUobnVsbCwgdGhpcy5oaWRlVGltZW91dCkgOiB0aGlzLnNjaGVkdWxlZFNob3coKTtcclxuICB9XHJcblxyXG4gIHNob3coKSB7XHJcbiAgICBpZiAodGhpcy5zaG93bikge1xyXG4gICAgICB0aGlzLm92ZXJyaWRlSGlkZVRpbWVvdXQoKTtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMuc2hvd24gPSB0cnVlO1xyXG4gICAgY29uc3QgcG9wcGVyUmVmID0gdGhpcy5wb3BwZXJDb250ZW50O1xyXG4gICAgY29uc3QgZWxlbWVudCA9IHRoaXMuZ2V0UmVmRWxlbWVudCgpO1xyXG4gICAgaWYgKHBvcHBlclJlZi5yZWZlcmVuY2VPYmplY3QgIT09IGVsZW1lbnQpIHtcclxuICAgICAgcG9wcGVyUmVmLnJlZmVyZW5jZU9iamVjdCA9IGVsZW1lbnQ7XHJcbiAgICB9XHJcbiAgICB0aGlzLnNldENvbnRlbnRQcm9wZXJ0aWVzKHBvcHBlclJlZik7XHJcbiAgICBwb3BwZXJSZWYuc2hvdygpO1xyXG4gICAgdGhpcy5wb3BwZXJPblNob3duLmVtaXQodGhpcyk7XHJcbiAgICBpZiAodGhpcy50aW1lb3V0QWZ0ZXJTaG93ID4gMCkge1xyXG4gICAgICB0aGlzLnNjaGVkdWxlZEhpZGUobnVsbCwgdGhpcy50aW1lb3V0QWZ0ZXJTaG93KTtcclxuICAgIH1cclxuICAgIHRoaXMuZ2xvYmFsRXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3RlbignZG9jdW1lbnQnLCAndG91Y2hlbmQnLCB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZUhhbmRsZXIuYmluZCh0aGlzKSkpO1xyXG4gICAgdGhpcy5nbG9iYWxFdmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKCdkb2N1bWVudCcsICdjbGljaycsIHRoaXMuaGlkZU9uQ2xpY2tPdXRzaWRlSGFuZGxlci5iaW5kKHRoaXMpKSk7XHJcbiAgICB0aGlzLmdsb2JhbEV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5nZXRTY3JvbGxQYXJlbnQodGhpcy5nZXRSZWZFbGVtZW50KCkpLCAnc2Nyb2xsJywgdGhpcy5oaWRlT25TY3JvbGxIYW5kbGVyLmJpbmQodGhpcykpKTtcclxuICB9XHJcblxyXG4gIGhpZGUoKSB7XHJcbiAgICBpZiAodGhpcy5kaXNhYmxlZCkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcbiAgICBpZiAoIXRoaXMuc2hvd24pIHtcclxuICAgICAgdGhpcy5vdmVycmlkZVNob3dUaW1lb3V0KCk7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuXHJcbiAgICB0aGlzLnNob3duID0gZmFsc2U7XHJcbiAgICBpZiAodGhpcy5wb3BwZXJDb250ZW50UmVmKSB7XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudFJlZi5pbnN0YW5jZS5oaWRlKCk7XHJcbiAgICB9XHJcbiAgICBlbHNlIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50LmhpZGUoKTtcclxuICAgIH1cclxuICAgIHRoaXMucG9wcGVyT25IaWRkZW4uZW1pdCh0aGlzKTtcclxuICAgIHRoaXMuY2xlYXJHbG9iYWxFdmVudExpc3RlbmVycygpO1xyXG4gIH1cclxuXHJcbiAgc2NoZWR1bGVkU2hvdyhkZWxheTogbnVtYmVyIHwgdW5kZWZpbmVkID0gdGhpcy5zaG93RGVsYXkpIHtcclxuICAgIGlmICh0aGlzLmRpc2FibGVkKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHRoaXMub3ZlcnJpZGVIaWRlVGltZW91dCgpO1xyXG4gICAgdGhpcy5zY2hlZHVsZWRTaG93VGltZW91dCA9IHNldFRpbWVvdXQoKCkgPT4ge1xyXG4gICAgICB0aGlzLnNob3coKTtcclxuICAgICAgdGhpcy5hcHBseUNoYW5nZXMoKTtcclxuICAgIH0sIGRlbGF5KVxyXG4gIH1cclxuXHJcbiAgc2NoZWR1bGVkSGlkZSgkZXZlbnQ6IGFueSA9IG51bGwsIGRlbGF5OiBudW1iZXIgPSB0aGlzLmhpZGVUaW1lb3V0KSB7XHJcbiAgICBpZiAodGhpcy5kaXNhYmxlZCkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcbiAgICB0aGlzLm92ZXJyaWRlU2hvd1RpbWVvdXQoKTtcclxuICAgIHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQgPSBzZXRUaW1lb3V0KCgpID0+IHtcclxuICAgICAgY29uc3QgdG9FbGVtZW50ID0gJGV2ZW50ID8gJGV2ZW50LnRvRWxlbWVudCA6IG51bGw7XHJcbiAgICAgIGNvbnN0IHBvcHBlckNvbnRlbnRWaWV3ID0gdGhpcy5wb3BwZXJDb250ZW50LnBvcHBlclZpZXdSZWYgPyB0aGlzLnBvcHBlckNvbnRlbnQucG9wcGVyVmlld1JlZi5uYXRpdmVFbGVtZW50IDogZmFsc2U7XHJcbiAgICAgIGlmICghcG9wcGVyQ29udGVudFZpZXcgfHwgcG9wcGVyQ29udGVudFZpZXcgPT09IHRvRWxlbWVudCB8fCBwb3BwZXJDb250ZW50Vmlldy5jb250YWlucyh0b0VsZW1lbnQpIHx8ICh0aGlzLmNvbnRlbnQgYXMgUG9wcGVyQ29udGVudCkuaXNNb3VzZU92ZXIpIHtcclxuICAgICAgICByZXR1cm47XHJcbiAgICAgIH1cclxuICAgICAgdGhpcy5oaWRlKCk7XHJcbiAgICAgIHRoaXMuYXBwbHlDaGFuZ2VzKCk7XHJcbiAgICB9LCBkZWxheSk7XHJcbiAgfVxyXG5cclxuICBnZXRSZWZFbGVtZW50KCkge1xyXG4gICAgcmV0dXJuIHRoaXMudGFyZ2V0RWxlbWVudCB8fCB0aGlzLnZpZXdDb250YWluZXJSZWYuZWxlbWVudC5uYXRpdmVFbGVtZW50O1xyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBhcHBseUNoYW5nZXMoKSB7XHJcbiAgICB0aGlzLmNoYW5nZURldGVjdG9yUmVmLm1hcmtGb3JDaGVjaygpO1xyXG4gICAgdGhpcy5jaGFuZ2VEZXRlY3RvclJlZi5kZXRlY3RDaGFuZ2VzKCk7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIHNldERlZmF1bHRzKCkge1xyXG4gICAgdGhpcy5zaG93RGVsYXkgPSB0eXBlb2YgdGhpcy5zaG93RGVsYXkgPT09ICd1bmRlZmluZWQnID8gUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucy5zaG93RGVsYXkgOiB0aGlzLnNob3dEZWxheTtcclxuICAgIHRoaXMuc2hvd1RyaWdnZXIgPSB0eXBlb2YgdGhpcy5zaG93VHJpZ2dlciA9PT0gJ3VuZGVmaW5lZCcgPyBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zLnRyaWdnZXIgOiB0aGlzLnNob3dUcmlnZ2VyO1xyXG4gICAgdGhpcy5oaWRlT25DbGlja091dHNpZGUgPSB0eXBlb2YgdGhpcy5oaWRlT25DbGlja091dHNpZGUgPT09ICd1bmRlZmluZWQnID8gUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucy5oaWRlT25DbGlja091dHNpZGUgOiB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZTtcclxuICAgIHRoaXMuaGlkZU9uU2Nyb2xsID0gdHlwZW9mIHRoaXMuaGlkZU9uU2Nyb2xsID09PSAndW5kZWZpbmVkJyA/IFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMuaGlkZU9uU2Nyb2xsIDogdGhpcy5oaWRlT25TY3JvbGw7XHJcbiAgICB0aGlzLmhpZGVPbk1vdXNlTGVhdmUgPSB0eXBlb2YgdGhpcy5oaWRlT25Nb3VzZUxlYXZlID09PSAndW5kZWZpbmVkJyA/IFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMuaGlkZU9uTW91c2VMZWF2ZSA6IHRoaXMuaGlkZU9uTW91c2VMZWF2ZTtcclxuICAgIHRoaXMuYXJpYVJvbGUgPSB0eXBlb2YgdGhpcy5hcmlhUm9sZSA9PT0gJ3VuZGVmaW5lZCcgPyBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zLmFyaWFSb2xlIDogdGhpcy5hcmlhUm9sZTtcclxuICAgIHRoaXMuYXJpYURlc2NyaWJlID0gdHlwZW9mIHRoaXMuYXJpYURlc2NyaWJlID09PSAndW5kZWZpbmVkJyA/IFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMuYXJpYURlc2NyaWJlIDogdGhpcy5hcmlhRGVzY3JpYmU7XHJcbiAgICB0aGlzLnN0eWxlcyA9IHR5cGVvZiB0aGlzLnN0eWxlcyA9PT0gJ3VuZGVmaW5lZCcgPyBPYmplY3QuYXNzaWduKHt9LCBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zLnN0eWxlcykgOiB0aGlzLnN0eWxlcztcclxuICB9XHJcblxyXG4gIHByaXZhdGUgY2xlYXJFdmVudExpc3RlbmVycygpIHtcclxuICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMuZm9yRWFjaChldnQgPT4ge1xyXG4gICAgICBldnQgJiYgdHlwZW9mIGV2dCA9PT0gJ2Z1bmN0aW9uJyAmJiBldnQoKTtcclxuICAgIH0pO1xyXG4gICAgdGhpcy5ldmVudExpc3RlbmVycy5sZW5ndGggPSAwO1xyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBjbGVhckdsb2JhbEV2ZW50TGlzdGVuZXJzKCkge1xyXG4gICAgdGhpcy5nbG9iYWxFdmVudExpc3RlbmVycy5mb3JFYWNoKGV2dCA9PiB7XHJcbiAgICAgIGV2dCAmJiB0eXBlb2YgZXZ0ID09PSAnZnVuY3Rpb24nICYmIGV2dCgpO1xyXG4gICAgfSk7XHJcbiAgICB0aGlzLmdsb2JhbEV2ZW50TGlzdGVuZXJzLmxlbmd0aCA9IDA7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIG92ZXJyaWRlU2hvd1RpbWVvdXQoKSB7XHJcbiAgICBpZiAodGhpcy5zY2hlZHVsZWRTaG93VGltZW91dCkge1xyXG4gICAgICBjbGVhclRpbWVvdXQodGhpcy5zY2hlZHVsZWRTaG93VGltZW91dCk7XHJcbiAgICAgIHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQgPSAwO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBvdmVycmlkZUhpZGVUaW1lb3V0KCkge1xyXG4gICAgaWYgKHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQpIHtcclxuICAgICAgY2xlYXJUaW1lb3V0KHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQpO1xyXG4gICAgICB0aGlzLnNjaGVkdWxlZEhpZGVUaW1lb3V0ID0gMDtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHByaXZhdGUgY29uc3RydWN0Q29udGVudCgpOiBQb3BwZXJDb250ZW50IHtcclxuICAgIGNvbnN0IGZhY3RvcnkgPSB0aGlzLnJlc29sdmVyLnJlc29sdmVDb21wb25lbnRGYWN0b3J5KHRoaXMucG9wcGVyQ29udGVudENsYXNzKTtcclxuICAgIHRoaXMucG9wcGVyQ29udGVudFJlZiA9IHRoaXMudmlld0NvbnRhaW5lclJlZi5jcmVhdGVDb21wb25lbnQoZmFjdG9yeSk7XHJcbiAgICByZXR1cm4gdGhpcy5wb3BwZXJDb250ZW50UmVmLmluc3RhbmNlIGFzIFBvcHBlckNvbnRlbnQ7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIHNldENvbnRlbnRQcm9wZXJ0aWVzKHBvcHBlclJlZjogUG9wcGVyQ29udGVudCkge1xyXG4gICAgcG9wcGVyUmVmLnBvcHBlck9wdGlvbnMgPSBQb3BwZXJDb250cm9sbGVyLmFzc2lnbkRlZmluZWQocG9wcGVyUmVmLnBvcHBlck9wdGlvbnMsIFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMsIHtcclxuICAgICAgc2hvd0RlbGF5OiB0aGlzLnNob3dEZWxheSxcclxuICAgICAgZGlzYWJsZUFuaW1hdGlvbjogdGhpcy5kaXNhYmxlQW5pbWF0aW9uLFxyXG4gICAgICBkaXNhYmxlRGVmYXVsdFN0eWxpbmc6IHRoaXMuZGlzYWJsZVN0eWxlLFxyXG4gICAgICBwbGFjZW1lbnQ6IHRoaXMucGxhY2VtZW50LFxyXG4gICAgICBib3VuZGFyaWVzRWxlbWVudDogdGhpcy5ib3VuZGFyaWVzRWxlbWVudCxcclxuICAgICAgdHJpZ2dlcjogdGhpcy5zaG93VHJpZ2dlcixcclxuICAgICAgcG9zaXRpb25GaXhlZDogdGhpcy5wb3NpdGlvbkZpeGVkLFxyXG4gICAgICBwb3BwZXJNb2RpZmllcnM6IHRoaXMucG9wcGVyTW9kaWZpZXJzLFxyXG4gICAgICBhcmlhRGVzY3JpYmU6IHRoaXMuYXJpYURlc2NyaWJlLFxyXG4gICAgICBhcmlhUm9sZTogdGhpcy5hcmlhUm9sZSxcclxuICAgICAgYXBwbHlDbGFzczogdGhpcy5hcHBseUNsYXNzLFxyXG4gICAgICBhcHBseUFycm93Q2xhc3M6IHRoaXMuYXBwbHlBcnJvd0NsYXNzLFxyXG4gICAgICBoaWRlT25Nb3VzZUxlYXZlOiB0aGlzLmhpZGVPbk1vdXNlTGVhdmUsXHJcbiAgICAgIHN0eWxlczogdGhpcy5zdHlsZXMsXHJcbiAgICAgIGFwcGVuZFRvOiB0aGlzLmFwcGVuZFRvLFxyXG4gICAgICBwcmV2ZW50T3ZlcmZsb3c6IHRoaXMucHJldmVudE92ZXJmbG93LFxyXG4gICAgfSk7XHJcbiAgICBwb3BwZXJSZWYub25VcGRhdGUgPSB0aGlzLm9uUG9wcGVyVXBkYXRlLmJpbmQodGhpcyk7XHJcbiAgICB0aGlzLnN1YnNjcmlwdGlvbnMucHVzaChwb3BwZXJSZWYub25IaWRkZW4uc3Vic2NyaWJlKHRoaXMuaGlkZS5iaW5kKHRoaXMpKSk7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIGdldFNjcm9sbFBhcmVudChub2RlKSB7XHJcbiAgICBjb25zdCBpc0VsZW1lbnQgPSBub2RlIGluc3RhbmNlb2YgSFRNTEVsZW1lbnQ7XHJcbiAgICBjb25zdCBvdmVyZmxvd1kgPSBpc0VsZW1lbnQgJiYgd2luZG93LmdldENvbXB1dGVkU3R5bGUobm9kZSkub3ZlcmZsb3dZO1xyXG4gICAgY29uc3QgaXNTY3JvbGxhYmxlID0gb3ZlcmZsb3dZICE9PSAndmlzaWJsZScgJiYgb3ZlcmZsb3dZICE9PSAnaGlkZGVuJztcclxuXHJcbiAgICBpZiAoIW5vZGUpIHtcclxuICAgICAgcmV0dXJuIG51bGw7XHJcbiAgICB9IGVsc2UgaWYgKGlzU2Nyb2xsYWJsZSAmJiBub2RlLnNjcm9sbEhlaWdodCA+PSBub2RlLmNsaWVudEhlaWdodCkge1xyXG4gICAgICByZXR1cm4gbm9kZTtcclxuICAgIH1cclxuXHJcbiAgICByZXR1cm4gdGhpcy5nZXRTY3JvbGxQYXJlbnQobm9kZS5wYXJlbnROb2RlKSB8fCBkb2N1bWVudDtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgb25Qb3BwZXJVcGRhdGUoZXZlbnQpIHtcclxuICAgIHRoaXMucG9wcGVyT25VcGRhdGUuZW1pdChldmVudCk7XHJcbiAgfVxyXG5cclxufVxyXG4iXX0=