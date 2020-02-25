package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Created by AleaActaEst on 10/10/2017.
 */

public class GroupType extends LookupEntity {

    private String hexColorCode;
    private int orderIndex;

    /**
     * @return the orderIndex
     */
    public int getOrderIndex() {
        return orderIndex;
    }

    /**
     * @param orderIndex the orderIndex to set
     */
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getHexColorCode() {
        return hexColorCode;
    }

    public void setHexColorCode(String hexColorCode) {
        this.hexColorCode = hexColorCode;
    }

}
