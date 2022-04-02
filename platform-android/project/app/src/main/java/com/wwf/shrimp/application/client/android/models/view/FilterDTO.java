package com.wwf.shrimp.application.client.android.models.view;

import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.User;

import java.util.Date;

public class FilterDTO {

    public final static int FILTER_STATUS_CLEAR = 1;
    public final static int FILTER_STATUS_SET = 2;
    public final static int FILTER_STATUS_DISABLED= 4;


    private int status = FilterDTO.FILTER_STATUS_CLEAR;
    private Date dateFrom;
    private Date dateTo;
    private DocumentType docType;
    private User recipient;

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
