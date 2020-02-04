/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
import * as tslib_1 from "tslib";
import { Directive, ViewContainerRef, ComponentFactoryResolver, Input, Output, EventEmitter, Renderer2, ChangeDetectorRef, Inject, ElementRef } from '@angular/core';
import { Placements, Triggers } from './popper-model';
import { PopperContent } from './popper-content';
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
        PopperController.baseOptions = tslib_1.__assign({}, PopperController.baseOptions, this.popperDefaults);
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
            for (var sources_1 = tslib_1.__values(sources), sources_1_1 = sources_1.next(); !sources_1_1.done; sources_1_1 = sources_1.next()) {
                var source = sources_1_1.value;
                try {
                    for (var _c = tslib_1.__values(Object.keys(source)), _d = _c.next(); !_d.done; _d = _c.next()) {
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
export { PopperController };
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicG9wcGVyLWRpcmVjdGl2ZS5qcyIsInNvdXJjZVJvb3QiOiJuZzovL25neC1wb3BwZXIvIiwic291cmNlcyI6WyJzcmMvcG9wcGVyLWRpcmVjdGl2ZS50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7OztBQUFBLE9BQU8sRUFDTCxTQUFTLEVBRVQsZ0JBQWdCLEVBQ2hCLHdCQUF3QixFQUN4QixLQUFLLEVBR0wsTUFBTSxFQUVOLFlBQVksRUFBVSxTQUFTLEVBQUUsaUJBQWlCLEVBQUUsTUFBTSxFQUFFLFVBQVUsRUFDdkUsTUFBTSxlQUFlLENBQUM7QUFDdkIsT0FBTyxFQUFZLFVBQVUsRUFBaUMsUUFBUSxFQUFDLE1BQU0sZ0JBQWdCLENBQUM7QUFDOUYsT0FBTyxFQUFDLGFBQWEsRUFBQyxNQUFNLGtCQUFrQixDQUFDO0FBRS9DO0lBZUUsMEJBQW9CLGdCQUFrQyxFQUNsQyxpQkFBb0MsRUFDcEMsUUFBa0MsRUFDbEMsVUFBc0IsRUFDdEIsUUFBbUIsRUFDTyxjQUF5QztRQUF6QywrQkFBQSxFQUFBLG1CQUF5QztRQUxuRSxxQkFBZ0IsR0FBaEIsZ0JBQWdCLENBQWtCO1FBQ2xDLHNCQUFpQixHQUFqQixpQkFBaUIsQ0FBbUI7UUFDcEMsYUFBUSxHQUFSLFFBQVEsQ0FBMEI7UUFDbEMsZUFBVSxHQUFWLFVBQVUsQ0FBWTtRQUN0QixhQUFRLEdBQVIsUUFBUSxDQUFXO1FBQ08sbUJBQWMsR0FBZCxjQUFjLENBQTJCO1FBZi9FLHVCQUFrQixHQUFHLGFBQWEsQ0FBQztRQUVuQyxVQUFLLEdBQVksS0FBSyxDQUFDO1FBR3ZCLGtCQUFhLEdBQVUsRUFBRSxDQUFDO1FBQzFCLG1CQUFjLEdBQVUsRUFBRSxDQUFDO1FBQzNCLHlCQUFvQixHQUFVLEVBQUUsQ0FBQztRQTRDekMsZ0JBQVcsR0FBVyxDQUFDLENBQUM7UUFHeEIscUJBQWdCLEdBQVcsQ0FBQyxDQUFDO1FBc0Q3QixrQkFBYSxHQUFtQyxJQUFJLFlBQVksRUFBb0IsQ0FBQztRQUdyRixtQkFBYyxH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Qjs7SUFFMUIsdUNBQ3FCOztJQUVyQiwrQ0FDNkI7O0lBRTdCLDhDQUN3Qzs7SUFFeEMsd0NBQ2tDOztJQUVsQyw0Q0FDc0M7O0lBRXRDLHlDQUN1Qjs7SUFFdkIsMkNBQ29COztJQUVwQix3Q0FDc0I7O0lBRXRCLDRDQUMwQjs7SUFFMUIsc0NBQ21COztJQUVuQiwyQ0FDd0I7O0lBRXhCLHdDQUNpQzs7SUFFakMsb0NBQzZCOztJQUU3QixrQ0FDMkI7O0lBRTNCLG9DQUNpQjs7SUFFakIsMkNBQ3lCOztJQUV6Qix5Q0FDcUY7O0lBRXJGLDBDQUNzRjs7SUFFdEYsMENBQzREOzs7OztJQXhHaEQsNENBQTBDOzs7OztJQUMxQyw2Q0FBNEM7Ozs7O0lBQzVDLG9DQUEwQzs7Ozs7SUFDMUMsc0NBQThCOzs7OztJQUM5QixvQ0FBMkI7Ozs7O0lBQzNCLDBDQUEyRSIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB7XHJcbiAgRGlyZWN0aXZlLFxyXG4gIENvbXBvbmVudFJlZixcclxuICBWaWV3Q29udGFpbmVyUmVmLFxyXG4gIENvbXBvbmVudEZhY3RvcnlSZXNvbHZlcixcclxuICBJbnB1dCxcclxuICBPbkNoYW5nZXMsXHJcbiAgU2ltcGxlQ2hhbmdlLFxyXG4gIE91dHB1dCxcclxuICBPbkRlc3Ryb3ksXHJcbiAgRXZlbnRFbWl0dGVyLCBPbkluaXQsIFJlbmRlcmVyMiwgQ2hhbmdlRGV0ZWN0b3JSZWYsIEluamVjdCwgRWxlbWVudFJlZlxyXG59IGZyb20gJ0Bhbmd1bGFyL2NvcmUnO1xyXG5pbXBvcnQge1BsYWNlbWVudCwgUGxhY2VtZW50cywgUG9wcGVyQ29udGVudE9wdGlvbnMsIFRyaWdnZXIsIFRyaWdnZXJzfSBmcm9tICcuL3BvcHBlci1tb2RlbCc7XHJcbmltcG9ydCB7UG9wcGVyQ29udGVudH0gZnJvbSAnLi9wb3BwZXItY29udGVudCc7XHJcblxyXG5ARGlyZWN0aXZlKHtcclxuICBzZWxlY3RvcjogJ1twb3BwZXJdJyxcclxuICBleHBvcnRBczogJ3BvcHBlcidcclxufSlcclxuZXhwb3J0IGNsYXNzIFBvcHBlckNvbnRyb2xsZXIgaW1wbGVtZW50cyBPbkluaXQsIE9uRGVzdHJveSwgT25DaGFuZ2VzIHtcclxuICBwcml2YXRlIHBvcHBlckNvbnRlbnRDbGFzcyA9IFBvcHBlckNvbnRlbnQ7XHJcbiAgcHJpdmF0ZSBwb3BwZXJDb250ZW50UmVmOiBDb21wb25lbnRSZWY8UG9wcGVyQ29udGVudD47XHJcbiAgcHJpdmF0ZSBzaG93bjogYm9vbGVhbiA9IGZhbHNlO1xyXG4gIHByaXZhdGUgc2NoZWR1bGVkU2hvd1RpbWVvdXQ6IGFueTtcclxuICBwcml2YXRlIHNjaGVkdWxlZEhpZGVUaW1lb3V0OiBhbnk7XHJcbiAgcHJpdmF0ZSBzdWJzY3JpcHRpb25zOiBhbnlbXSA9IFtdO1xyXG4gIHByaXZhdGUgZXZlbnRMaXN0ZW5lcnM6IGFueVtdID0gW107XHJcbiAgcHJpdmF0ZSBnbG9iYWxFdmVudExpc3RlbmVyczogYW55W10gPSBbXTtcclxuICBwcml2YXRlIHBvcHBlckNvbnRlbnQ6IFBvcHBlckNvbnRlbnQ7XHJcblxyXG4gIGNvbnN0cnVjdG9yKHByaXZhdGUgdmlld0NvbnRhaW5lclJlZjogVmlld0NvbnRhaW5lclJlZixcclxuICAgICAgICAgICAgICBwcml2YXRlIGNoYW5nZURldGVjdG9yUmVmOiBDaGFuZ2VEZXRlY3RvclJlZixcclxuICAgICAgICAgICAgICBwcml2YXRlIHJlc29sdmVyOiBDb21wb25lbnRGYWN0b3J5UmVzb2x2ZXIsXHJcbiAgICAgICAgICAgICAgcHJpdmF0ZSBlbGVtZW50UmVmOiBFbGVtZW50UmVmLFxyXG4gICAgICAgICAgICAgIHByaXZhdGUgcmVuZGVyZXI6IFJlbmRlcmVyMixcclxuICAgICAgICAgICAgICBASW5qZWN0KCdwb3BwZXJEZWZhdWx0cycpIHByaXZhdGUgcG9wcGVyRGVmYXVsdHM6IFBvcHBlckNvbnRlbnRPcHRpb25zID0ge30pIHtcclxuICAgIFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMgPSB7Li4uUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucywgLi4udGhpcy5wb3BwZXJEZWZhdWx0c307XHJcbiAgfVxyXG5cclxuICBwdWJsaWMgc3RhdGljIGJhc2VPcHRpb25zOiBQb3BwZXJDb250ZW50T3B0aW9ucyA9IDxQb3BwZXJDb250ZW50T3B0aW9ucz57XHJcbiAgICBzaG93RGVsYXk6IDAsXHJcbiAgICBwbGFjZW1lbnQ6IFBsYWNlbWVudHMuQXV0byxcclxuICAgIGhpZGVPbkNsaWNrT3V0c2lkZTogdHJ1ZSxcclxuICAgIGhpZGVPbk1vdXNlTGVhdmU6IGZhbHNlLFxyXG4gICAgaGlkZU9uU2Nyb2xsOiBmYWxzZSxcclxuICAgIHNob3dUcmlnZ2VyOiBUcmlnZ2Vycy5IT1ZFUixcclxuICAgIGFwcGVuZFRvOiB1bmRlZmluZWQsXHJcbiAgICBhcmlhUm9sZTogJ3BvcHBlcicsXHJcbiAgICBhcmlhRGVzY3JpYmU6ICcnLFxyXG4gICAgc3R5bGVzOiB7fVxyXG4gIH07XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyJylcclxuICBjb250ZW50OiBzdHJpbmcgfCBQb3BwZXJDb250ZW50O1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckRpc2FibGVkJylcclxuICBkaXNhYmxlZDogYm9vbGVhbjtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJQbGFjZW1lbnQnKVxyXG4gIHBsYWNlbWVudDogUGxhY2VtZW50O1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclRyaWdnZXInKVxyXG4gIHNob3dUcmlnZ2VyOiBUcmlnZ2VyIHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclRhcmdldCcpXHJcbiAgdGFyZ2V0RWxlbWVudDogSFRNTEVsZW1lbnQ7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyRGVsYXknKVxyXG4gIHNob3dEZWxheTogbnVtYmVyIHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclRpbWVvdXQnKVxyXG4gIGhpZGVUaW1lb3V0OiBudW1iZXIgPSAwO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclRpbWVvdXRBZnRlclNob3cnKVxyXG4gIHRpbWVvdXRBZnRlclNob3c6IG51bWJlciA9IDA7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyQm91bmRhcmllcycpXHJcbiAgYm91bmRhcmllc0VsZW1lbnQ6IHN0cmluZztcclxuXHJcbiAgQElucHV0KCdwb3BwZXJTaG93T25TdGFydCcpXHJcbiAgc2hvd09uU3RhcnQ6IGJvb2xlYW47XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyQ2xvc2VPbkNsaWNrT3V0c2lkZScpXHJcbiAgY2xvc2VPbkNsaWNrT3V0c2lkZTogYm9vbGVhbjtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJIaWRlT25DbGlja091dHNpZGUnKVxyXG4gIGhpZGVPbkNsaWNrT3V0c2lkZTogYm9vbGVhbiB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJIaWRlT25TY3JvbGwnKVxyXG4gIGhpZGVPblNjcm9sbDogYm9vbGVhbiB8IHVuZGVmaW5lZDtcclxuXHJcbiAgQElucHV0KCdwb3BwZXJIaWRlT25Nb3VzZUxlYXZlJylcclxuICBoaWRlT25Nb3VzZUxlYXZlOiBib29sZWFuIHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclBvc2l0aW9uRml4ZWQnKVxyXG4gIHBvc2l0aW9uRml4ZWQ6IGJvb2xlYW47XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyTW9kaWZpZXJzJylcclxuICBwb3BwZXJNb2RpZmllcnM6IHt9O1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckRpc2FibGVTdHlsZScpXHJcbiAgZGlzYWJsZVN0eWxlOiBib29sZWFuO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckRpc2FibGVBbmltYXRpb24nKVxyXG4gIGRpc2FibGVBbmltYXRpb246IGJvb2xlYW47XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyQXBwbHlDbGFzcycpXHJcbiAgYXBwbHlDbGFzczogc3RyaW5nO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckFwcGx5QXJyb3dDbGFzcycpXHJcbiAgYXBwbHlBcnJvd0NsYXNzOiBzdHJpbmc7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyQXJpYURlc2NyaWJlQnknKVxyXG4gIGFyaWFEZXNjcmliZTogc3RyaW5nIHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlckFyaWFSb2xlJylcclxuICBhcmlhUm9sZTogc3RyaW5nIHwgdW5kZWZpbmVkO1xyXG5cclxuICBASW5wdXQoJ3BvcHBlclN0eWxlcycpXHJcbiAgc3R5bGVzOiBPYmplY3QgfCB1bmRlZmluZWQ7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyQXBwZW5kVG8nKVxyXG4gIGFwcGVuZFRvOiBzdHJpbmc7XHJcblxyXG4gIEBJbnB1dCgncG9wcGVyUHJldmVudE92ZXJmbG93JylcclxuICBwcmV2ZW50T3ZlcmZsb3c6IGJvb2xlYW47XHJcblxyXG4gIEBPdXRwdXQoKVxyXG4gIHBvcHBlck9uU2hvd246IEV2ZW50RW1pdHRlcjxQb3BwZXJDb250cm9sbGVyPiA9IG5ldyBFdmVudEVtaXR0ZXI8UG9wcGVyQ29udHJvbGxlcj4oKTtcclxuXHJcbiAgQE91dHB1dCgpXHJcbiAgcG9wcGVyT25IaWRkZW46IEV2ZW50RW1pdHRlcjxQb3BwZXJDb250cm9sbGVyPiA9IG5ldyBFdmVudEVtaXR0ZXI8UG9wcGVyQ29udHJvbGxlcj4oKTtcclxuXHJcbiAgQE91dHB1dCgpXHJcbiAgcG9wcGVyT25VcGRhdGU6IEV2ZW50RW1pdHRlcjxhbnk+ID0gbmV3IEV2ZW50RW1pdHRlcjxhbnk+KCk7XHJcblxyXG4gIGhpZGVPbkNsaWNrT3V0c2lkZUhhbmRsZXIoJGV2ZW50OiBNb3VzZUV2ZW50KTogdm9pZCB7XHJcbiAgICBpZiAodGhpcy5kaXNhYmxlZCB8fCAhdGhpcy5oaWRlT25DbGlja091dHNpZGUgfHwgJGV2ZW50LnNyY0VsZW1lbnQgJiZcclxuICAgICAgJGV2ZW50LnNyY0VsZW1lbnQgPT09IHRoaXMucG9wcGVyQ29udGVudC5lbGVtUmVmLm5hdGl2ZUVsZW1lbnQgfHxcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50LmVsZW1SZWYubmF0aXZlRWxlbWVudC5jb250YWlucygkZXZlbnQuc3JjRWxlbWVudCkpIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG4gICAgdGhpcy5zY2hlZHVsZWRIaWRlKCRldmVudCwgdGhpcy5oaWRlVGltZW91dCk7XHJcbiAgfVxyXG5cclxuICBoaWRlT25TY3JvbGxIYW5kbGVyKCRldmVudDogTW91c2VFdmVudCk6IHZvaWQge1xyXG4gICAgaWYgKHRoaXMuZGlzYWJsZWQgfHwgIXRoaXMuaGlkZU9uU2Nyb2xsKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHRoaXMuc2NoZWR1bGVkSGlkZSgkZXZlbnQsIHRoaXMuaGlkZVRpbWVvdXQpO1xyXG4gIH1cclxuXHJcbiAgYXBwbHlUcmlnZ2VyTGlzdGVuZXJzKCkge1xyXG4gICAgc3dpdGNoICh0aGlzLnNob3dUcmlnZ2VyKSB7XHJcbiAgICAgIGNhc2UgVHJpZ2dlcnMuQ0xJQ0s6XHJcbiAgICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAnY2xpY2snLCB0aGlzLnRvZ2dsZS5iaW5kKHRoaXMpKSk7XHJcbiAgICAgICAgYnJlYWs7XHJcbiAgICAgIGNhc2UgVHJpZ2dlcnMuTU9VU0VET1dOOlxyXG4gICAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ21vdXNlZG93bicsIHRoaXMudG9nZ2xlLmJpbmQodGhpcykpKTtcclxuICAgICAgICBicmVhaztcclxuICAgICAgY2FzZSBUcmlnZ2Vycy5IT1ZFUjpcclxuICAgICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICdtb3VzZWVudGVyJywgdGhpcy5zY2hlZHVsZWRTaG93LmJpbmQodGhpcywgdGhpcy5zaG93RGVsYXkpKSk7XHJcbiAgICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAndG91Y2hlbmQnLCB0aGlzLnNjaGVkdWxlZEhpZGUuYmluZCh0aGlzLCBudWxsLCB0aGlzLmhpZGVUaW1lb3V0KSkpO1xyXG4gICAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ3RvdWNoY2FuY2VsJywgdGhpcy5zY2hlZHVsZWRIaWRlLmJpbmQodGhpcywgbnVsbCwgdGhpcy5oaWRlVGltZW91dCkpKTtcclxuICAgICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICdtb3VzZWxlYXZlJywgdGhpcy5zY2hlZHVsZWRIaWRlLmJpbmQodGhpcywgbnVsbCwgdGhpcy5oaWRlVGltZW91dCkpKTtcclxuICAgICAgICBicmVhaztcclxuICAgIH1cclxuICAgIGlmICh0aGlzLnNob3dUcmlnZ2VyICE9PSBUcmlnZ2Vycy5IT1ZFUiAmJiB0aGlzLmhpZGVPbk1vdXNlTGVhdmUpIHtcclxuICAgICAgdGhpcy5ldmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKHRoaXMuZWxlbWVudFJlZi5uYXRpdmVFbGVtZW50LCAndG91Y2hlbmQnLCB0aGlzLnNjaGVkdWxlZEhpZGUuYmluZCh0aGlzLCBudWxsLCB0aGlzLmhpZGVUaW1lb3V0KSkpO1xyXG4gICAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4odGhpcy5lbGVtZW50UmVmLm5hdGl2ZUVsZW1lbnQsICd0b3VjaGNhbmNlbCcsIHRoaXMuc2NoZWR1bGVkSGlkZS5iaW5kKHRoaXMsIG51bGwsIHRoaXMuaGlkZVRpbWVvdXQpKSk7XHJcbiAgICAgIHRoaXMuZXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmVsZW1lbnRSZWYubmF0aXZlRWxlbWVudCwgJ21vdXNlbGVhdmUnLCB0aGlzLnNjaGVkdWxlZEhpZGUuYmluZCh0aGlzLCBudWxsLCB0aGlzLmhpZGVUaW1lb3V0KSkpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgc3RhdGljIGFzc2lnbkRlZmluZWQodGFyZ2V0OiBhbnksIC4uLnNvdXJjZXM6IGFueVtdKSB7XHJcbiAgICBmb3IgKGNvbnN0IHNvdXJjZSBvZiBzb3VyY2VzKSB7XHJcbiAgICAgIGZvciAoY29uc3Qga2V5IG9mIE9iamVjdC5rZXlzKHNvdXJjZSkpIHtcclxuICAgICAgICBjb25zdCB2YWwgPSBzb3VyY2Vba2V5XTtcclxuICAgICAgICBpZiAodmFsICE9PSB1bmRlZmluZWQpIHtcclxuICAgICAgICAgIHRhcmdldFtrZXldID0gdmFsO1xyXG4gICAgICAgIH1cclxuICAgICAgfVxyXG4gICAgfVxyXG4gICAgcmV0dXJuIHRhcmdldDtcclxuICB9XHJcblxyXG4gIG5nT25Jbml0KCkge1xyXG4gICAgLy9TdXBwb3J0IGxlZ2FjeSBwcm9wXHJcbiAgICB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZSA9IHR5cGVvZiB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZSA9PT0gJ3VuZGVmaW5lZCcgP1xyXG4gICAgICB0aGlzLmNsb3NlT25DbGlja091dHNpZGUgOiB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZTtcclxuXHJcbiAgICBpZiAodHlwZW9mIHRoaXMuY29udGVudCA9PT0gJ3N0cmluZycpIHtcclxuICAgICAgY29uc3QgdGV4dCA9IHRoaXMuY29udGVudDtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50ID0gdGhpcy5jb25zdHJ1Y3RDb250ZW50KCk7XHJcbiAgICAgIHRoaXMucG9wcGVyQ29udGVudC50ZXh0ID0gdGV4dDtcclxuICAgIH1cclxuICAgIGVsc2Uge1xyXG4gICAgICB0aGlzLnBvcHBlckNvbnRlbnQgPSB0aGlzLmNvbnRlbnQ7XHJcbiAgICB9XHJcbiAgICBjb25zdCBwb3BwZXJSZWYgPSB0aGlzLnBvcHBlckNvbnRlbnQ7XHJcbiAgICBwb3BwZXJSZWYucmVmZXJlbmNlT2JqZWN0ID0gdGhpcy5nZXRSZWZFbGVtZW50KCk7XHJcbiAgICB0aGlzLnNldENvbnRlbnRQcm9wZXJ0aWVzKHBvcHBlclJlZik7XHJcbiAgICB0aGlzLnNldERlZmF1bHRzKCk7XHJcbiAgICB0aGlzLmFwcGx5VHJpZ2dlckxpc3RlbmVycygpO1xyXG4gICAgaWYgKHRoaXMuc2hvd09uU3RhcnQpIHtcclxuICAgICAgdGhpcy5zY2hlZHVsZWRTaG93KCk7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICBuZ09uQ2hhbmdlcyhjaGFuZ2VzOiB7IFtwcm9wZXJ0eU5hbWU6IHN0cmluZ106IFNpbXBsZUNoYW5nZSB9KSB7XHJcbiAgICBpZiAoY2hhbmdlc1sncG9wcGVyRGlzYWJsZWQnXSAmJiBjaGFuZ2VzWydwb3BwZXJEaXNhYmxlZCddLmN1cnJlbnRWYWx1ZSkge1xyXG4gICAgICB0aGlzLmhpZGUoKTtcclxuICAgIH1cclxuICAgIGlmIChjaGFuZ2VzWydjb250ZW50J11cclxuICAgICAgJiYgIWNoYW5nZXNbJ2NvbnRlbnQnXS5maXJzdENoYW5nZVxyXG4gICAgICAmJiB0eXBlb2YgY2hhbmdlc1snY29udGVudCddLmN1cnJlbnRWYWx1ZSA9PT0gJ3N0cmluZycpIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50LnRleHQgPSBjaGFuZ2VzWydjb250ZW50J10uY3VycmVudFZhbHVlO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChjaGFuZ2VzWydhcHBseUNsYXNzJ11cclxuICAgICAgJiYgIWNoYW5nZXNbJ2FwcGx5Q2xhc3MnXS5maXJzdENoYW5nZVxyXG4gICAgICAmJiB0eXBlb2YgY2hhbmdlc1snYXBwbHlDbGFzcyddLmN1cnJlbnRWYWx1ZSA9PT0gJ3N0cmluZycpIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50LnBvcHBlck9wdGlvbnMuYXBwbHlDbGFzcyA9IGNoYW5nZXNbJ2FwcGx5Q2xhc3MnXS5jdXJyZW50VmFsdWU7XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKGNoYW5nZXNbJ2FwcGx5QXJyb3dDbGFzcyddXHJcbiAgICAgICYmICFjaGFuZ2VzWydhcHBseUFycm93Q2xhc3MnXS5maXJzdENoYW5nZVxyXG4gICAgICAmJiB0eXBlb2YgY2hhbmdlc1snYXBwbHlBcnJvd0NsYXNzJ10uY3VycmVudFZhbHVlID09PSAnc3RyaW5nJykge1xyXG4gICAgICB0aGlzLnBvcHBlckNvbnRlbnQucG9wcGVyT3B0aW9ucy5hcHBseUFycm93Q2xhc3MgPSBjaGFuZ2VzWydhcHBseUFycm93Q2xhc3MnXS5jdXJyZW50VmFsdWU7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICBuZ09uRGVzdHJveSgpIHtcclxuICAgIHRoaXMuc3Vic2NyaXB0aW9ucy5mb3JFYWNoKHN1YiA9PiBzdWIudW5zdWJzY3JpYmUgJiYgc3ViLnVuc3Vic2NyaWJlKCkpO1xyXG4gICAgdGhpcy5zdWJzY3JpcHRpb25zLmxlbmd0aCA9IDA7XHJcbiAgICB0aGlzLmNsZWFyRXZlbnRMaXN0ZW5lcnMoKTtcclxuICAgIHRoaXMuY2xlYXJHbG9iYWxFdmVudExpc3RlbmVycygpO1xyXG4gICAgY2xlYXJUaW1lb3V0KHRoaXMuc2NoZWR1bGVkU2hvd1RpbWVvdXQpO1xyXG4gICAgY2xlYXJUaW1lb3V0KHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQpO1xyXG4gICAgdGhpcy5wb3BwZXJDb250ZW50ICYmIHRoaXMucG9wcGVyQ29udGVudC5jbGVhbigpO1xyXG4gIH1cclxuXHJcbiAgdG9nZ2xlKCkge1xyXG4gICAgaWYgKHRoaXMuZGlzYWJsZWQpIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG4gICAgdGhpcy5zaG93biA/IHRoaXMuc2NoZWR1bGVkSGlkZShudWxsLCB0aGlzLmhpZGVUaW1lb3V0KSA6IHRoaXMuc2NoZWR1bGVkU2hvdygpO1xyXG4gIH1cclxuXHJcbiAgc2hvdygpIHtcclxuICAgIGlmICh0aGlzLnNob3duKSB7XHJcbiAgICAgIHRoaXMub3ZlcnJpZGVIaWRlVGltZW91dCgpO1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcblxyXG4gICAgdGhpcy5zaG93biA9IHRydWU7XHJcbiAgICBjb25zdCBwb3BwZXJSZWYgPSB0aGlzLnBvcHBlckNvbnRlbnQ7XHJcbiAgICBjb25zdCBlbGVtZW50ID0gdGhpcy5nZXRSZWZFbGVtZW50KCk7XHJcbiAgICBpZiAocG9wcGVyUmVmLnJlZmVyZW5jZU9iamVjdCAhPT0gZWxlbWVudCkge1xyXG4gICAgICBwb3BwZXJSZWYucmVmZXJlbmNlT2JqZWN0ID0gZWxlbWVudDtcclxuICAgIH1cclxuICAgIHRoaXMuc2V0Q29udGVudFByb3BlcnRpZXMocG9wcGVyUmVmKTtcclxuICAgIHBvcHBlclJlZi5zaG93KCk7XHJcbiAgICB0aGlzLnBvcHBlck9uU2hvd24uZW1pdCh0aGlzKTtcclxuICAgIGlmICh0aGlzLnRpbWVvdXRBZnRlclNob3cgPiAwKSB7XHJcbiAgICAgIHRoaXMuc2NoZWR1bGVkSGlkZShudWxsLCB0aGlzLnRpbWVvdXRBZnRlclNob3cpO1xyXG4gICAgfVxyXG4gICAgdGhpcy5nbG9iYWxFdmVudExpc3RlbmVycy5wdXNoKHRoaXMucmVuZGVyZXIubGlzdGVuKCdkb2N1bWVudCcsICd0b3VjaGVuZCcsIHRoaXMuaGlkZU9uQ2xpY2tPdXRzaWRlSGFuZGxlci5iaW5kKHRoaXMpKSk7XHJcbiAgICB0aGlzLmdsb2JhbEV2ZW50TGlzdGVuZXJzLnB1c2godGhpcy5yZW5kZXJlci5saXN0ZW4oJ2RvY3VtZW50JywgJ2NsaWNrJywgdGhpcy5oaWRlT25DbGlja091dHNpZGVIYW5kbGVyLmJpbmQodGhpcykpKTtcclxuICAgIHRoaXMuZ2xvYmFsRXZlbnRMaXN0ZW5lcnMucHVzaCh0aGlzLnJlbmRlcmVyLmxpc3Rlbih0aGlzLmdldFNjcm9sbFBhcmVudCh0aGlzLmdldFJlZkVsZW1lbnQoKSksICdzY3JvbGwnLCB0aGlzLmhpZGVPblNjcm9sbEhhbmRsZXIuYmluZCh0aGlzKSkpO1xyXG4gIH1cclxuXHJcbiAgaGlkZSgpIHtcclxuICAgIGlmICh0aGlzLmRpc2FibGVkKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIGlmICghdGhpcy5zaG93bikge1xyXG4gICAgICB0aGlzLm92ZXJyaWRlU2hvd1RpbWVvdXQoKTtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMuc2hvd24gPSBmYWxzZTtcclxuICAgIGlmICh0aGlzLnBvcHBlckNvbnRlbnRSZWYpIHtcclxuICAgICAgdGhpcy5wb3BwZXJDb250ZW50UmVmLmluc3RhbmNlLmhpZGUoKTtcclxuICAgIH1cclxuICAgIGVsc2Uge1xyXG4gICAgICB0aGlzLnBvcHBlckNvbnRlbnQuaGlkZSgpO1xyXG4gICAgfVxyXG4gICAgdGhpcy5wb3BwZXJPbkhpZGRlbi5lbWl0KHRoaXMpO1xyXG4gICAgdGhpcy5jbGVhckdsb2JhbEV2ZW50TGlzdGVuZXJzKCk7XHJcbiAgfVxyXG5cclxuICBzY2hlZHVsZWRTaG93KGRlbGF5OiBudW1iZXIgfCB1bmRlZmluZWQgPSB0aGlzLnNob3dEZWxheSkge1xyXG4gICAgaWYgKHRoaXMuZGlzYWJsZWQpIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG4gICAgdGhpcy5vdmVycmlkZUhpZGVUaW1lb3V0KCk7XHJcbiAgICB0aGlzLnNjaGVkdWxlZFNob3dUaW1lb3V0ID0gc2V0VGltZW91dCgoKSA9PiB7XHJcbiAgICAgIHRoaXMuc2hvdygpO1xyXG4gICAgICB0aGlzLmFwcGx5Q2hhbmdlcygpO1xyXG4gICAgfSwgZGVsYXkpXHJcbiAgfVxyXG5cclxuICBzY2hlZHVsZWRIaWRlKCRldmVudDogYW55ID0gbnVsbCwgZGVsYXk6IG51bWJlciA9IHRoaXMuaGlkZVRpbWVvdXQpIHtcclxuICAgIGlmICh0aGlzLmRpc2FibGVkKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHRoaXMub3ZlcnJpZGVTaG93VGltZW91dCgpO1xyXG4gICAgdGhpcy5zY2hlZHVsZWRIaWRlVGltZW91dCA9IHNldFRpbWVvdXQoKCkgPT4ge1xyXG4gICAgICBjb25zdCB0b0VsZW1lbnQgPSAkZXZlbnQgPyAkZXZlbnQudG9FbGVtZW50IDogbnVsbDtcclxuICAgICAgY29uc3QgcG9wcGVyQ29udGVudFZpZXcgPSB0aGlzLnBvcHBlckNvbnRlbnQucG9wcGVyVmlld1JlZiA/IHRoaXMucG9wcGVyQ29udGVudC5wb3BwZXJWaWV3UmVmLm5hdGl2ZUVsZW1lbnQgOiBmYWxzZTtcclxuICAgICAgaWYgKCFwb3BwZXJDb250ZW50VmlldyB8fCBwb3BwZXJDb250ZW50VmlldyA9PT0gdG9FbGVtZW50IHx8IHBvcHBlckNvbnRlbnRWaWV3LmNvbnRhaW5zKHRvRWxlbWVudCkgfHwgKHRoaXMuY29udGVudCBhcyBQb3BwZXJDb250ZW50KS5pc01vdXNlT3Zlcikge1xyXG4gICAgICAgIHJldHVybjtcclxuICAgICAgfVxyXG4gICAgICB0aGlzLmhpZGUoKTtcclxuICAgICAgdGhpcy5hcHBseUNoYW5nZXMoKTtcclxuICAgIH0sIGRlbGF5KTtcclxuICB9XHJcblxyXG4gIGdldFJlZkVsZW1lbnQoKSB7XHJcbiAgICByZXR1cm4gdGhpcy50YXJnZXRFbGVtZW50IHx8IHRoaXMudmlld0NvbnRhaW5lclJlZi5lbGVtZW50Lm5hdGl2ZUVsZW1lbnQ7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIGFwcGx5Q2hhbmdlcygpIHtcclxuICAgIHRoaXMuY2hhbmdlRGV0ZWN0b3JSZWYubWFya0ZvckNoZWNrKCk7XHJcbiAgICB0aGlzLmNoYW5nZURldGVjdG9yUmVmLmRldGVjdENoYW5nZXMoKTtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgc2V0RGVmYXVsdHMoKSB7XHJcbiAgICB0aGlzLnNob3dEZWxheSA9IHR5cGVvZiB0aGlzLnNob3dEZWxheSA9PT0gJ3VuZGVmaW5lZCcgPyBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zLnNob3dEZWxheSA6IHRoaXMuc2hvd0RlbGF5O1xyXG4gICAgdGhpcy5zaG93VHJpZ2dlciA9IHR5cGVvZiB0aGlzLnNob3dUcmlnZ2VyID09PSAndW5kZWZpbmVkJyA/IFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMudHJpZ2dlciA6IHRoaXMuc2hvd1RyaWdnZXI7XHJcbiAgICB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZSA9IHR5cGVvZiB0aGlzLmhpZGVPbkNsaWNrT3V0c2lkZSA9PT0gJ3VuZGVmaW5lZCcgPyBQb3BwZXJDb250cm9sbGVyLmJhc2VPcHRpb25zLmhpZGVPbkNsaWNrT3V0c2lkZSA6IHRoaXMuaGlkZU9uQ2xpY2tPdXRzaWRlO1xyXG4gICAgdGhpcy5oaWRlT25TY3JvbGwgPSB0eXBlb2YgdGhpcy5oaWRlT25TY3JvbGwgPT09ICd1bmRlZmluZWQnID8gUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucy5oaWRlT25TY3JvbGwgOiB0aGlzLmhpZGVPblNjcm9sbDtcclxuICAgIHRoaXMuaGlkZU9uTW91c2VMZWF2ZSA9IHR5cGVvZiB0aGlzLmhpZGVPbk1vdXNlTGVhdmUgPT09ICd1bmRlZmluZWQnID8gUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucy5oaWRlT25Nb3VzZUxlYXZlIDogdGhpcy5oaWRlT25Nb3VzZUxlYXZlO1xyXG4gICAgdGhpcy5hcmlhUm9sZSA9IHR5cGVvZiB0aGlzLmFyaWFSb2xlID09PSAndW5kZWZpbmVkJyA/IFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMuYXJpYVJvbGUgOiB0aGlzLmFyaWFSb2xlO1xyXG4gICAgdGhpcy5hcmlhRGVzY3JpYmUgPSB0eXBlb2YgdGhpcy5hcmlhRGVzY3JpYmUgPT09ICd1bmRlZmluZWQnID8gUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucy5hcmlhRGVzY3JpYmUgOiB0aGlzLmFyaWFEZXNjcmliZTtcclxuICAgIHRoaXMuc3R5bGVzID0gdHlwZW9mIHRoaXMuc3R5bGVzID09PSAndW5kZWZpbmVkJyA/IE9iamVjdC5hc3NpZ24oe30sIFBvcHBlckNvbnRyb2xsZXIuYmFzZU9wdGlvbnMuc3R5bGVzKSA6IHRoaXMuc3R5bGVzO1xyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBjbGVhckV2ZW50TGlzdGVuZXJzKCkge1xyXG4gICAgdGhpcy5ldmVudExpc3RlbmVycy5mb3JFYWNoKGV2dCA9PiB7XHJcbiAgICAgIGV2dCAmJiB0eXBlb2YgZXZ0ID09PSAnZnVuY3Rpb24nICYmIGV2dCgpO1xyXG4gICAgfSk7XHJcbiAgICB0aGlzLmV2ZW50TGlzdGVuZXJzLmxlbmd0aCA9IDA7XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIGNsZWFyR2xvYmFsRXZlbnRMaXN0ZW5lcnMoKSB7XHJcbiAgICB0aGlzLmdsb2JhbEV2ZW50TGlzdGVuZXJzLmZvckVhY2goZXZ0ID0+IHtcclxuICAgICAgZXZ0ICYmIHR5cGVvZiBldnQgPT09ICdmdW5jdGlvbicgJiYgZXZ0KCk7XHJcbiAgICB9KTtcclxuICAgIHRoaXMuZ2xvYmFsRXZlbnRMaXN0ZW5lcnMubGVuZ3RoID0gMDtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgb3ZlcnJpZGVTaG93VGltZW91dCgpIHtcclxuICAgIGlmICh0aGlzLnNjaGVkdWxlZFNob3dUaW1lb3V0KSB7XHJcbiAgICAgIGNsZWFyVGltZW91dCh0aGlzLnNjaGVkdWxlZFNob3dUaW1lb3V0KTtcclxuICAgICAgdGhpcy5zY2hlZHVsZWRIaWRlVGltZW91dCA9IDA7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICBwcml2YXRlIG92ZXJyaWRlSGlkZVRpbWVvdXQoKSB7XHJcbiAgICBpZiAodGhpcy5zY2hlZHVsZWRIaWRlVGltZW91dCkge1xyXG4gICAgICBjbGVhclRpbWVvdXQodGhpcy5zY2hlZHVsZWRIaWRlVGltZW91dCk7XHJcbiAgICAgIHRoaXMuc2NoZWR1bGVkSGlkZVRpbWVvdXQgPSAwO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBjb25zdHJ1Y3RDb250ZW50KCk6IFBvcHBlckNvbnRlbnQge1xyXG4gICAgY29uc3QgZmFjdG9yeSA9IHRoaXMucmVzb2x2ZXIucmVzb2x2ZUNvbXBvbmVudEZhY3RvcnkodGhpcy5wb3BwZXJDb250ZW50Q2xhc3MpO1xyXG4gICAgdGhpcy5wb3BwZXJDb250ZW50UmVmID0gdGhpcy52aWV3Q29udGFpbmVyUmVmLmNyZWF0ZUNvbXBvbmVudChmYWN0b3J5KTtcclxuICAgIHJldHVybiB0aGlzLnBvcHBlckNvbnRlbnRSZWYuaW5zdGFuY2UgYXMgUG9wcGVyQ29udGVudDtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgc2V0Q29udGVudFByb3BlcnRpZXMocG9wcGVyUmVmOiBQb3BwZXJDb250ZW50KSB7XHJcbiAgICBwb3BwZXJSZWYucG9wcGVyT3B0aW9ucyA9IFBvcHBlckNvbnRyb2xsZXIuYXNzaWduRGVmaW5lZChwb3BwZXJSZWYucG9wcGVyT3B0aW9ucywgUG9wcGVyQ29udHJvbGxlci5iYXNlT3B0aW9ucywge1xyXG4gICAgICBzaG93RGVsYXk6IHRoaXMuc2hvd0RlbGF5LFxyXG4gICAgICBkaXNhYmxlQW5pbWF0aW9uOiB0aGlzLmRpc2FibGVBbmltYXRpb24sXHJcbiAgICAgIGRpc2FibGVEZWZhdWx0U3R5bGluZzogdGhpcy5kaXNhYmxlU3R5bGUsXHJcbiAgICAgIHBsYWNlbWVudDogdGhpcy5wbGFjZW1lbnQsXHJcbiAgICAgIGJvdW5kYXJpZXNFbGVtZW50OiB0aGlzLmJvdW5kYXJpZXNFbGVtZW50LFxyXG4gICAgICB0cmlnZ2VyOiB0aGlzLnNob3dUcmlnZ2VyLFxyXG4gICAgICBwb3NpdGlvbkZpeGVkOiB0aGlzLnBvc2l0aW9uRml4ZWQsXHJcbiAgICAgIHBvcHBlck1vZGlmaWVyczogdGhpcy5wb3BwZXJNb2RpZmllcnMsXHJcbiAgICAgIGFyaWFEZXNjcmliZTogdGhpcy5hcmlhRGVzY3JpYmUsXHJcbiAgICAgIGFyaWFSb2xlOiB0aGlzLmFyaWFSb2xlLFxyXG4gICAgICBhcHBseUNsYXNzOiB0aGlzLmFwcGx5Q2xhc3MsXHJcbiAgICAgIGFwcGx5QXJyb3dDbGFzczogdGhpcy5hcHBseUFycm93Q2xhc3MsXHJcbiAgICAgIGhpZGVPbk1vdXNlTGVhdmU6IHRoaXMuaGlkZU9uTW91c2VMZWF2ZSxcclxuICAgICAgc3R5bGVzOiB0aGlzLnN0eWxlcyxcclxuICAgICAgYXBwZW5kVG86IHRoaXMuYXBwZW5kVG8sXHJcbiAgICAgIHByZXZlbnRPdmVyZmxvdzogdGhpcy5wcmV2ZW50T3ZlcmZsb3csXHJcbiAgICB9KTtcclxuICAgIHBvcHBlclJlZi5vblVwZGF0ZSA9IHRoaXMub25Qb3BwZXJVcGRhdGUuYmluZCh0aGlzKTtcclxuICAgIHRoaXMuc3Vic2NyaXB0aW9ucy5wdXNoKHBvcHBlclJlZi5vbkhpZGRlbi5zdWJzY3JpYmUodGhpcy5oaWRlLmJpbmQodGhpcykpKTtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgZ2V0U2Nyb2xsUGFyZW50KG5vZGUpIHtcclxuICAgIGNvbnN0IGlzRWxlbWVudCA9IG5vZGUgaW5zdGFuY2VvZiBIVE1MRWxlbWVudDtcclxuICAgIGNvbnN0IG92ZXJmbG93WSA9IGlzRWxlbWVudCAmJiB3aW5kb3cuZ2V0Q29tcHV0ZWRTdHlsZShub2RlKS5vdmVyZmxvd1k7XHJcbiAgICBjb25zdCBpc1Njcm9sbGFibGUgPSBvdmVyZmxvd1kgIT09ICd2aXNpYmxlJyAmJiBvdmVyZmxvd1kgIT09ICdoaWRkZW4nO1xyXG5cclxuICAgIGlmICghbm9kZSkge1xyXG4gICAgICByZXR1cm4gbnVsbDtcclxuICAgIH0gZWxzZSBpZiAoaXNTY3JvbGxhYmxlICYmIG5vZGUuc2Nyb2xsSGVpZ2h0ID49IG5vZGUuY2xpZW50SGVpZ2h0KSB7XHJcbiAgICAgIHJldHVybiBub2RlO1xyXG4gICAgfVxyXG5cclxuICAgIHJldHVybiB0aGlzLmdldFNjcm9sbFBhcmVudChub2RlLnBhcmVudE5vZGUpIHx8IGRvY3VtZW50O1xyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBvblBvcHBlclVwZGF0ZShldmVudCkge1xyXG4gICAgdGhpcy5wb3BwZXJPblVwZGF0ZS5lbWl0KGV2ZW50KTtcclxuICB9XHJcblxyXG59XHJcbiJdfQ==