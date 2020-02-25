package com.wwf.shrimp.application.client.android.models.dto.search;

import com.wwf.shrimp.application.client.android.models.dto.DocumentType;

import java.util.Date;

/**
 * Search criteria specific to Document data
 */
public class DocumentSearchCriteria  extends BaseSearchCriteria {

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DocumentSearchCriteria [userName=" + userName + ", organizationId=" + organizationId + ", groupId="
                + groupId + ", userId=" + userId + ", docTYpe=" + docType + ", dateFrom=" + dateFrom + ", dateTo="
                + dateTo + "]";
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }
    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    /**
     * @return the organizationId
     */
    public long getOrganizationId() {
        return organizationId;
    }
    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }
    /**
     * @return the groupId
     */
    public long getGroupId() {
        return groupId;
    }
    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    /**
     * @return the userId
     */
    public long getUserId() {
        return userId;
    }
    /**
     * @param userId the userId to set
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }
    /**
     * @return the docTYpe
     */
    public DocumentType getDocType() {
        return docType;
    }
    /**
     * @param docType the docType to set
     */
    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }
    /**
     * @return the dateFrom
     */
    public Date getDateFrom() {
        return dateFrom;
    }
    /**
     * @param dateFrom the dateFrom to set
     */
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }
    /**
     * @return the dateTo
     */
    public Date getDateTo() {
        return dateTo;
    }
    /**
     * @param dateTo the dateTo to set
     */
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    private String userName;
    private long organizationId;
    private long groupId;
    private long userId;
    private DocumentType docType;
    private Date dateFrom;
    private Date dateTo;
}
