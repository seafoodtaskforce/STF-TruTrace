
export class DocumentType {
     /**
     * Static global variables
     */
     public static readonly DEFAULT_HEX_COLOR:string = "#32a8a6";
     public static readonly KEY_PREFIX_INTERNATIONALIZATION:string = "document_type_key_";

     public id: number = 0;
     public name: string;
     public value: string;
     public hexColorCode: string;
     public documentDesignation: string;
     

     public static clone(type:DocumentType){
          let clone: DocumentType = new DocumentType();
          clone.id = type.id;
          clone.documentDesignation = type.documentDesignation;
          clone.hexColorCode = type.hexColorCode;
          clone.name = type.name;
          clone.value = type.value;

          return clone;
     }

     constructor(){
          this.id = 0;
          this.name = "";
          this.value = "";
          this.hexColorCode = "";
          this.documentDesignation = "";
     }
}