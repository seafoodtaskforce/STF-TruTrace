import { IdentifiableEntity } from './identifiableEntity';

/**
 * PPliaction Resource used to define localized (i.e. internationalized) resources within the application
 */
export class AppResource extends IdentifiableEntity {
    key: string;
    locale: string;
    type: string;
    subType: string;
    value: string;
    platform:string;
    description:string;

    /**
     * The defining constructor for this class
     * @param id - the id of the entity; 0 if the id is not set
     * @param key - the key for this resource. This is in fact a sub-key
     * @param locale - the appended value to fully create a primary key. It defines the locale for the key
     * @param type -  a descritive type for the resource being defined (such as for example 'document' to identify a document resource)
     * @param subType - a subtype to further define the type of resource (such as for example a static label)
     * @param value - the actual value for the entity
     * @param platfrom - the platform for which this resource has been defined (for example 'android')
     */
    /*constructor(id : number, 
                key: string, 
                value: string,
                locale: string,
                type: string, 
                subType: string, 
                platfrom:string){
        
        super(id);
        this.key = key;
        this.locale = locale;
        this.type = type;
        this.subType = subType;
        this.value = value;
        this.platfrom = platfrom;
      }
      */
}