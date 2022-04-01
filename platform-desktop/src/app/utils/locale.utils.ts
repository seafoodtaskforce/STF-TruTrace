import { AppResource } from '../models/AppResource';


export class LocaleUtils {

    
    public static defaultValueForMissingKey: string = "VALUE IS MISSING";
    //
    // Resource Types
    public static TYPE_DOCUMENT: string = "document";
    public static SUB_TYPE_METADATA: string = "type metadata";
    public static TYPE_STAGE: string = "stage";

    /**
     * Simple resource fetching method which will pair up a resource value 
     * with the input key and langauge
     * @param key  - the main key for this resource
     * @param language - the locale language for this resource request
     */
    static getInternationalizedString(key : string, language:string) {
        let compositeKey:string = LocaleUtils.createCompositeKey(key, language);
        //console.log('[Locale Services - Utilities ---------------- ] '.concat(compositeKey).concat(' '));

        let resource: AppResource = JSON.parse(localStorage.getItem(compositeKey));

        //
        // Check if the resource is in fact defined
        if(resource == undefined){
            return LocaleUtils.defaultValueForMissingKey;
        }else{
            return resource.value;
        }
    }

    /**
     * Creates a composite key out of the resource key and resource locale
     * @param key  - the main key for this resource
     * @param language - the locale language for this resource request
     */
    static createCompositeKey(key : string, language:string){
        return key + '_' + language;
    }

        /**
     * Creates a composite key out of the resource key and resource locale
     * @param key  - the main key for this resource
     * @param language - the locale language for this resource request
     */
    static loadResourceMap(resources : AppResource[]){
        if(resources == null){
            console.log('[Locale Services - NULL Resources ]');
            return;
        }
        //
        // Process the data
        for (const resource of resources) {
            // add the resource
            LocaleUtils.addResourceToResourceMap(resource);
        }
    }

    static addResourceToResourceMap(resource : AppResource){
        // create the key
        const key:string = LocaleUtils.createCompositeKey(resource.key, resource.locale);
        // store the data in session
        localStorage.setItem(key, JSON.stringify(resource));
        console.log('[Locale Services - Add Resource ] '.concat(key).concat(' ').concat(JSON.stringify(resource)));
    }  

    static removeResourceFromResourceMap(resource : AppResource){
        // create the key
        const key:string = LocaleUtils.createCompositeKey(resource.key, resource.locale);
        // store the data in session
        localStorage.removeItem(key);
        console.log('[Locale Services - Remove Resource ] '.concat(key).concat(' ').concat(JSON.stringify(resource)));
    }  

    static fetchResourceFromResourceMap(resourceKey:string , resourceLocale:string){
        // create the key
        const key:string = LocaleUtils.createCompositeKey(resourceKey, resourceLocale);
        // get teh data from session
        let resourceString: string = localStorage.getItem(key);
        let resource : AppResource = JSON.parse(resourceString);
        return resource;
    }

    static fetchResourceKeyByValue(resourceValue:string){
        var values = [],
        keys = Object.keys(localStorage),
        i = keys.length;

        while ( i-- ) {
            var key: string = keys[i].split("_")[0]
            //console.log('LOCALE UTILS ---- Resource <key>' + keys[i]);
            let resourceString: string = localStorage.getItem(keys[i]);
            try {
                let resource : AppResource = JSON.parse(resourceString);
                console.log('PDF IMPORT - TYPE<reversedNames>' + JSON.stringify(resource));
                    if(resource.value === resourceValue){
                        return resource.key;
                    }
            }
            catch(e) {
                if(e instanceof Error) {
                    // IDE type hinting now available
                    // properly handle Error e
                }
            }
            
        }
        return null;
    }

    static fetchAllResourceKeysByValue(resourceValue:string){
        var values = [],
        keys = Object.keys(localStorage),
        i = keys.length;

        while ( i-- ) {
            var key: string = keys[i].split("_")[0]
            //console.log('LOCALE UTILS ---- Resource <key>' + keys[i]);
            let resourceString: string = localStorage.getItem(keys[i]);
            try {
                let resource : AppResource = JSON.parse(resourceString);
                    if(resource.value === resourceValue){
                        values.push(resource.key);
                    }
            }
            catch(e) {
                if(e instanceof Error) {
                    // IDE type hinting now available
                    // properly handle Error e
                }
            }
            
        }
        return values;
    }

    /**
     * Get the flag icon for the specific iso code for a given country
     * @param isoName - the iso code for the country
     */
    static fetchCountryFlagByISO(isoName:string){
        if(isoName == 'en'){
            isoName = 'us';
        }

        return "flag-icon flag-icon-" + isoName + " flag-icon-squared";
    }

    static ciEquals(a : string, b : string) {
        return typeof a === 'string' && typeof b === 'string'
            ? a.localeCompare(b, undefined, { sensitivity: 'accent' }) === 0
            : a === b;
    }

}