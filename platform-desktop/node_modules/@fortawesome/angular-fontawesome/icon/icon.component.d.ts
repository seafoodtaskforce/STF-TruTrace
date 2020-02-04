import { OnChanges, SimpleChanges } from '@angular/core';
import { Icon } from '@fortawesome/fontawesome-svg-core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
/**
 * Fontawesome icon.
 */
export declare class FaIconComponent implements OnChanges {
    private sanitizer;
    icon: Icon;
    renderedIconHTML: SafeHtml;
    constructor(sanitizer: DomSanitizer);
    private params;
    private iconSpec;
    private iconProp;
    private title?;
    private spin?;
    private pulse?;
    private mask?;
    private styles?;
    private flip?;
    private size?;
    private pull?;
    private border?;
    private inverse?;
    private symbol?;
    private listItem?;
    private rotate?;
    private fixedWidth?;
    private classes?;
    private transform?;
    ngOnChanges(changes: SimpleChanges): void;
    /**
     * Updating icon spec.
     */
    private updateIconSpec();
    /**
     * Updating params by component props.
     */
    private updateParams();
    /**
     * Updating icon by params and icon spec.
     */
    private updateIcon();
    /**
     * Rendering icon.
     */
    private renderIcon();
}
