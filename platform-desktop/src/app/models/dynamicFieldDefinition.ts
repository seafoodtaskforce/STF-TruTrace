export class DynamicFieldDefinition {

    public id: number=0;
    public orgID: number=0;
	public docTypeId: number=0;
	public fieldTypeId: number=0;
	public docTypeName: string;
	public displayName: string;
	public description: string;
	public fieldType: string;
	public maxLength: number=0;
	public isRequired: boolean= true;
	public isDocIdText: string= 'No';
	public isRequiredText: string= 'Required';
	public ordinal: number=0;
	public isDocId: boolean= false;
	public ocrMatchText: string='';
	public ocrGrabLength: number=0;
    
}