import { LookupEntity } from './lookupEntity';

export class DynamicFieldType extends LookupEntity {
    public static readonly NUMERIC_TYPE = 2;
    public static readonly ALPHANUMERIC_TYPE = 3;
    public static readonly DATE_TYPE = 4; 
    public static readonly EXPIRY_DATE_TYPE = 5;

    public valueMask : string;
}