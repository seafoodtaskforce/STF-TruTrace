import { __assign, __values } from 'tslib';
import Popper from 'popper.js';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Renderer2, ViewChild, ViewContainerRef, ViewEncapsulation, Directive, ComponentFactoryResolver, Input, Output, Inject, NgModule } from '@angular/core';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
var Triggers = /** @class */ (function () {
    function Triggers() {
    }
    Triggers.CLICK = 'click';
    Triggers.HOVER = 'hover';
    Triggers.MOUSEDOWN = 'mousedown';
    Triggers.NONE = 'none';
    return Triggers;
}());
var Placements = /** @class */ (function () {
    function Placements() {
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
    return Placements;
}());

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
var PopperContent = /** @class */ (function () {
    function PopperContent(elemRef, renderer, viewRef, CDR) {
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
    PopperContent.prototype.onMouseOver = /**
     * @return {?}
     */
    function () {
        this.isMouseOver = true;
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.showOnLeave = /**
     * @return {?}
     */
    function () {
        this.isMouseOver = false;
        if (this.popperOptions.trigger !== Triggers.HOVER && !this.popperOptions.hideOnMouseLeave) {
            return;
        }
        this.hide();
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.onDocumentResize = /**
     * @return {?}
     */
    function () {
        this.update();
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.ngOnDestroy = /**
     * @return {?}
     */
    function () {
        this.clean();
        if (this.popperOptions.appendTo && this.elemRef && this.elemRef.nativeElement && this.elemRef.nativeElement.parentNode) {
            this.viewRef.detach();
            this.elemRef.nativeElement.parentNode.removeChild(this.elemRef.nativeElement);
        }
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.clean = /**
     * @return {?}
     */
    function () {
        this.toggleVisibility(false);
        if (!this.popperInstance) {
            return;
        }
        ((/** @type {?} */ (this.popperInstance))).disableEventListeners();
        this.popperInstance.destroy();
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.show = /**
     * @return {?}
     */
    function () {
        if (!this.referenceObject) {
            return;
        }
        /** @type {?} */
        var appendToParent = this.popperOptions.appendTo && document.querySelector(this.popperOptions.appendTo);
        if (appendToParent && this.elemRef.nativeElement.parentNode !== appendToParent) {
            this.elemRef.nativeElement.parentNode && this.elemRef.nativeElement.parentNode.removeChild(this.elemRef.nativeElement);
            appendToParent.appendChild(this.elemRef.nativeElement);
        }
        /** @type {?} */
        var popperOptions = (/** @type {?} */ ({
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
        var boundariesElement = this.popperOptions.boundariesElement && document.querySelector(this.popperOptions.boundariesElement);
        if (popperOptions.modifiers && boundariesElement) {
            popperOptions.modifiers.preventOverflow = { boundariesElement: boundariesElement };
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
    };
    /**
     * @private
     * @return {?}
     */
    PopperContent.prototype.determineArrowColor = /**
     * @private
     * @return {?}
     */
    function () {
        var _this = this;
        ['background-color', 'backgroundColor'].some(function (clr) {
            if (!_this.popperOptions.styles) {
                return false;
            }
            if (_this.popperOptions.styles.hasOwnProperty(clr)) {
                _this.arrowColor = _this.popperOptions.styles[clr];
                return true;
            }
            return false;
        });
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.update = /**
     * @return {?}
     */
    function () {
        this.popperInstance && ((/** @type {?} */ (this.popperInstance))).update();
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.scheduleUpdate = /**
     * @return {?}
     */
    function () {
        this.popperInstance && ((/** @type {?} */ (this.popperInstance))).scheduleUpdate();
    };
    /**
     * @return {?}
     */
    PopperContent.prototype.hide = /**
     * @return {?}
     */
    function () {
        if (this.popperInstance) {
            this.popperInstance.destroy();
        }
        this.toggleVisibility(false);
        this.onHidden.emit();
    };
    /**
     * @param {?} state
     * @return {?}
     */
    PopperContent.prototype.toggleVisibility = /**
     * @param {?} state
     * @return {?}
     */
    function (state) {
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
    };
    /**
     * @param {?=} classList
     * @return {?}
     */
    PopperContent.prototype.extractAppliedClassListExpr = /**
     * @param {?=} classList
     * @return {?}
     */
    function (classList) {
        if (!classList || typeof classList !== 'string') {
            return null;
        }
        try {
            return classList
                .replace(/ /, '')
                .split(',')
                .reduce(function (acc, clss) {
                acc[clss] = true;
                return acc;
            }, {});
        }
        catch (e) {
            return null;
        }
    };
    /**
     * @private
     * @return {?}
     */
    PopperContent.prototype.clearGlobalResize = /**
     * @private
     * @return {?}
     */
    function () {
        this.globalResize && typeof this.globalResize === 'function' && this.globalResize();
    };
    PopperContent.decorators = [
        { type: Component, args: [{
                    selector: "popper-content",
                    encapsulation: ViewEncapsulation.None,
                    changeDetection: ChangeDetectionStrategy.OnPush,
                    template: "\n    <div #popperViewRef\n         [class.ngxp__container]=\"!popperOptions.disableDefaultStyling\"\n         [class.ngxp__animation]=\"!popperOptions.disableAnimation\"\n         [style.display]=\"displayType\"\n         [style.opacity]=\"opacity\"\n         [ngStyle]=\"popperOptions.styles\"\n         [ngClass]=\"extractAppliedClassListExpr(popperOptions.applyClass)\"\n         attr.aria-hidden=\"{{ariaHidden}}\"\n         [attr.aria-describedby]=\"popperOptions.ariaDescribe || null\"\n         attr.role=\"{{popperOptions.ariaRole}}\">\n      <div class=\"ngxp__inner\" *ngIf=\"text\" [innerHTML]=\"text\">\n        <ng-content></ng-content>\n      </div>\n      <div class=\"ngxp__inner\" *ngIf=\"!text\">\n        <ng-content></ng-content>\n      </div>\n      <div class=\"ngxp__arrow\" [style.border-color]=\"arrowColor\" [class.__force-arrow]=\"arrowColor\"\n           [ngClass]=\"extractAppliedClassListExpr(popperOptions.applyArrowClass)\"></div>\n\n    </div>\n  ",
                    styles: [".ngxp__container{display:none;position:absolute;border-radius:3px;border:1px solid grey;box-shadow:0 0 2px rgba(0,0,0,.5);padding:10px}.ngxp__container.ngxp__animation{-webkit-animation:150ms ease-out ngxp-fadeIn;animation:150ms ease-out ngxp-fadeIn}.ngxp__container>.ngxp__arrow{border-color:grey;width:0;height:0;border-style:solid;position:absolute;margin:5px}.ngxp__container[x-placement^=bottom],.ngxp__container[x-placement^=left],.ngxp__container[x-placement^=right],.ngxp__container[x-placement^=top]{display:block}.ngxp__container[x-placement^=top]{margin-bottom:5px}.ngxp__container[x-placement^=top]>.ngxp__arrow{border-width:5px 5px 0;border-right-color:transparent;border-bottom-color:transparent;border-left-color:transparent;bottom:-5px;left:calc(50% - 5px);margin-top:0;margin-bottom:0}.ngxp__container[x-placement^=top]>.ngxp__arrow.__force-arrow{border-right-color:transparent!important;border-bottom-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=bottom]{margin-top:5px}.ngxp__container[x-placement^=bottom]>.ngxp__arrow{border-width:0 5px 5px;border-top-color:transparent;border-right-color:transparent;border-left-color:transparent;top:-5px;left:calc(50% - 5px);margin-top:0;margin-bottom:0}.ngxp__container[x-placement^=bottom]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-right-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=right]{margin-left:5px}.ngxp__container[x-placement^=right]>.ngxp__arrow{border-width:5px 5px 5px 0;border-top-color:transparent;border-bottom-color:transparent;border-left-color:transparent;left:-5px;top:calc(50% - 5px);margin-left:0;margin-right:0}.ngxp__container[x-placement^=right]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-bottom-color:transparent!important;border-left-color:transparent!important}.ngxp__container[x-placement^=left]{margin-right:5px}.ngxp__container[x-placement^=left]>.ngxp__arrow{border-width:5px 0 5px 5px;border-top-color:transparent;border-bottom-color:transparent;border-right-color:transparent;right:-5px;top:calc(50% - 5px);margin-left:0;margin-right:0}.ngxp__container[x-placement^=left]>.ngxp__arrow.__force-arrow{border-top-color:transparent!important;border-bottom-color:transparent!important;border-right-color:transparent!important}@-webkit-keyframes ngxp-fadeIn{0%{display:none;opacity:0}1%{display:block;opacity:0}100%{display:block;opacity:1}}@keyframes ngxp-fadeIn{0%{display:none;opacity:0}1%{display:block;opacity:0}100%{display:block;opacity:1}}"]
                }] }
    ];
    /** @nocollapse */
    PopperContent.ctorParameters = function () { return [
        { type: ElementRef },
        { type: Renderer2 },
        { type: ViewContainerRef },
        { type: ChangeDetectorRef }
    ]; };
    PopperContent.propDecorators = {
        popperViewRef: [{ type: ViewChild, args: ["popperViewRef",] }],
        onMouseOver: [{ type: HostListener, args: ['mouseover',] }],
        showOnLeave: [{ type: HostListener, args: ['mouseleave',] }]
    };
    return PopperContent;
}());

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
var PopperController = /** @class */ (function () {
    function PopperController(viewContainerRef, changeDetectorRef, resolver, elementRef, renderer, popperDefaults) {
        if (popperDefaults === void 0) { popperDefaults = {}; }
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
        PopperController.baseOptions = __assign({}, PopperController.baseOptions, this.popperDefaults);
    }
    /**
     * @param {?} $event
     * @return {?}
     */
    PopperController.prototype.hideOnClickOutsideHandler = /**
     * @param {?} $event
     * @return {?}
     */
    function ($event) {
        if (this.disabled || !this.hideOnClickOutside || $event.srcElement &&
            $event.srcElement === this.popperContent.elemRef.nativeElement ||
            this.popperContent.elemRef.nativeElement.contains($event.srcElement)) {
            return;
        }
        this.scheduledHide($event, this.hideTimeout);
    };
    /**
     * @param {?} $event
     * @return {?}
     */
    PopperController.prototype.hideOnScrollHandler = /**
     * @param {?} $event
     * @return {?}
     */
    function ($event) {
        if (this.disabled || !this.hideOnScroll) {
            return;
        }
        this.scheduledHide($event, this.hideTimeout);
    };
    /**
     * @return {?}
     */
    PopperController.prototype.applyTriggerListeners = /**
     * @return {?}
     */
    function () {
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
    };
    /**
     * @param {?} target
     * @param {...?} sources
     * @return {?}
     */
    PopperController.assignDefined = /**
     * @param {?} target
     * @param {...?} sources
     * @return {?}
     */
    function (target) {
        var sources = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            sources[_i - 1] = arguments[_i];
        }
        var e_1, _a, e_2, _b;
        try {
            for (var sources_1 = __values(sources), sources_1_1 = sources_1.next(); !sources_1_1.done; sources_1_1 = sources_1.next()) {
                var source = sources_1_1.value;
                try {
                    for (var _c = __values(Object.keys(source)), _d = _c.next(); !_d.done; _d = _c.next()) {
                        var key = _d.value;
                        /** @type {?} */
                        var val = source[key];
                        if (val !== undefined) {
                            target[key] = val;
                        }
                    }
                }
                catch (e_2_1) { e_2 = { error: e_2_1 }; }
                finally {
                    try {
                        if (_d && !_d.done && (_b = _c.return)) _b.call(_c);
                    }
                    finally { if (e_2) throw e_2.error; }
                }
            }
        }
        catch (e_1_1) { e_1 = { error: e_1_1 }; }
        finally {
            try {
                if (sources_1_1 && !sources_1_1.done && (_a = sources_1.return)) _a.call(sources_1);
            }
            finally { if (e_1) throw e_1.error; }
        }
        return target;
    };
    /**
     * @return {?}
     */
    PopperController.prototype.ngOnInit = /**
     * @return {?}
     */
    function () {
        //Support legacy prop
        this.hideOnClickOutside = typeof this.hideOnClickOutside === 'undefined' ?
            this.closeOnClickOutside : this.hideOnClickOutside;
        if (typeof this.content === 'string') {
            /** @type {?} */
            var text = this.content;
            this.popperContent = this.constructContent();
            this.popperContent.text = text;
        }
        else {
            this.popperContent = this.content;
        }
        /** @type {?} */
        var popperRef = this.popperContent;
        popperRef.referenceObject = this.getRefElement();
        this.setContentProperties(popperRef);
        this.setDefaults();
        this.applyTriggerListeners();
        if (this.showOnStart) {
            this.scheduledShow();
        }
    };
    /**
     * @param {?} changes
     * @return {?}
     */
    PopperController.prototype.ngOnChanges = /**
     * @param {?} changes
     * @return {?}
     */
    function (changes) {
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
    };
    /**
     * @return {?}
     */
    PopperController.prototype.ngOnDestroy = /**
     * @return {?}
     */
    function () {
        this.subscriptions.forEach(function (sub) { return sub.unsubscribe && sub.unsubscribe(); });
        this.subscriptions.length = 0;
        this.clearEventListeners();
        this.clearGlobalEventListeners();
        clearTimeout(this.scheduledShowTimeout);
        clearTimeout(this.scheduledHideTimeout);
        this.popperContent && this.popperContent.clean();
    };
    /**
     * @return {?}
     */
    PopperController.prototype.toggle = /**
     * @return {?}
     */
    function () {
        if (this.disabled) {
            return;
        }
        this.shown ? this.scheduledHide(null, this.hideTimeout) : this.scheduledShow();
    };
    /**
     * @return {?}
     */
    PopperController.prototype.show = /**
     * @return {?}
     */
    function () {
        if (this.shown) {
            this.overrideHideTimeout();
            return;
        }
        this.shown = true;
        /** @type {?} */
        var popperRef = this.popperContent;
        /** @type {?} */
        var element = this.getRefElement();
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
    };
    /**
     * @return {?}
     */
    PopperController.prototype.hide = /**
     * @return {?}
     */
    function () {
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
    };
    /**
     * @param {?=} delay
     * @return {?}
     */
    PopperController.prototype.scheduledShow = /**
     * @param {?=} delay
     * @return {?}
     */
    function (delay) {
        var _this = this;
        if (delay === void 0) { delay = this.showDelay; }
        if (this.disabled) {
            return;
        }
        this.overrideHideTimeout();
        this.scheduledShowTimeout = setTimeout(function () {
            _this.show();
            _this.applyChanges();
        }, delay);
    };
    /**
     * @param {?=} $event
     * @param {?=} delay
     * @return {?}
     */
    PopperController.prototype.scheduledHide = /**
     * @param {?=} $event
     * @param {?=} delay
     * @return {?}
     */
    function ($event, delay) {
        var _this = this;
        if ($event === void 0) { $event = null; }
        if (delay === void 0) { delay = this.hideTimeout; }
        if (this.disabled) {
            return;
        }
        this.overrideShowTimeout();
        this.scheduledHideTimeout = setTimeout(function () {
            /** @type {?} */
            var toElement = $event ? $event.toElement : null;
            /** @type {?} */
            var popperContentView = _this.popperContent.popperViewRef ? _this.popperContent.popperViewRef.nativeElement : false;
            if (!popperContentView || popperContentView === toElement || popperContentView.contains(toElement) || ((/** @type {?} */ (_this.content))).isMouseOver) {
                return;
            }
            _this.hide();
            _this.applyChanges();
        }, delay);
    };
    /**
     * @return {?}
     */
    PopperController.prototype.getRefElement = /**
     * @return {?}
     */
    function () {
        return this.targetElement || this.viewContainerRef.element.nativeElement;
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.applyChanges = /**
     * @private
     * @return {?}
     */
    function () {
        this.changeDetectorRef.markForCheck();
        this.changeDetectorRef.detectChanges();
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.setDefaults = /**
     * @private
     * @return {?}
     */
    function () {
        this.showDelay = typeof this.showDelay === 'undefined' ? PopperController.baseOptions.showDelay : this.showDelay;
        this.showTrigger = typeof this.showTrigger === 'undefined' ? PopperController.baseOptions.trigger : this.showTrigger;
        this.hideOnClickOutside = typeof this.hideOnClickOutside === 'undefined' ? PopperController.baseOptions.hideOnClickOutside : this.hideOnClickOutside;
        this.hideOnScroll = typeof this.hideOnScroll === 'undefined' ? PopperController.baseOptions.hideOnScroll : this.hideOnScroll;
        this.hideOnMouseLeave = typeof this.hideOnMouseLeave === 'undefined' ? PopperController.baseOptions.hideOnMouseLeave : this.hideOnMouseLeave;
        this.ariaRole = typeof this.ariaRole === 'undefined' ? PopperController.baseOptions.ariaRole : this.ariaRole;
        this.ariaDescribe = typeof this.ariaDescribe === 'undefined' ? PopperController.baseOptions.ariaDescribe : this.ariaDescribe;
        this.styles = typeof this.styles === 'undefined' ? Object.assign({}, PopperController.baseOptions.styles) : this.styles;
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.clearEventListeners = /**
     * @private
     * @return {?}
     */
    function () {
        this.eventListeners.forEach(function (evt) {
            evt && typeof evt === 'function' && evt();
        });
        this.eventListeners.length = 0;
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.clearGlobalEventListeners = /**
     * @private
     * @return {?}
     */
    function () {
        this.globalEventListeners.forEach(function (evt) {
            evt && typeof evt === 'function' && evt();
        });
        this.globalEventListeners.length = 0;
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.overrideShowTimeout = /**
     * @private
     * @return {?}
     */
    function () {
        if (this.scheduledShowTimeout) {
            clearTimeout(this.scheduledShowTimeout);
            this.scheduledHideTimeout = 0;
        }
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.overrideHideTimeout = /**
     * @private
     * @return {?}
     */
    function () {
        if (this.scheduledHideTimeout) {
            clearTimeout(this.scheduledHideTimeout);
            this.scheduledHideTimeout = 0;
        }
    };
    /**
     * @private
     * @return {?}
     */
    PopperController.prototype.constructContent = /**
     * @private
     * @return {?}
     */
    function () {
        /** @type {?} */
        var factory = this.resolver.resolveComponentFactory(this.popperContentClass);
        this.popperContentRef = this.viewContainerRef.createComponent(factory);
        return (/** @type {?} */ (this.popperContentRef.instance));
    };
    /**
     * @private
     * @param {?} popperRef
     * @return {?}
     */
    PopperController.prototype.setContentProperties = /**
     * @private
     * @param {?} popperRef
     * @return {?}
     */
    function (popperRef) {
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
    };
    /**
     * @private
     * @param {?} node
     * @return {?}
     */
    PopperController.prototype.getScrollParent = /**
     * @private
     * @param {?} node
     * @return {?}
     */
    function (node) {
        /** @type {?} */
        var isElement = node instanceof HTMLElement;
        /** @type {?} */
        var overflowY = isElement && window.getComputedStyle(node).overflowY;
        /** @type {?} */
        var isScrollable = overflowY !== 'visible' && overflowY !== 'hidden';
        if (!node) {
            return null;
        }
        else if (isScrollable && node.scrollHeight >= node.clientHeight) {
            return node;
        }
        return this.getScrollParent(node.parentNode) || document;
    };
    /**
     * @private
     * @param {?} event
     * @return {?}
     */
    PopperController.prototype.onPopperUpdate = /**
     * @private
     * @param {?} event
     * @return {?}
     */
    function (event) {
        this.popperOnUpdate.emit(event);
    };
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
    PopperController.ctorParameters = function () { return [
        { type: ViewContainerRef },
        { type: ChangeDetectorRef },
        { type: ComponentFactoryResolver },
        { type: ElementRef },
        { type: Renderer2 },
        { type: undefined, decorators: [{ type: Inject, args: ['popperDefaults',] }] }
    ]; };
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
    return PopperController;
}());

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
var ɵ0 = {};
var NgxPopperModule = /** @class */ (function () {
    function NgxPopperModule() {
    }
    /**
     * @return {?}
     */
    NgxPopperModule.prototype.ngDoBootstrap = /**
     * @return {?}
     */
    function () {
    };
    /**
     * @param {?=} popperBaseOptions
     * @return {?}
     */
    NgxPopperModule.forRoot = /**
     * @param {?=} popperBaseOptions
     * @return {?}
     */
    function (popperBaseOptions) {
        if (popperBaseOptions === void 0) { popperBaseOptions = {}; }
        return { ngModule: NgxPopperModule, providers: [{ provide: 'popperDefaults', useValue: popperBaseOptions }] };
    };
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
    return NgxPopperModule;
}());

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