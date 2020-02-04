import { Text, TextParams } from '@fortawesome/fontawesome-svg-core';
import { FaLayersTextBaseComponent } from './layers-text-base.component';
/**
 * Fontawesome layers text.
 */
export declare class FaLayersTextComponent extends FaLayersTextBaseComponent {
    private spin?;
    private pulse?;
    private flip?;
    private size?;
    private pull?;
    private border?;
    private inverse?;
    private listItem?;
    private rotate?;
    private fixedWidth?;
    private transform?;
    /**
     * Updating params by component props.
     */
    protected updateParams(): void;
    protected renderFontawesomeObject(content: string, params?: TextParams): Text;
}
