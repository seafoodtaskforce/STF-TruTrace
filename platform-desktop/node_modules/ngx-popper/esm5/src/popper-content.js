/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Renderer2, ViewChild, ViewContainerRef, ViewEncapsulation, } from "@angular/core";
import Popper from 'popper.js';
import { Placements, Triggers } from './popper-model';
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
export { PopperContent };
if (false) {
    /** @type {?} */
    PopperContent.prototype.popperOptions;
    /** @type {?} */
    PopperContent.prototype.referenceObject;
    /** @type {?} */
    PopperContent.prototype.isMouseOver;
    /** @type {?} */
    PopperContent.prototype.onHidden;
    /** @type {?} */
    PopperContent.prototype.text;
    /** @type {?} */
    PopperContent.prototype.popperInstance;
    /** @type {?} */
    PopperContent.prototype.displayType;
    /** @type {?} */
    PopperContent.prototype.opacity;
    /** @type {?} */
    PopperContent.prototype.ariaHidden;
    /** @type {?} */
    PopperContent.prototype.arrowColor;
    /** @type {?} */
    PopperContent.prototype.onUpdate;
    /** @type {?} */
    PopperContent.prototype.state;
    /**
     * @type {?}
     * @private
     */
    PopperContent.prototype.globalResize;
    /** @type {?} */
    PopperContent.prototype.popperViewRef;
    /** @type {?} */
    PopperContent.prototype.elemRef;
    /**
     * @type {?}
     * @private
     */
    PopperContent.prototype.renderer;
    /**
     * @type {?}
     * @private
     */
    PopperContent.prototype.viewRef;
    /**
     * @type {?}
     * @private
     */
    PopperContent.prototype.CDR;
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicG9wcGVyLWNvbnRlbnQuanMiLCJzb3VyY2VSb290Ijoibmc6Ly9uZ3gtcG9wcGVyLyIsInNvdXJjZXMiOlsic3JjL3BvcHBlci1jb250ZW50LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7Ozs7QUFBQSxPQUFPLEVBQ0wsdUJBQXVCLEVBQUUsaUJBQWlCLEVBQzFDLFNBQVMsRUFDVCxVQUFVLEVBQ1YsWUFBWSxFQUNaLFlBQVksRUFFWixTQUFTLEVBQ1QsU0FBUyxFQUNULGdCQUFnQixFQUNoQixpQkFBaUIsR0FDbEIsTUFBTSxlQUFlLENBQUE7QUFDdEIsT0FBTyxNQUFNLE1BQU0sV0FBVyxDQUFBO0FBQzlCLE9BQU8sRUFBQyxVQUFVLEVBQXdCLFFBQVEsRUFBQyxNQUFNLGdCQUFnQixDQUFBO0FBRXpFO0lBc0ZFLHVCQUNTLE9BQW1CLEVBQ2xCLFFBQW1CLEVBQ25CLE9BQXlCLEVBQ3pCLEdBQXNCO1FBSHZCLFlBQU8sR0FBUCxPQUFPLENBQVk7UUFDbEIsYUFBUSxHQUFSLFFBQVEsQ0FBVztRQUNuQixZQUFPLEdBQVAsT0FBTyxDQUFrQjtRQUN6QixRQUFHLEdBQUgsR0FBRyxDQUFtQjtRQTVEaEMsa0JBQWEsR0FBeUIsbUJBQXNCO1lBQzFELGdCQUFnQixFQUFFLEtBQUs7WUFDdkIscUJBQXFCLEVBQUUsS0FBSztZQUM1QixTQUFTLEVBQUUsVUFBVSxDQUFDLElBQUk7WUFDMUIsaUJBQWlCLEVBQUUsRUFBRTtZQUNyQixPQUFPLEVBQUUsUUFBUSxDQUFDLEtBQUs7WUFDdkIsYUFBYSxFQUFFLEtBQUs7WUFDcEIsWUFBWSxFQUFFLEtBQUs7WUFDbkIsZUFBZSxFQUFFLEVBQUU7U0FDcEIsRUFBQSxDQUFDO1FBSUYsZ0JBQVcsR0FBWSxLQUFLLENBQUM7UUFFN0IsYUFBUSxHQUFHLElBQUksWUFBWSxFQUFFLENBQUM7UUFNOUIsZ0JBQVcsR0FBVyxNQUFNLENBQUM7UUFFN0IsWUFBTyxHQUFXLENBQUMsQ0FBQztRQUVwQixlQUFVLEdBQVcsTUFBTSxD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QyxZQUFZLEVBQUUsQ0FBQztJQUN0RixDQUFDOztnQkFwT0YsU0FBUyxTQUFDO29CQUNULFFBQVEsRUFBRSxnQkFBZ0I7b0JBQzFCLGFBQWEsRUFBRSxpQkFBaUIsQ0FBQyxJQUFJO29CQUNyQyxlQUFlLEVBQUUsdUJBQXVCLENBQUMsTUFBTTtvQkFDL0MsUUFBUSxFQUFFLHU5QkFxQlQ7O2lCQUVGOzs7O2dCQXZDQyxVQUFVO2dCQUlWLFNBQVM7Z0JBRVQsZ0JBQWdCO2dCQVJTLGlCQUFpQjs7O2dDQStFekMsU0FBUyxTQUFDLGVBQWU7OEJBR3pCLFlBQVksU0FBQyxXQUFXOzhCQUt4QixZQUFZLFNBQUMsWUFBWTs7SUE2SjVCLG9CQUFDO0NBQUEsQUF0T0QsSUFzT0M7U0ExTVksYUFBYTs7O0lBRXhCLHNDQVNFOztJQUVGLHdDQUE2Qjs7SUFFN0Isb0NBQTZCOztJQUU3QixpQ0FBOEI7O0lBRTlCLDZCQUFhOztJQUViLHVDQUF1Qjs7SUFFdkIsb0NBQTZCOztJQUU3QixnQ0FBb0I7O0lBRXBCLG1DQUE0Qjs7SUFFNUIsbUNBQWlDOztJQUVqQyxpQ0FBbUI7O0lBRW5CLDhCQUFzQjs7Ozs7SUFFdEIscUNBQTBCOztJQUUxQixzQ0FDMEI7O0lBcUJ4QixnQ0FBMEI7Ozs7O0lBQzFCLGlDQUEyQjs7Ozs7SUFDM0IsZ0NBQWlDOzs7OztJQUNqQyw0QkFBOEIiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQge1xyXG4gIENoYW5nZURldGVjdGlvblN0cmF0ZWd5LCBDaGFuZ2VEZXRlY3RvclJlZixcclxuICBDb21wb25lbnQsXHJcbiAgRWxlbWVudFJlZixcclxuICBFdmVudEVtaXR0ZXIsXHJcbiAgSG9zdExpc3RlbmVyLFxyXG4gIE9uRGVzdHJveSxcclxuICBSZW5kZXJlcjIsXHJcbiAgVmlld0NoaWxkLFxyXG4gIFZpZXdDb250YWluZXJSZWYsXHJcbiAgVmlld0VuY2Fwc3VsYXRpb24sXHJcbn0gZnJvbSBcIkBhbmd1bGFyL2NvcmVcIlxyXG5pbXBvcnQgUG9wcGVyIGZyb20gJ3BvcHBlci5qcydcclxuaW1wb3J0IHtQbGFjZW1lbnRzLCBQb3BwZXJDb250ZW50T3B0aW9ucywgVHJpZ2dlcnN9IGZyb20gJy4vcG9wcGVyLW1vZGVsJ1xyXG5cclxuQENvbXBvbmVudCh7XHJcbiAgc2VsZWN0b3I6IFwicG9wcGVyLWNvbnRlbnRcIixcclxuICBlbmNhcHN1bGF0aW9uOiBWaWV3RW5jYXBzdWxhdGlvbi5Ob25lLFxyXG4gIGNoYW5nZURldGVjdGlvbjogQ2hhbmdlRGV0ZWN0aW9uU3RyYXRlZ3kuT25QdXNoLFxyXG4gIHRlbXBsYXRlOiBgXHJcbiAgICA8ZGl2ICNwb3BwZXJWaWV3UmVmXHJcbiAgICAgICAgIFtjbGFzcy5uZ3hwX19jb250YWluZXJdPVwiIXBvcHBlck9wdGlvbnMuZGlzYWJsZURlZmF1bHRTdHlsaW5nXCJcclxuICAgICAgICAgW2NsYXNzLm5neHBfX2FuaW1hdGlvbl09XCIhcG9wcGVyT3B0aW9ucy5kaXNhYmxlQW5pbWF0aW9uXCJcclxuICAgICAgICAgW3N0eWxlLmRpc3BsYXldPVwiZGlzcGxheVR5cGVcIlxyXG4gICAgICAgICBbc3R5bGUub3BhY2l0eV09XCJvcGFjaXR5XCJcclxuICAgICAgICAgW25nU3R5bGVdPVwicG9wcGVyT3B0aW9ucy5zdHlsZXNcIlxyXG4gICAgICAgICBbbmdDbGFzc109XCJleHRyYWN0QXBwbGllZENsYXNzTGlzdEV4cHIocG9wcGVyT3B0aW9ucy5hcHBseUNsYXNzKVwiXHJcbiAgICAgICAgIGF0dHIuYXJpYS1oaWRkZW49XCJ7e2FyaWFIaWRkZW59fVwiXHJcbiAgICAgICAgIFthdHRyLmFyaWEtZGVzY3JpYmVkYnldPVwicG9wcGVyT3B0aW9ucy5hcmlhRGVzY3JpYmUgfHwgbnVsbFwiXHJcbiAgICAgICAgIGF0dHIucm9sZT1cInt7cG9wcGVyT3B0aW9ucy5hcmlhUm9sZX19XCI+XHJcbiAgICAgIDxkaXYgY2xhc3M9XCJuZ3hwX19pbm5lclwiICpuZ0lmPVwidGV4dFwiIFtpbm5lckhUTUxdPVwidGV4dFwiPlxyXG4gICAgICAgIDxuZy1jb250ZW50PjwvbmctY29udGVudD5cclxuICAgICAgPC9kaXY+XHJcbiAgICAgIDxkaXYgY2xhc3M9XCJuZ3hwX19pbm5lclwiICpuZ0lmPVwiIXRleHRcIj5cclxuICAgICAgICA8bmctY29udGVudD48L25nLWNvbnRlbnQ+XHJcbiAgICAgIDwvZGl2PlxyXG4gICAgICA8ZGl2IGNsYXNzPVwibmd4cF9fYXJyb3dcIiBbc3R5bGUuYm9yZGVyLWNvbG9yXT1cImFycm93Q29sb3JcIiBbY2xhc3MuX19mb3JjZS1hcnJvd109XCJhcnJvd0NvbG9yXCJcclxuICAgICAgICAgICBbbmdDbGFzc109XCJleHRyYWN0QXBwbGllZENsYXNzTGlzdEV4cHIocG9wcGVyT3B0aW9ucy5hcHBseUFycm93Q2xhc3MpXCI+PC9kaXY+XHJcblxyXG4gICAgPC9kaXY+XHJcbiAgYCxcclxuICBzdHlsZVVybHM6IFsnLi9wb3BwZXItY29udGVudC5jc3MnXSxcclxufSlcclxuZXhwb3J0IGNsYXNzIFBvcHBlckNvbnRlbnQgaW1wbGVtZW50cyBPbkRlc3Ryb3kge1xyXG5cclxuICBwb3BwZXJPcHRpb25zOiBQb3BwZXJDb250ZW50T3B0aW9ucyA9IDxQb3BwZXJDb250ZW50T3B0aW9ucz57XHJcbiAgICBkaXNhYmxlQW5pbWF0aW9uOiBmYWxzZSxcclxuICAgIGRpc2FibGVEZWZhdWx0U3R5bGluZzogZmFsc2UsXHJcbiAgICBwbGFjZW1lbnQ6IFBsYWNlbWVudHMuQXV0byxcclxuICAgIGJvdW5kYXJpZXNFbGVtZW50OiAnJyxcclxuICAgIHRyaWdnZXI6IFRyaWdnZXJzLkhPVkVSLFxyXG4gICAgcG9zaXRpb25GaXhlZDogZmFsc2UsXHJcbiAgICBhcHBlbmRUb0JvZHk6IGZhbHNlLFxyXG4gICAgcG9wcGVyTW9kaWZpZXJzOiB7fVxyXG4gIH07XHJcblxyXG4gIHJlZmVyZW5jZU9iamVjdDogSFRNTEVsZW1lbnQ7XHJcblxyXG4gIGlzTW91c2VPdmVyOiBib29sZWFuID0gZmFsc2U7XHJcblxyXG4gIG9uSGlkZGVuID0gbmV3IEV2ZW50RW1pdHRlcigpO1xyXG5cclxuICB0ZXh0OiBzdHJpbmc7XHJcblxyXG4gIHBvcHBlckluc3RhbmNlOiBQb3BwZXI7XHJcblxyXG4gIGRpc3BsYXlUeXBlOiBzdHJpbmcgPSBcIm5vbmVcIjtcclxuXHJcbiAgb3BhY2l0eTogbnVtYmVyID0gMDtcclxuXHJcbiAgYXJpYUhpZGRlbjogc3RyaW5nID0gJ3RydWUnO1xyXG5cclxuICBhcnJvd0NvbG9yOiBzdHJpbmcgfCBudWxsID0gbnVsbDtcclxuXHJcbiAgb25VcGRhdGU6IEZ1bmN0aW9uO1xyXG5cclxuICBzdGF0ZTogYm9vbGVhbiA9IHRydWU7XHJcblxyXG4gIHByaXZhdGUgZ2xvYmFsUmVzaXplOiBhbnk7XHJcblxyXG4gIEBWaWV3Q2hpbGQoXCJwb3BwZXJWaWV3UmVmXCIpXHJcbiAgcG9wcGVyVmlld1JlZjogRWxlbWVudFJlZjtcclxuXHJcbiAgQEhvc3RMaXN0ZW5lcignbW91c2VvdmVyJylcclxuICBvbk1vdXNlT3ZlcigpIHtcclxuICAgIHRoaXMuaXNNb3VzZU92ZXIgPSB0cnVlO1xyXG4gIH1cclxuXHJcbiAgQEhvc3RMaXN0ZW5lcignbW91c2VsZWF2ZScpXHJcbiAgc2hvd09uTGVhdmUoKSB7XHJcbiAgICB0aGlzLmlzTW91c2VPdmVyID0gZmFsc2U7XHJcbiAgICBpZiAodGhpcy5wb3BwZXJPcHRpb25zLnRyaWdnZXIgIT09IFRyaWdnZXJzLkhPVkVSICYmICF0aGlzLnBvcHBlck9wdGlvbnMuaGlkZU9uTW91c2VMZWF2ZSkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcbiAgICB0aGlzLmhpZGUoKTtcclxuICB9XHJcblxyXG4gIG9uRG9jdW1lbnRSZXNpemUoKSB7XHJcbiAgICB0aGlzLnVwZGF0ZSgpO1xyXG4gIH1cclxuXHJcbiAgY29uc3RydWN0b3IoXHJcbiAgICBwdWJsaWMgZWxlbVJlZjogRWxlbWVudFJlZixcclxuICAgIHByaXZhdGUgcmVuZGVyZXI6IFJlbmRlcmVyMixcclxuICAgIHByaXZhdGUgdmlld1JlZjogVmlld0NvbnRhaW5lclJlZixcclxuICAgIHByaXZhdGUgQ0RSOiBDaGFuZ2VEZXRlY3RvclJlZikge1xyXG4gIH1cclxuXHJcbiAgbmdPbkRlc3Ryb3koKSB7XHJcbiAgICB0aGlzLmNsZWFuKCk7XHJcbiAgICBpZih0aGlzLnBvcHBlck9wdGlvbnMuYXBwZW5kVG8gJiYgdGhpcy5lbGVtUmVmICYmIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50ICYmIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50LnBhcmVudE5vZGUpe1xyXG4gICAgICB0aGlzLnZpZXdSZWYuZGV0YWNoKCk7XHJcbiAgICAgIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50LnBhcmVudE5vZGUucmVtb3ZlQ2hpbGQodGhpcy5lbGVtUmVmLm5hdGl2ZUVsZW1lbnQpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgY2xlYW4oKSB7XHJcbiAgICB0aGlzLnRvZ2dsZVZpc2liaWxpdHkoZmFsc2UpO1xyXG4gICAgaWYgKCF0aGlzLnBvcHBlckluc3RhbmNlKSB7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgICh0aGlzLnBvcHBlckluc3RhbmNlIGFzIGFueSkuZGlzYWJsZUV2ZW50TGlzdGVuZXJzKCk7XHJcbiAgICB0aGlzLnBvcHBlckluc3RhbmNlLmRlc3Ryb3koKTtcclxuXHJcbiAgfVxyXG5cclxuICBzaG93KCk6IHZvaWQge1xyXG4gICAgaWYgKCF0aGlzLnJlZmVyZW5jZU9iamVjdCkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcblxyXG4gICAgY29uc3QgYXBwZW5kVG9QYXJlbnQgPSB0aGlzLnBvcHBlck9wdGlvbnMuYXBwZW5kVG8gJiYgZG9jdW1lbnQucXVlcnlTZWxlY3Rvcih0aGlzLnBvcHBlck9wdGlvbnMuYXBwZW5kVG8pO1xyXG4gICAgaWYgKGFwcGVuZFRvUGFyZW50ICYmIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50LnBhcmVudE5vZGUgIT09IGFwcGVuZFRvUGFyZW50KSB7XHJcbiAgICAgIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50LnBhcmVudE5vZGUgJiYgdGhpcy5lbGVtUmVmLm5hdGl2ZUVsZW1lbnQucGFyZW50Tm9kZS5yZW1vdmVDaGlsZCh0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudCk7XHJcbiAgICAgIGFwcGVuZFRvUGFyZW50LmFwcGVuZENoaWxkKHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50KTtcclxuICAgIH1cclxuXHJcbiAgICBsZXQgcG9wcGVyT3B0aW9uczogUG9wcGVyLlBvcHBlck9wdGlvbnMgPSA8UG9wcGVyLlBvcHBlck9wdGlvbnM+e1xyXG4gICAgICBwbGFjZW1lbnQ6IHRoaXMucG9wcGVyT3B0aW9ucy5wbGFjZW1lbnQsXHJcbiAgICAgIHBvc2l0aW9uRml4ZWQ6IHRoaXMucG9wcGVyT3B0aW9ucy5wb3NpdGlvbkZpeGVkLFxyXG4gICAgICBtb2RpZmllcnM6IHtcclxuICAgICAgICBhcnJvdzoge1xyXG4gICAgICAgICAgZWxlbWVudDogdGhpcy5wb3BwZXJWaWV3UmVmLm5hdGl2ZUVsZW1lbnQucXVlcnlTZWxlY3RvcignLm5neHBfX2Fycm93JylcclxuICAgICAgICB9XHJcbiAgICAgIH1cclxuICAgIH07XHJcbiAgICBpZiAodGhpcy5vblVwZGF0ZSkge1xyXG4gICAgICBwb3BwZXJPcHRpb25zLm9uVXBkYXRlID0gdGhpcy5vblVwZGF0ZSBhcyBhbnk7XHJcbiAgICB9XHJcblxyXG4gICAgbGV0IGJvdW5kYXJpZXNFbGVtZW50ID0gdGhpcy5wb3BwZXJPcHRpb25zLmJvdW5kYXJpZXNFbGVtZW50ICYmIGRvY3VtZW50LnF1ZXJ5U2VsZWN0b3IodGhpcy5wb3BwZXJPcHRpb25zLmJvdW5kYXJpZXNFbGVtZW50KTtcclxuXHJcbiAgICBpZiAocG9wcGVyT3B0aW9ucy5tb2RpZmllcnMgJiYgYm91bmRhcmllc0VsZW1lbnQpIHtcclxuICAgICAgcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMucHJldmVudE92ZXJmbG93ID0ge2JvdW5kYXJpZXNFbGVtZW50fTtcclxuICAgIH1cclxuICAgIGlmIChwb3BwZXJPcHRpb25zLm1vZGlmaWVycyAmJiB0aGlzLnBvcHBlck9wdGlvbnMucHJldmVudE92ZXJmbG93ICE9PSB1bmRlZmluZWQpIHtcclxuICAgICAgcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMucHJldmVudE92ZXJmbG93ID0gcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMucHJldmVudE92ZXJmbG93IHx8IHt9O1xyXG4gICAgICBwb3BwZXJPcHRpb25zLm1vZGlmaWVycy5wcmV2ZW50T3ZlcmZsb3cuZW5hYmxlZCA9IHRoaXMucG9wcGVyT3B0aW9ucy5wcmV2ZW50T3ZlcmZsb3c7XHJcbiAgICAgIGlmICghcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMucHJldmVudE92ZXJmbG93LmVuYWJsZWQpIHtcclxuICAgICAgICBwb3BwZXJPcHRpb25zLm1vZGlmaWVycy5oaWRlID0ge2VuYWJsZWQ6IGZhbHNlfTtcclxuICAgICAgfVxyXG4gICAgfVxyXG4gICAgdGhpcy5kZXRlcm1pbmVBcnJvd0NvbG9yKCk7XHJcbiAgICBwb3BwZXJPcHRpb25zLm1vZGlmaWVycyA9IE9iamVjdC5hc3NpZ24ocG9wcGVyT3B0aW9ucy5tb2RpZmllcnMsIHRoaXMucG9wcGVyT3B0aW9ucy5wb3BwZXJNb2RpZmllcnMpO1xyXG5cclxuICAgIHRoaXMucG9wcGVySW5zdGFuY2UgPSBuZXcgUG9wcGVyKFxyXG4gICAgICB0aGlzLnJlZmVyZW5jZU9iamVjdCxcclxuICAgICAgdGhpcy5wb3BwZXJWaWV3UmVmLm5hdGl2ZUVsZW1lbnQsXHJcbiAgICAgIHBvcHBlck9wdGlvbnMsXHJcbiAgICApO1xyXG5cclxuICAgICh0aGlzLnBvcHBlckluc3RhbmNlIGFzIGFueSkuZW5hYmxlRXZlbnRMaXN0ZW5lcnMoKTtcclxuICAgIHRoaXMuc2NoZWR1bGVVcGRhdGUoKTtcclxuICAgIHRoaXMudG9nZ2xlVmlzaWJpbGl0eSh0cnVlKTtcclxuICAgIHRoaXMuZ2xvYmFsUmVzaXplID0gdGhpcy5yZW5kZXJlci5saXN0ZW4oJ2RvY3VtZW50JywgJ3Jlc2l6ZScsIHRoaXMub25Eb2N1bWVudFJlc2l6ZS5iaW5kKHRoaXMpKVxyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBkZXRlcm1pbmVBcnJvd0NvbG9yKCkge1xyXG4gICAgWydiYWNrZ3JvdW5kLWNvbG9yJywgJ2JhY2tncm91bmRDb2xvciddLnNvbWUoKGNscikgPT4ge1xyXG4gICAgICBpZiAoIXRoaXMucG9wcGVyT3B0aW9ucy5zdHlsZXMpIHtcclxuICAgICAgICByZXR1cm4gZmFsc2U7XHJcbiAgICAgIH1cclxuICAgICAgaWYgKHRoaXMucG9wcGVyT3B0aW9ucy5zdHlsZXMuaGFzT3duUHJvcGVydHkoY2xyKSkge1xyXG4gICAgICAgIHRoaXMuYXJyb3dDb2xvciA9IHRoaXMucG9wcGVyT3B0aW9ucy5zdHlsZXNbY2xyXTtcclxuICAgICAgICByZXR1cm4gdHJ1ZTtcclxuICAgICAgfVxyXG4gICAgICByZXR1cm4gZmFsc2U7XHJcbiAgICB9KVxyXG4gIH1cclxuXHJcbiAgdXBkYXRlKCk6IHZvaWQge1xyXG4gICAgdGhpcy5wb3BwZXJJbnN0YW5jZSAmJiAodGhpcy5wb3BwZXJJbnN0YW5jZSBhcyBhbnkpLnVwZGF0ZSgpO1xyXG4gIH1cclxuXHJcbiAgc2NoZWR1bGVVcGRhdGUoKTogdm9pZCB7XHJcbiAgICB0aGlzLnBvcHBlckluc3RhbmNlICYmICh0aGlzLnBvcHBlckluc3RhbmNlIGFzIGFueSkuc2NoZWR1bGVVcGRhdGUoKTtcclxuICB9XHJcblxyXG4gIGhpZGUoKTogdm9pZCB7XHJcblxyXG4gICAgaWYgKHRoaXMucG9wcGVySW5zdGFuY2UpIHtcclxuICAgICAgdGhpcy5wb3BwZXJJbnN0YW5jZS5kZXN0cm95KCk7XHJcbiAgICB9XHJcbiAgICB0aGlzLnRvZ2dsZVZpc2liaWxpdHkoZmFsc2UpO1xyXG4gICAgdGhpcy5vbkhpZGRlbi5lbWl0KCk7XHJcbiAgfVxyXG5cclxuICB0b2dnbGVWaXNpYmlsaXR5KHN0YXRlOiBib29sZWFuKSB7XHJcbiAgICBpZiAoIXN0YXRlKSB7XHJcbiAgICAgIHRoaXMub3BhY2l0eSA9IDA7XHJcbiAgICAgIHRoaXMuZGlzcGxheVR5cGUgPSBcIm5vbmVcIjtcclxuICAgICAgdGhpcy5hcmlhSGlkZGVuID0gJ3RydWUnO1xyXG4gICAgfVxyXG4gICAgZWxzZSB7XHJcbiAgICAgIHRoaXMub3BhY2l0eSA9IDE7XHJcbiAgICAgIHRoaXMuZGlzcGxheVR5cGUgPSBcImJsb2NrXCI7XHJcbiAgICAgIHRoaXMuYXJpYUhpZGRlbiA9ICdmYWxzZSc7XHJcbiAgICB9XHJcbiAgICBpZiAoIXRoaXMuQ0RSWydkZXN0cm95ZWQnXSkge1xyXG4gICAgICB0aGlzLkNEUi5kZXRlY3RDaGFuZ2VzKCk7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICBleHRyYWN0QXBwbGllZENsYXNzTGlzdEV4cHIoY2xhc3NMaXN0Pzogc3RyaW5nKTogT2JqZWN0IHwgbnVsbCB7XHJcbiAgICBpZiAoIWNsYXNzTGlzdCB8fCB0eXBlb2YgY2xhc3NMaXN0ICE9PSAnc3RyaW5nJykge1xyXG4gICAgICByZXR1cm4gbnVsbDtcclxuICAgIH1cclxuICAgIHRyeSB7XHJcbiAgICAgIHJldHVybiBjbGFzc0xpc3RcclxuICAgICAgICAucmVwbGFjZSgvIC8sICcnKVxyXG4gICAgICAgIC5zcGxpdCgnLCcpXHJcbiAgICAgICAgLnJlZHVjZSgoYWNjLCBjbHNzKSA9PiB7XHJcbiAgICAgICAgICBhY2NbY2xzc10gPSB0cnVlO1xyXG4gICAgICAgICAgcmV0dXJuIGFjYztcclxuICAgICAgICB9LCB7fSlcclxuICAgIH1cclxuICAgIGNhdGNoIChlKSB7XHJcbiAgICAgIHJldHVybiBudWxsO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcHJpdmF0ZSBjbGVhckdsb2JhbFJlc2l6ZSgpIHtcclxuICAgIHRoaXMuZ2xvYmFsUmVzaXplICYmIHR5cGVvZiB0aGlzLmdsb2JhbFJlc2l6ZSA9PT0gJ2Z1bmN0aW9uJyAmJiB0aGlzLmdsb2JhbFJlc2l6ZSgpO1xyXG4gIH1cclxuXHJcbn1cclxuIl19