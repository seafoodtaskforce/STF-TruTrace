import { LookupEntity } from './lookupEntity';

export class OrganizationStage extends LookupEntity {
    orderIndex: number;
    orgID: number;
    colorHexCode: string='';

    public static clone(stage:OrganizationStage){
        let clone: OrganizationStage = new OrganizationStage();
        clone.id = stage.id;
        clone.name = stage.name;
        clone.value = stage.value;
        clone.colorHexCode = stage.colorHexCode;
        clone.orderIndex = stage.orderIndex;
        clone.orgID = stage.orgID;

        return clone;

   }
}