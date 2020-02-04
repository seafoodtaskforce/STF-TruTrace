import Popper from 'popper.js';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Renderer2, ViewChild, ViewContainerRef, ViewEncapsulation, Directive, ComponentFactoryResolver, Input, Output, Inject, NgModule } from '@angular/core';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
class Triggers {
}
Triggers.CLICK = 'click';
Triggers.HOVER = 'hover';
Triggers.MOUSEDOWN = 'mousedown';
Triggers.NONE = 'none';
class Placements {
}
Placements.Top = 'top';
Placements.Bottom = 'bottom';
Placements.Left = 'left';
Placements.Right = 'right';
Placements.TopStart = 'top-start';
Placements.BottomStart = 'bottom-start';
Placements.LeftStart = 'left-start';
Placements.RightStart = 'right-start';
Placements.TopEnd = 'top-end';
Placements.BottomEnd = 'bottom-end';
Placements.LeftEnd = 'left-end';
Placements.RightEnd = 'right-end';
Placements.Auto = 'auto';
Placements.AutoStart = 'auto-start';
Placements.AutoEnd = 'auto-end';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
class PopperContent {
    /**
     * @param {?} elemRef
     * @param {?} renderer
     * @param {?} viewRef
     * @param {?} CDR
     */
    constructor(elemRef, renderer, viewRef, CDR) {
        this.elemRef = elemRef;
        this.renderer = renderer;
        this.viewRef = viewRef;
        this.CDR = CDR;
        this.popperOptions = (/** @type {?} */ ({
            disableAnimation: false,
            disableDefaultStyling: false,
            placement: Placements.Auto,
            boundariesElement: '',
            trigger: Triggers.HOVER,
            positionFixed: false,
            appendToBody: false,
            popperModifiers: {}
        }));
        this.isMouseOver = false;
        this.onHidden = new EventEmitter();
        this.displayType = "none";
        this.opacity = 0;
        this.ariaHidden = 'true';
        this.arrowColor = null;
        this.state = true;
    }
    /**
     * @return {?}
     */
    onMouseOver() {
        this.isMouseOver = true;
    }
    /**
     * @return {?}
     */
    showOnLeave() {
        this.isMouseOver = false;
        if (this.popperOptions.trigger !== Triggers.HOVER && !this.popperOptions.hideOnMouseLeave) {
            return;
        }
        this.hide();
    }
    /**
     * @return {?}
     */
    onDocumentResize() {
        this.update();
    }
    /**
     * @return {?}
     */
    ngOnDestroy() {
        this.clean();
        if (this.popperOptions.appendTo && this.elemRef && this.elemRef.nativeElement && this.elemRef.nativeElement.parentNode) {
            this.viewRef.detach();
            this.elemRef.nativeElement.parentNode.removeChild(this.elemRef.nativeElement);
        }
    }
    /**
     * @return {?}
     */
    clean() {
        this.toggleVisibility(false);
        if (!this.popperInstance) {
            return;
        }
        ((/** @type {?} */ (this.popperInstance))).disableEventListeners();
        this.popperInstance.destroy();
    }
    /**
     * @return {?}
     */
    show() {
        if (!this.referenceObject) {
            return;
        }
        /** @type {?} */
        const appendToParent = this.popperOptions.appendTo && document.querySelector(this.popperOptions.appendTo);
        if (appendToParent && this.elemRef.nativeElement.parentNode !== appendToParent) {
            this.elemRef.nativeElement.parentNode && this.elemRef.nativeElement.parentNode.removeChild(this.elemRef.nativeElement);
            appendToParent.appendChild(this.elemRef.nativeElement);
        }
        /** @type {?} */
        let popperOptions = (/** @type {?} */ ({
            placement: this.popperOptions.placement,
            positionFixed: this.popperOptions.positionFixed,
            modifiers: {
                arrow: {
                    element: this.popperViewRef.nativeElement.querySelector('.ngxp__arrow')
                }
            }
        }));
        if (this.onUpdate) {
            popperOptions.onUpdate = (/** @type {?} */ (this.onUpdate));
        }
        /** @type {?} */
        let boundariesElement = this.popperOptions.boundariesElement && document.querySelector(this.popperOptions.boundariesElement);
        if (popperOptions.modifiers && boundariesElement) {
            popperOptions.modifiers.preventOverflow = { boundariesElement };
        }
        if (popperOptions.modifiers && this.popperOptions.preventOverflow !== undefined) {
            popperOptions.modifiers.preventOverflow = popperOptions.modifiers.preventOverflow || {};
            popperOptions.modifiers.preventOverflow.enabled = this.popperOptions.preventOverflow;
            if (!popperOptions.modifiers.preventOverflow.enabled) {
                popperOptions.modifiers.hide = { enabled: false };
            }
        }
        this.determineArrowColor();
        popperOptions.modifiers = Object.assign(popperOptions.modifiers, this.popperOptions.popperModifiers);
        this.popperInstance = new Popper(this.referenceObject, this.popperViewRef.nativeElement, popperOptions);
        ((/** @type {?} */ (this.popperInstance))).enableEventListeners();
        this.scheduleUpdate();
        this.toggleVisibility(true);
        this.globalResize = this.renderer.listen('document', 'resize', this.onDocumentResize.bind(this));
    }
    /**
     * @private
     * @return {?}
     */
    determineArrowColor() {
        ['background-color', 'backgroundColor'].some((clr) => {
            if (!this.popperOptions.styles) {
                return false;
            }
            if (this.popperOptions.styles.hasOwnProperty(clr)) {
                this.arrowColor = this.popperOptions.styles[clr];
                return true;
            }
            return false;
        });
    }
    /**
     * @return {?}
     */
    update() {
        this.popperInstance && ((/** @type {?} */ (this.popperInstance))).update();
    }
    /**
     * @return {?}
     */
    scheduleUpdate() {
        this.popperInstance && ((/** @type {?} */ (this.popperInstance))).scheduleUpdate();
    }
    /**
     * @return {?}
     */
    hide() {
        if (this.popperInstance) {
            this.popperInstance.destroy();
        }
        this.toggleVisibility(false);
        this.onHidden.emit();
    }
    /**
     * @param {?} state
     * @return {?}
     */
    toggleVisibility(state) {
        if (!state) {
            this.opacity = 0;
            this.displayType = "none";
            this.ariaHidden = 'true';
        }
        else {
            this.opacity = 1;
            this.displayType = "block";
            this.ariaHidden = 'false';
        }
        if (!this.CDR['destroyed']) {
            this.CDR.detectChanges();
        }
    }
    /**
     * @param {?=} classList
     * @return {?}
     */
    extractAppliedClassListExpr(classList) {
        if (!classList || typeof classList !== 'string') {
            return null;
        }
        try {
            return classList
                .replace(/ /, '')
                .split(',')
                .reduce((acc, clss) => {
                acc[clss] = true;
                return acc;
            }, {});
        }
        catch (e) {
            return null;
        }
    }
    /**
     * @private
     * @return {?}
     */
    clearGlobalResize() {
        this.globalResize && typeof this.globalResize === 'function' && this.globalResize();
    }
}
PopperContent.decorators = [
    { type: Component, args: [{
                selector: "popper-content",
                encapsulation: ViewEncapsulation.None,
                changeDetection: ChangeDetectionStrategy.OnPush,
                template: `
    <div #popperViewRef
         [class.ngxp__container]="!popperOptions.disableDefaultStyling"
         [class.ngxp__animation]="!popperOptions.disableAnimation"
         [style.display]="displayType"
         [style.opacity]="opacity"
         [ngStyle]="popperOptions.styles"
         [ngClass]="extractAppliedClassListExpr(popperOptions.applyClass)"
         attr.aria-hidden="{{ariaHidden}}"
         [attr.aria-describedby]="popperOptions.ariaDescribe || null"
         attr.role="{{popperOptions.ariaRole}}">
      <div class="ngxp__inner" *ngIf="text" [innerHTML]="text">
        <ng-content></ng-content>
      </div>
      <div class="ngxp__inner" *ngIf="!text">
        <ng-content></ng-content>
      </div>
      <div class="ngxp__arrow" [style.border-color]="arrowColor" [class.__force-arrow]="arrowColor"
           [ngClass]="extractAppliedClassListExpr(popperOptions.applyArrowClass)"></div>

    </div>
  `,
                styles: [".ngxp__container{display:none;position:absolute;border-radius:3px;border:1px solid grey;box-shadow:0 0 2px rgba(0,0,0,.5);padding:10px}.ngxp__container.ngxp__animation{-webkit-animation:150ms ease-out ngxp-fadeIn;animation:150ms ease-out ngxp-fadeIn}.ngxp__container>.ngxp__arrow{border-color:grey;width:0;height:0;border-style:solid;position:absolute;margin:5px}.ngxp__container[x-placement^=bottom],.ngxp__container[x-placement^=left],.ngxp__container[x-placement^=right],.ngxp__container[x-placement^=top]{display:block}.ngxp__container[x-placement^=top]{margin-bottom:5px}.ngxp__container[x-placement^=top]>.ngxp__arrow{border-width:5px 5px 0;border-right-color:transparent;border-bottom-color:transparent;border-left-color:transparent;bottom:-5px;left:calc(50% - 5px);margin-top:0;margin-bottom:0}.ngxp__container[x-placement^=top]>.ngxp__arrow.__force-arrow{border-right-color:transparent!important;border-bottom-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=bottom]{margin-top:5px}.ngxp__container[x-placement^=bottom]>.ngxp__arrow{border-width:0 5px 5px;border-top-color:transparent;border-right-color:transparent;border-left-color:transparent;top:-5px;left:calc(50% - 5px);margin-top:0;margin-bottom:0}.ngxp__container[x-placement^=bottom]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-right-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=right]{margin-left:5px}.ngxp__container[x-placement^=right]>.ngxp__arrow{border-width:5px 5px 5px 0;border-top-color:transparent;border-bottom-color:transparent;border-left-color:transparent;left:-5px;top:calc(50% - 5px);margin-left:0;margin-right:0}.ngxp__container[x-placement^=right]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-bottom-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=left]{margin-right:5px}.ngxp__container[x-placement^=left]>.ngxp__arrow{border-width:5px 0 5px 5px;border-top-color:transparent;border-bottom-color:transparent;border-right-color:transparent;right:-5px;top:calc(50% - 5px);margin-left:0;margin-right:0}.ngxp__container[x-placement^=left]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-bottom-color:transparent!important;border-right-color:transparent!important}@-webkit-keyframes ngxp-fadeIn{0%{display:none;opacity:0}1%{display:block;opacity:0}100%{display:block;opacity:1}}@keyframes ngxp-fadeIn{0%{display:none;opacity:0}1%{display:block;opacity:0}100%{display:block;opacity:1}}"]
            }] }
];
/** @nocollapse */
PopperContent.ctorParameters = () => [
    { type: ElementRef },
    { type: Renderer2 },
    { type: ViewContainerRef },
    { type: ChangeDetectorRef }
];
PopperContent.propDecorators = {
    popperViewRef: [{ type: ViewChild, args: ["popperViewRef",] }],
    onMouseOver: [{ type: HostListener, args: ['mouseover',] }],
    showOnLeave: [{ type: HostListener, args: ['mouseleave',] }]
};

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
class PopperController {
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

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
const ɵ0 = {};
class NgxPopperModule {
    /**
     * @return {?}
     */
    ngDoBootstrap() {
    }
    /**
     * @param {?=} popperBaseOptions
     * @return {?}
     */
    static forRoot(popperBaseOptions = {}) {
        return { ngModule: NgxPopperModule, providers: [{ provide: 'popperDefaults', useValue: popperBaseOptions }] };
    }
}
NgxPopperModule.decorators = [
    { type: NgModule, args: [{
                imports: [
                    CommonModule
                ],
                declarations: [
                    PopperController,
                    PopperContent
                ],
                exports: [
                    PopperController,
                    PopperContent
                ],
                entryComponents: [
                    PopperContent
                ],
                providers: [
                    {
                        provide: 'popperDefaults', useValue: ɵ0
                    }
                ]
            },] }
];

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */

export { Triggers, Placements, PopperController, PopperContent, NgxPopperModule };

//# sourceMappingURL=ngx-popper.js.map