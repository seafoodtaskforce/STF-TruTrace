export class ArrayUtils {

    /**
     * Simple resource fetching method which will pair up a resource value 
     * with the input key and langauge
     * @param key  - the main key for this resource
     * @param language - the locale language for this resource request
     */
    static removeDuplicates(data : any) {

        var newarr = [data[0]];
        for (var i=1; i<data.length; i++) {
            if (data[i].id !=data[i-1].id) newarr.push(data[i]);
        }
        return newarr;
    }
}