/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Renderer2, ViewChild, ViewContainerRef, ViewEncapsulation, } from "@angular/core";
import Popper from 'popper.js';
import { Placements, Triggers } from './popper-model';
export class PopperContent {
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicG9wcGVyLWNvbnRlbnQuanMiLCJzb3VyY2VSb290Ijoibmc6Ly9uZ3gtcG9wcGVyLyIsInNvdXJjZXMiOlsic3JjL3BvcHBlci1jb250ZW50LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7Ozs7QUFBQSxPQUFPLEVBQ0wsdUJBQXVCLEVBQUUsaUJBQWlCLEVBQzFDLFNBQVMsRUFDVCxVQUFVLEVBQ1YsWUFBWSxFQUNaLFlBQVksRUFFWixTQUFTLEVBQ1QsU0FBUyxFQUNULGdCQUFnQixFQUNoQixpQkFBaUIsR0FDbEIsTUFBTSxlQUFlLENBQUE7QUFDdEIsT0FBTyxNQUFNLE1BQU0sV0FBVyxDQUFBO0FBQzlCLE9BQU8sRUFBQyxVQUFVLEVBQXdCLFFBQVEsRUFBQyxNQUFNLGdCQUFnQixDQUFBO0FBOEJ6RSxNQUFNLE9BQU8sYUFBYTs7Ozs7OztJQTBEeEIsWUFDUyxPQUFtQixFQUNsQixRQUFtQixFQUNuQixPQUF5QixFQUN6QixHQUFzQjtRQUh2QixZQUFPLEdBQVAsT0FBTyxDQUFZO1FBQ2xCLGFBQVEsR0FBUixRQUFRLENBQVc7UUFDbkIsWUFBTyxHQUFQLE9BQU8sQ0FBa0I7UUFDekIsUUFBRyxHQUFILEdBQUcsQ0FBbUI7UUE1RGhDLGtCQUFhLEdBQXlCLG1CQUFzQjtZQUMxRCxnQkFBZ0IsRUFBRSxLQUFLO1lBQ3ZCLHFCQUFxQixFQUFFLEtBQUs7WUFDNUIsU0FBUyxFQUFFLFVBQVUsQ0FBQyxJQUFJO1lBQzFCLGlCQUFpQixFQUFFLEVBQUU7WUFDckIsT0FBTyxFQUFFLFFBQVEsQ0FBQyxLQUFLO1lBQ3ZCLGFBQWEsRUFBRSxLQUFLO1lBQ3BCLFlBQVksRUFBRSxLQUFLO1lBQ25CLGVBQWUsRUFBRSxFQUFFO1NBQ3BCLEVBQUEsQ0FBQztRQUlGLGdCQUFXLEdBQVksS0FBSyxDQUFDO1FBRTdCLGFBQVEsR0FBRyxJQUFJLFlBQVksRUFBRSxDQUFDO1FBTTlCLGdCQUFXLEdBQVcsTUFBTSxDQUFDO1FBRTdCLFlBQU8sR0FBVyxDQUFDLENBQUM7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QUUsaUJBQWlCLENBQUMsSUFBSTtnQkFDckMsZUFBZSxFQUFFLHVCQUF1QixDQUFDLE1BQU07Z0JBQy9DLFFBQVEsRUFBRTs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7O0dBcUJUOzthQUVGOzs7O1lBdkNDLFVBQVU7WUFJVixTQUFTO1lBRVQsZ0JBQWdCO1lBUlMsaUJBQWlCOzs7NEJBK0V6QyxTQUFTLFNBQUMsZUFBZTswQkFHekIsWUFBWSxTQUFDLFdBQVc7MEJBS3hCLFlBQVksU0FBQyxZQUFZOzs7O0lBM0MxQixzQ0FTRTs7SUFFRix3Q0FBNkI7O0lBRTdCLG9DQUE2Qjs7SUFFN0IsaUNBQThCOztJQUU5Qiw2QkFBYTs7SUFFYix1Q0FBdUI7O0lBRXZCLG9DQUE2Qjs7SUFFN0IsZ0NBQW9COztJQUVwQixtQ0FBNEI7O0lBRTVCLG1DQUFpQzs7SUFFakMsaUNBQW1COztJQUVuQiw4QkFBc0I7Ozs7O0lBRXRCLHFDQUEwQjs7SUFFMUIsc0NBQzBCOztJQXFCeEIsZ0NBQTBCOzs7OztJQUMxQixpQ0FBMkI7Ozs7O0lBQzNCLGdDQUFpQzs7Ozs7SUFDakMsNEJBQThCIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHtcclxuICBDaGFuZ2VEZXRlY3Rpb25TdHJhdGVneSwgQ2hhbmdlRGV0ZWN0b3JSZWYsXHJcbiAgQ29tcG9uZW50LFxyXG4gIEVsZW1lbnRSZWYsXHJcbiAgRXZlbnRFbWl0dGVyLFxyXG4gIEhvc3RMaXN0ZW5lcixcclxuICBPbkRlc3Ryb3ksXHJcbiAgUmVuZGVyZXIyLFxyXG4gIFZpZXdDaGlsZCxcclxuICBWaWV3Q29udGFpbmVyUmVmLFxyXG4gIFZpZXdFbmNhcHN1bGF0aW9uLFxyXG59IGZyb20gXCJAYW5ndWxhci9jb3JlXCJcclxuaW1wb3J0IFBvcHBlciBmcm9tICdwb3BwZXIuanMnXHJcbmltcG9ydCB7UGxhY2VtZW50cywgUG9wcGVyQ29udGVudE9wdGlvbnMsIFRyaWdnZXJzfSBmcm9tICcuL3BvcHBlci1tb2RlbCdcclxuXHJcbkBDb21wb25lbnQoe1xyXG4gIHNlbGVjdG9yOiBcInBvcHBlci1jb250ZW50XCIsXHJcbiAgZW5jYXBzdWxhdGlvbjogVmlld0VuY2Fwc3VsYXRpb24uTm9uZSxcclxuICBjaGFuZ2VEZXRlY3Rpb246IENoYW5nZURldGVjdGlvblN0cmF0ZWd5Lk9uUHVzaCxcclxuICB0ZW1wbGF0ZTogYFxyXG4gICAgPGRpdiAjcG9wcGVyVmlld1JlZlxyXG4gICAgICAgICBbY2xhc3Mubmd4cF9fY29udGFpbmVyXT1cIiFwb3BwZXJPcHRpb25zLmRpc2FibGVEZWZhdWx0U3R5bGluZ1wiXHJcbiAgICAgICAgIFtjbGFzcy5uZ3hwX19hbmltYXRpb25dPVwiIXBvcHBlck9wdGlvbnMuZGlzYWJsZUFuaW1hdGlvblwiXHJcbiAgICAgICAgIFtzdHlsZS5kaXNwbGF5XT1cImRpc3BsYXlUeXBlXCJcclxuICAgICAgICAgW3N0eWxlLm9wYWNpdHldPVwib3BhY2l0eVwiXHJcbiAgICAgICAgIFtuZ1N0eWxlXT1cInBvcHBlck9wdGlvbnMuc3R5bGVzXCJcclxuICAgICAgICAgW25nQ2xhc3NdPVwiZXh0cmFjdEFwcGxpZWRDbGFzc0xpc3RFeHByKHBvcHBlck9wdGlvbnMuYXBwbHlDbGFzcylcIlxyXG4gICAgICAgICBhdHRyLmFyaWEtaGlkZGVuPVwie3thcmlhSGlkZGVufX1cIlxyXG4gICAgICAgICBbYXR0ci5hcmlhLWRlc2NyaWJlZGJ5XT1cInBvcHBlck9wdGlvbnMuYXJpYURlc2NyaWJlIHx8IG51bGxcIlxyXG4gICAgICAgICBhdHRyLnJvbGU9XCJ7e3BvcHBlck9wdGlvbnMuYXJpYVJvbGV9fVwiPlxyXG4gICAgICA8ZGl2IGNsYXNzPVwibmd4cF9faW5uZXJcIiAqbmdJZj1cInRleHRcIiBbaW5uZXJIVE1MXT1cInRleHRcIj5cclxuICAgICAgICA8bmctY29udGVudD48L25nLWNvbnRlbnQ+XHJcbiAgICAgIDwvZGl2PlxyXG4gICAgICA8ZGl2IGNsYXNzPVwibmd4cF9faW5uZXJcIiAqbmdJZj1cIiF0ZXh0XCI+XHJcbiAgICAgICAgPG5nLWNvbnRlbnQ+PC9uZy1jb250ZW50PlxyXG4gICAgICA8L2Rpdj5cclxuICAgICAgPGRpdiBjbGFzcz1cIm5neHBfX2Fycm93XCIgW3N0eWxlLmJvcmRlci1jb2xvcl09XCJhcnJvd0NvbG9yXCIgW2NsYXNzLl9fZm9yY2UtYXJyb3ddPVwiYXJyb3dDb2xvclwiXHJcbiAgICAgICAgICAgW25nQ2xhc3NdPVwiZXh0cmFjdEFwcGxpZWRDbGFzc0xpc3RFeHByKHBvcHBlck9wdGlvbnMuYXBwbHlBcnJvd0NsYXNzKVwiPjwvZGl2PlxyXG5cclxuICAgIDwvZGl2PlxyXG4gIGAsXHJcbiAgc3R5bGVVcmxzOiBbJy4vcG9wcGVyLWNvbnRlbnQuY3NzJ10sXHJcbn0pXHJcbmV4cG9ydCBjbGFzcyBQb3BwZXJDb250ZW50IGltcGxlbWVudHMgT25EZXN0cm95IHtcclxuXHJcbiAgcG9wcGVyT3B0aW9uczogUG9wcGVyQ29udGVudE9wdGlvbnMgPSA8UG9wcGVyQ29udGVudE9wdGlvbnM+e1xyXG4gICAgZGlzYWJsZUFuaW1hdGlvbjogZmFsc2UsXHJcbiAgICBkaXNhYmxlRGVmYXVsdFN0eWxpbmc6IGZhbHNlLFxyXG4gICAgcGxhY2VtZW50OiBQbGFjZW1lbnRzLkF1dG8sXHJcbiAgICBib3VuZGFyaWVzRWxlbWVudDogJycsXHJcbiAgICB0cmlnZ2VyOiBUcmlnZ2Vycy5IT1ZFUixcclxuICAgIHBvc2l0aW9uRml4ZWQ6IGZhbHNlLFxyXG4gICAgYXBwZW5kVG9Cb2R5OiBmYWxzZSxcclxuICAgIHBvcHBlck1vZGlmaWVyczoge31cclxuICB9O1xyXG5cclxuICByZWZlcmVuY2VPYmplY3Q6IEhUTUxFbGVtZW50O1xyXG5cclxuICBpc01vdXNlT3ZlcjogYm9vbGVhbiA9IGZhbHNlO1xyXG5cclxuICBvbkhpZGRlbiA9IG5ldyBFdmVudEVtaXR0ZXIoKTtcclxuXHJcbiAgdGV4dDogc3RyaW5nO1xyXG5cclxuICBwb3BwZXJJbnN0YW5jZTogUG9wcGVyO1xyXG5cclxuICBkaXNwbGF5VHlwZTogc3RyaW5nID0gXCJub25lXCI7XHJcblxyXG4gIG9wYWNpdHk6IG51bWJlciA9IDA7XHJcblxyXG4gIGFyaWFIaWRkZW46IHN0cmluZyA9ICd0cnVlJztcclxuXHJcbiAgYXJyb3dDb2xvcjogc3RyaW5nIHwgbnVsbCA9IG51bGw7XHJcblxyXG4gIG9uVXBkYXRlOiBGdW5jdGlvbjtcclxuXHJcbiAgc3RhdGU6IGJvb2xlYW4gPSB0cnVlO1xyXG5cclxuICBwcml2YXRlIGdsb2JhbFJlc2l6ZTogYW55O1xyXG5cclxuICBAVmlld0NoaWxkKFwicG9wcGVyVmlld1JlZlwiKVxyXG4gIHBvcHBlclZpZXdSZWY6IEVsZW1lbnRSZWY7XHJcblxyXG4gIEBIb3N0TGlzdGVuZXIoJ21vdXNlb3ZlcicpXHJcbiAgb25Nb3VzZU92ZXIoKSB7XHJcbiAgICB0aGlzLmlzTW91c2VPdmVyID0gdHJ1ZTtcclxuICB9XHJcblxyXG4gIEBIb3N0TGlzdGVuZXIoJ21vdXNlbGVhdmUnKVxyXG4gIHNob3dPbkxlYXZlKCkge1xyXG4gICAgdGhpcy5pc01vdXNlT3ZlciA9IGZhbHNlO1xyXG4gICAgaWYgKHRoaXMucG9wcGVyT3B0aW9ucy50cmlnZ2VyICE9PSBUcmlnZ2Vycy5IT1ZFUiAmJiAhdGhpcy5wb3BwZXJPcHRpb25zLmhpZGVPbk1vdXNlTGVhdmUpIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG4gICAgdGhpcy5oaWRlKCk7XHJcbiAgfVxyXG5cclxuICBvbkRvY3VtZW50UmVzaXplKCkge1xyXG4gICAgdGhpcy51cGRhdGUoKTtcclxuICB9XHJcblxyXG4gIGNvbnN0cnVjdG9yKFxyXG4gICAgcHVibGljIGVsZW1SZWY6IEVsZW1lbnRSZWYsXHJcbiAgICBwcml2YXRlIHJlbmRlcmVyOiBSZW5kZXJlcjIsXHJcbiAgICBwcml2YXRlIHZpZXdSZWY6IFZpZXdDb250YWluZXJSZWYsXHJcbiAgICBwcml2YXRlIENEUjogQ2hhbmdlRGV0ZWN0b3JSZWYpIHtcclxuICB9XHJcblxyXG4gIG5nT25EZXN0cm95KCkge1xyXG4gICAgdGhpcy5jbGVhbigpO1xyXG4gICAgaWYodGhpcy5wb3BwZXJPcHRpb25zLmFwcGVuZFRvICYmIHRoaXMuZWxlbVJlZiAmJiB0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudCAmJiB0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudC5wYXJlbnROb2RlKXtcclxuICAgICAgdGhpcy52aWV3UmVmLmRldGFjaCgpO1xyXG4gICAgICB0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudC5wYXJlbnROb2RlLnJlbW92ZUNoaWxkKHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50KTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIGNsZWFuKCkge1xyXG4gICAgdGhpcy50b2dnbGVWaXNpYmlsaXR5KGZhbHNlKTtcclxuICAgIGlmICghdGhpcy5wb3BwZXJJbnN0YW5jZSkge1xyXG4gICAgICByZXR1cm47XHJcbiAgICB9XHJcbiAgICAodGhpcy5wb3BwZXJJbnN0YW5jZSBhcyBhbnkpLmRpc2FibGVFdmVudExpc3RlbmVycygpO1xyXG4gICAgdGhpcy5wb3BwZXJJbnN0YW5jZS5kZXN0cm95KCk7XHJcblxyXG4gIH1cclxuXHJcbiAgc2hvdygpOiB2b2lkIHtcclxuICAgIGlmICghdGhpcy5yZWZlcmVuY2VPYmplY3QpIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG5cclxuICAgIGNvbnN0IGFwcGVuZFRvUGFyZW50ID0gdGhpcy5wb3BwZXJPcHRpb25zLmFwcGVuZFRvICYmIGRvY3VtZW50LnF1ZXJ5U2VsZWN0b3IodGhpcy5wb3BwZXJPcHRpb25zLmFwcGVuZFRvKTtcclxuICAgIGlmIChhcHBlbmRUb1BhcmVudCAmJiB0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudC5wYXJlbnROb2RlICE9PSBhcHBlbmRUb1BhcmVudCkge1xyXG4gICAgICB0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudC5wYXJlbnROb2RlICYmIHRoaXMuZWxlbVJlZi5uYXRpdmVFbGVtZW50LnBhcmVudE5vZGUucmVtb3ZlQ2hpbGQodGhpcy5lbGVtUmVmLm5hdGl2ZUVsZW1lbnQpO1xyXG4gICAgICBhcHBlbmRUb1BhcmVudC5hcHBlbmRDaGlsZCh0aGlzLmVsZW1SZWYubmF0aXZlRWxlbWVudCk7XHJcbiAgICB9XHJcblxyXG4gICAgbGV0IHBvcHBlck9wdGlvbnM6IFBvcHBlci5Qb3BwZXJPcHRpb25zID0gPFBvcHBlci5Qb3BwZXJPcHRpb25zPntcclxuICAgICAgcGxhY2VtZW50OiB0aGlzLnBvcHBlck9wdGlvbnMucGxhY2VtZW50LFxyXG4gICAgICBwb3NpdGlvbkZpeGVkOiB0aGlzLnBvcHBlck9wdGlvbnMucG9zaXRpb25GaXhlZCxcclxuICAgICAgbW9kaWZpZXJzOiB7XHJcbiAgICAgICAgYXJyb3c6IHtcclxuICAgICAgICAgIGVsZW1lbnQ6IHRoaXMucG9wcGVyVmlld1JlZi5uYXRpdmVFbGVtZW50LnF1ZXJ5U2VsZWN0b3IoJy5uZ3hwX19hcnJvdycpXHJcbiAgICAgICAgfVxyXG4gICAgICB9XHJcbiAgICB9O1xyXG4gICAgaWYgKHRoaXMub25VcGRhdGUpIHtcclxuICAgICAgcG9wcGVyT3B0aW9ucy5vblVwZGF0ZSA9IHRoaXMub25VcGRhdGUgYXMgYW55O1xyXG4gICAgfVxyXG5cclxuICAgIGxldCBib3VuZGFyaWVzRWxlbWVudCA9IHRoaXMucG9wcGVyT3B0aW9ucy5ib3VuZGFyaWVzRWxlbWVudCAmJiBkb2N1bWVudC5xdWVyeVNlbGVjdG9yKHRoaXMucG9wcGVyT3B0aW9ucy5ib3VuZGFyaWVzRWxlbWVudCk7XHJcblxyXG4gICAgaWYgKHBvcHBlck9wdGlvbnMubW9kaWZpZXJzICYmIGJvdW5kYXJpZXNFbGVtZW50KSB7XHJcbiAgICAgIHBvcHBlck9wdGlvbnMubW9kaWZpZXJzLnByZXZlbnRPdmVyZmxvdyA9IHtib3VuZGFyaWVzRWxlbWVudH07XHJcbiAgICB9XHJcbiAgICBpZiAocG9wcGVyT3B0aW9ucy5tb2RpZmllcnMgJiYgdGhpcy5wb3BwZXJPcHRpb25zLnByZXZlbnRPdmVyZmxvdyAhPT0gdW5kZWZpbmVkKSB7XHJcbiAgICAgIHBvcHBlck9wdGlvbnMubW9kaWZpZXJzLnByZXZlbnRPdmVyZmxvdyA9IHBvcHBlck9wdGlvbnMubW9kaWZpZXJzLnByZXZlbnRPdmVyZmxvdyB8fCB7fTtcclxuICAgICAgcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMucHJldmVudE92ZXJmbG93LmVuYWJsZWQgPSB0aGlzLnBvcHBlck9wdGlvbnMucHJldmVudE92ZXJmbG93O1xyXG4gICAgICBpZiAoIXBvcHBlck9wdGlvbnMubW9kaWZpZXJzLnByZXZlbnRPdmVyZmxvdy5lbmFibGVkKSB7XHJcbiAgICAgICAgcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMuaGlkZSA9IHtlbmFibGVkOiBmYWxzZX07XHJcbiAgICAgIH1cclxuICAgIH1cclxuICAgIHRoaXMuZGV0ZXJtaW5lQXJyb3dDb2xvcigpO1xyXG4gICAgcG9wcGVyT3B0aW9ucy5tb2RpZmllcnMgPSBPYmplY3QuYXNzaWduKHBvcHBlck9wdGlvbnMubW9kaWZpZXJzLCB0aGlzLnBvcHBlck9wdGlvbnMucG9wcGVyTW9kaWZpZXJzKTtcclxuXHJcbiAgICB0aGlzLnBvcHBlckluc3RhbmNlID0gbmV3IFBvcHBlcihcclxuICAgICAgdGhpcy5yZWZlcmVuY2VPYmplY3QsXHJcbiAgICAgIHRoaXMucG9wcGVyVmlld1JlZi5uYXRpdmVFbGVtZW50LFxyXG4gICAgICBwb3BwZXJPcHRpb25zLFxyXG4gICAgKTtcclxuXHJcbiAgICAodGhpcy5wb3BwZXJJbnN0YW5jZSBhcyBhbnkpLmVuYWJsZUV2ZW50TGlzdGVuZXJzKCk7XHJcbiAgICB0aGlzLnNjaGVkdWxlVXBkYXRlKCk7XHJcbiAgICB0aGlzLnRvZ2dsZVZpc2liaWxpdHkodHJ1ZSk7XHJcbiAgICB0aGlzLmdsb2JhbFJlc2l6ZSA9IHRoaXMucmVuZGVyZXIubGlzdGVuKCdkb2N1bWVudCcsICdyZXNpemUnLCB0aGlzLm9uRG9jdW1lbnRSZXNpemUuYmluZCh0aGlzKSlcclxuICB9XHJcblxyXG4gIHByaXZhdGUgZGV0ZXJtaW5lQXJyb3dDb2xvcigpIHtcclxuICAgIFsnYmFja2dyb3VuZC1jb2xvcicsICdiYWNrZ3JvdW5kQ29sb3InXS5zb21lKChjbHIpID0+IHtcclxuICAgICAgaWYgKCF0aGlzLnBvcHBlck9wdGlvbnMuc3R5bGVzKSB7XHJcbiAgICAgICAgcmV0dXJuIGZhbHNlO1xyXG4gICAgICB9XHJcbiAgICAgIGlmICh0aGlzLnBvcHBlck9wdGlvbnMuc3R5bGVzLmhhc093blByb3BlcnR5KGNscikpIHtcclxuICAgICAgICB0aGlzLmFycm93Q29sb3IgPSB0aGlzLnBvcHBlck9wdGlvbnMuc3R5bGVzW2Nscl07XHJcbiAgICAgICAgcmV0dXJuIHRydWU7XHJcbiAgICAgIH1cclxuICAgICAgcmV0dXJuIGZhbHNlO1xyXG4gICAgfSlcclxuICB9XHJcblxyXG4gIHVwZGF0ZSgpOiB2b2lkIHtcclxuICAgIHRoaXMucG9wcGVySW5zdGFuY2UgJiYgKHRoaXMucG9wcGVySW5zdGFuY2UgYXMgYW55KS51cGRhdGUoKTtcclxuICB9XHJcblxyXG4gIHNjaGVkdWxlVXBkYXRlKCk6IHZvaWQge1xyXG4gICAgdGhpcy5wb3BwZXJJbnN0YW5jZSAmJiAodGhpcy5wb3BwZXJJbnN0YW5jZSBhcyBhbnkpLnNjaGVkdWxlVXBkYXRlKCk7XHJcbiAgfVxyXG5cclxuICBoaWRlKCk6IHZvaWQge1xyXG5cclxuICAgIGlmICh0aGlzLnBvcHBlckluc3RhbmNlKSB7XHJcbiAgICAgIHRoaXMucG9wcGVySW5zdGFuY2UuZGVzdHJveSgpO1xyXG4gICAgfVxyXG4gICAgdGhpcy50b2dnbGVWaXNpYmlsaXR5KGZhbHNlKTtcclxuICAgIHRoaXMub25IaWRkZW4uZW1pdCgpO1xyXG4gIH1cclxuXHJcbiAgdG9nZ2xlVmlzaWJpbGl0eShzdGF0ZTogYm9vbGVhbikge1xyXG4gICAgaWYgKCFzdGF0ZSkge1xyXG4gICAgICB0aGlzLm9wYWNpdHkgPSAwO1xyXG4gICAgICB0aGlzLmRpc3BsYXlUeXBlID0gXCJub25lXCI7XHJcbiAgICAgIHRoaXMuYXJpYUhpZGRlbiA9ICd0cnVlJztcclxuICAgIH1cclxuICAgIGVsc2Uge1xyXG4gICAgICB0aGlzLm9wYWNpdHkgPSAxO1xyXG4gICAgICB0aGlzLmRpc3BsYXlUeXBlID0gXCJibG9ja1wiO1xyXG4gICAgICB0aGlzLmFyaWFIaWRkZW4gPSAnZmFsc2UnO1xyXG4gICAgfVxyXG4gICAgaWYgKCF0aGlzLkNEUlsnZGVzdHJveWVkJ10pIHtcclxuICAgICAgdGhpcy5DRFIuZGV0ZWN0Q2hhbmdlcygpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgZXh0cmFjdEFwcGxpZWRDbGFzc0xpc3RFeHByKGNsYXNzTGlzdD86IHN0cmluZyk6IE9iamVjdCB8IG51bGwge1xyXG4gICAgaWYgKCFjbGFzc0xpc3QgfHwgdHlwZW9mIGNsYXNzTGlzdCAhPT0gJ3N0cmluZycpIHtcclxuICAgICAgcmV0dXJuIG51bGw7XHJcbiAgICB9XHJcbiAgICB0cnkge1xyXG4gICAgICByZXR1cm4gY2xhc3NMaXN0XHJcbiAgICAgICAgLnJlcGxhY2UoLyAvLCAnJylcclxuICAgICAgICAuc3BsaXQoJywnKVxyXG4gICAgICAgIC5yZWR1Y2UoKGFjYywgY2xzcykgPT4ge1xyXG4gICAgICAgICAgYWNjW2Nsc3NdID0gdHJ1ZTtcclxuICAgICAgICAgIHJldHVybiBhY2M7XHJcbiAgICAgICAgfSwge30pXHJcbiAgICB9XHJcbiAgICBjYXRjaCAoZSkge1xyXG4gICAgICByZXR1cm4gbnVsbDtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHByaXZhdGUgY2xlYXJHbG9iYWxSZXNpemUoKSB7XHJcbiAgICB0aGlzLmdsb2JhbFJlc2l6ZSAmJiB0eXBlb2YgdGhpcy5nbG9iYWxSZXNpemUgPT09ICdmdW5jdGlvbicgJiYgdGhpcy5nbG9iYWxSZXNpemUoKTtcclxuICB9XHJcblxyXG59XHJcbiJdfQ==