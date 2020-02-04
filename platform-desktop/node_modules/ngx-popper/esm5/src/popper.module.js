/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes,extraRequire,missingReturn,unusedPrivateMembers,uselessCode} checked by tsc
 */
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { PopperController } from './popper-directive';
import { PopperContent } from './popper-content';
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
export { NgxPopperModule };
export { ɵ0 };
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicG9wcGVyLm1vZHVsZS5qcyIsInNvdXJjZVJvb3QiOiJuZzovL25neC1wb3BwZXIvIiwic291cmNlcyI6WyJzcmMvcG9wcGVyLm1vZHVsZS50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7O0FBQUEsT0FBTyxFQUFDLFlBQVksRUFBQyxNQUFNLGlCQUFpQixDQUFDO0FBQzdDLE9BQU8sRUFBc0IsUUFBUSxFQUFDLE1BQU0sZUFBZSxDQUFDO0FBRTVELE9BQU8sRUFBQyxnQkFBZ0IsRUFBQyxNQUFNLG9CQUFvQixDQUFDO0FBQ3BELE9BQU8sRUFBQyxhQUFhLEVBQUMsTUFBTSxrQkFBa0IsQ0FBQztTQW1CSixFQUFFO0FBakI3QztJQUFBO0lBMkJBLENBQUM7Ozs7SUFOQyx1Q0FBYTs7O0lBQWI7SUFDQSxDQUFDOzs7OztJQUVhLHVCQUFPOzs7O0lBQXJCLFVBQXNCLGlCQUE0QztRQUE1QyxrQ0FBQSxFQUFBLHNCQUE0QztRQUNoRSxPQUFPLEVBQUMsUUFBUSxFQUFFLGVBQWUsRUFBRSxTQUFTLEVBQUUsQ0FBQyxFQUFDLE9BQU8sRUFBRSxnQkFBZ0IsRUFBRSxRQUFRLEVBQUUsaUJBQWlCLEVBQUMsQ0FBQyxFQUFDLENBQUM7SUFDNUcsQ0FBQzs7Z0JBMUJGLFFBQVEsU0FBQztvQkFDUixPQUFPLEVBQUU7d0JBQ1AsWUFBWTtxQkFDYjtvQkFDRCxZQUFZLEVBQUU7d0JBQ1osZ0JBQWdCO3dCQUNoQixhQUFhO3FCQUNkO29CQUNELE9BQU8sRUFBRTt3QkFDUCxnQkFBZ0I7d0JBQ2hCLGFBQWE7cUJBQ2Q7b0JBQ0QsZUFBZSxFQUFFO3dCQUNmLGFBQWE7cUJBQ2Q7b0JBQ0QsU0FBUyxFQUFFO3dCQUNUOzRCQUNFLE9BQU8sRUFBRSxnQkFBZ0IsRUFBRSxRQUFRLElBQUk7eUJBQ3hDO3FCQUFDO2lCQUNMOztJQVFELHNCQUFDO0NBQUEsQUEzQkQsSUEyQkM7U0FQWSxlQUFlIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHtDb21tb25Nb2R1bGV9IGZyb20gXCJAYW5ndWxhci9jb21tb25cIjtcclxuaW1wb3J0IHtNb2R1bGVXaXRoUHJvdmlkZXJzLCBOZ01vZHVsZX0gZnJvbSBcIkBhbmd1bGFyL2NvcmVcIjtcclxuaW1wb3J0IHtQb3BwZXJDb250ZW50T3B0aW9uc30gZnJvbSAnLi9wb3BwZXItbW9kZWwnO1xyXG5pbXBvcnQge1BvcHBlckNvbnRyb2xsZXJ9IGZyb20gJy4vcG9wcGVyLWRpcmVjdGl2ZSc7XHJcbmltcG9ydCB7UG9wcGVyQ29udGVudH0gZnJvbSAnLi9wb3BwZXItY29udGVudCc7XHJcblxyXG5ATmdNb2R1bGUoe1xyXG4gIGltcG9ydHM6IFtcclxuICAgIENvbW1vbk1vZHVsZVxyXG4gIF0sXHJcbiAgZGVjbGFyYXRpb25zOiBbXHJcbiAgICBQb3BwZXJDb250cm9sbGVyLFxyXG4gICAgUG9wcGVyQ29udGVudFxyXG4gIF0sXHJcbiAgZXhwb3J0czogW1xyXG4gICAgUG9wcGVyQ29udHJvbGxlcixcclxuICAgIFBvcHBlckNvbnRlbnRcclxuICBdLFxyXG4gIGVudHJ5Q29tcG9uZW50czogW1xyXG4gICAgUG9wcGVyQ29udGVudFxyXG4gIF0sXHJcbiAgcHJvdmlkZXJzOiBbXHJcbiAgICB7XHJcbiAgICAgIHByb3ZpZGU6ICdwb3BwZXJEZWZhdWx0cycsIHVzZVZhbHVlOiB7fVxyXG4gICAgfV1cclxufSlcclxuZXhwb3J0IGNsYXNzIE5neFBvcHBlck1vZHVsZSB7XHJcbiAgbmdEb0Jvb3RzdHJhcCgpIHtcclxuICB9XHJcblxyXG4gIHB1YmxpYyBzdGF0aWMgZm9yUm9vdChwb3BwZXJCYXNlT3B0aW9uczogUG9wcGVyQ29udGVudE9wdGlvbnMgPSB7fSk6IE1vZHVsZVdpdGhQcm92aWRlcnMge1xyXG4gICAgcmV0dXJuIHtuZ01vZHVsZTogTmd4UG9wcGVyTW9kdWxlLCBwcm92aWRlcnM6IFt7cHJvdmlkZTogJ3BvcHBlckRlZmF1bHRzJywgdXNlVmFsdWU6IHBvcHBlckJhc2VPcHRpb25zfV19O1xyXG4gIH1cclxufSJdfQ==