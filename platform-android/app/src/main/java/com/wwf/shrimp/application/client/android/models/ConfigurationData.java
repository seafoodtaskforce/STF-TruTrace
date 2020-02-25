package com.wwf.shrimp.application.client.android.models;

/**
 * Simple configuration POJO for all configuration data needed by the application
 * @author AleaActaEst
 */
public class ConfigurationData {

    /**
     * The server URL such as for example: http://52.73.145.74:8080/
     * Note the trailing "/" which needs to be added
     */
    private String serverURL;
    /**
     * This is the application UTL which will then lead to the API.
     * For example: WWFShrimpProject_v2/api_v2
     * Note that here there is *no* trailing "/"
     */
    private String applicationInfixURL;

    /**
     * This is a socket timeout limit for when getting data from the backend
     */
    private int clientSocketTimeoutInSeconds;

    /**
     * This is for testing purposes to limit the number of records
     */
    private boolean clientRecordFetchThrottleFlag;
    private int clientRecordFetchThrottlePageLimit;

    private String restLoginURL;
    private String restLogoutURL;
    private String restOrganizationFetchFlatURL;


    private String restRecipientsFetchByDocumentSyncIdURL;
    private String restRecipientsAddByDocumentSyncIdURL;

    /**
     * Document REST Gestures
     */
    private String restDocumentFetchAllURL;
    private String restDocumentFetchLinkedDocsBySyncIdURL;
    private String restDocumentAttachLinkedDocsBySyncIdURL;
    private String restDocumentFetchallDocsToLinkURL;
    private String restDocumentFetchallDocsToAttachURL;
    private String restDocumentFetchAttachedDocsBySyncIdURL;
    private String restDocumentAttachAttachmentDocsBySyncIdURL;
    private String restDocumentAttachmentFetchDocCollectionURL;
    private String restDocumentLinkedFetchDocCollectionURL;

    private String restDocumentCreateURL;
    private String restDocumentUpdateURL;
    private String restDocumentDeleteURL;
    private String restDocumentDeletePagesURL;
    private String restDocumentSearchURL;
    private String restDocumentTypesFetchAllURL;
    private String restDocumentMarkAsReadURL;
    private String restDocumentFetchPageByDocIdURL;
    private String restDocumentFetchPageThumbnailByDocIdURL;
    private String restDocumentAddPagesURL;
    private String restDocumentAddNotesURL;
    private String restDocumentSetStatusURL;
    private String restDocumentSetLocationURL;

    /**
     * User REST Gestures
     */
    private String restUserFetchAllURL;
    private String restUserFetchProfileImage;
    private String restUserUpdateProfileImage;
    private String restUserUpdateData;
    private String restUserUpdateCredentials;


    /**
     * Tag REST Gestures
     */
    private String restTagCreateURL;
    private String restTagFetchAllURL;
    private String restTagFetchByDocumentSyncIdURL;
    private String restTagAttachByDocumentSyncIdURL;

    /**
     * Resource REST Gestures
     */
    private String restAppResourcesFetchAllURL;


    public String getRestNotificationFetchByUserName() {
        return restNotificationFetchByUserName;
    }

    public void setRestNotificationFetchByUserName(String restNotificationFetchByUserName) {
        this.restNotificationFetchByUserName = restNotificationFetchByUserName;
    }

    private String restNotificationFetchByUserName;


    private String globalConstantTaggingCustomPrefix;
    private String globalConstantGalleryImageRemovalPrefix;


    public String getRestDocumentFetchPageThumbnailByDocIdURL() {
        return restDocumentFetchPageThumbnailByDocIdURL;
    }

    public void setRestDocumentFetchPageThumbnailByDocIdURL(String restDocumentFetchPageThumbnailByDocIdURL) {
        this.restDocumentFetchPageThumbnailByDocIdURL = restDocumentFetchPageThumbnailByDocIdURL;
    }

    public String getGlobalConstantGalleryImageRemovalPrefix() {
        return globalConstantGalleryImageRemovalPrefix;
    }

    public void setGlobalConstantGalleryImageRemovalPrefix(String globalConstantGalleryImageRemovalPrefix) {
        this.globalConstantGalleryImageRemovalPrefix = globalConstantGalleryImageRemovalPrefix;
    }

    public String getRestUserFetchProfileImage() {
        return restUserFetchProfileImage;
    }

    public void setRestUserFetchProfileImage(String restUserFetchProfileImage) {
        this.restUserFetchProfileImage = restUserFetchProfileImage;
    }

    public String getRestLoginURL() {
        return restLoginURL;
    }

    public void setRestLoginURL(String restLoginURL) {
        this.restLoginURL = restLoginURL;
    }

    public String getApplicationInfixURL() {
        return applicationInfixURL;
    }

    public void setApplicationInfixURL(String applicationInfixURL) {
        this.applicationInfixURL = applicationInfixURL;
    }

    public String getServerURL() {
        return this.serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getRestLogoutURL() {
        return restLogoutURL;
    }

    public void setRestLogoutURL(String restLogoutURL) {
        this.restLogoutURL = restLogoutURL;
    }

    public String getRestOrganizationFetchFlatURL() {
        return restOrganizationFetchFlatURL;
    }

    public void setRestOrganizationFetchFlatURL(String restOrganizationFetchFlatURL) {
        this.restOrganizationFetchFlatURL = restOrganizationFetchFlatURL;
    }

    public String getRestTagFetchAllURL() {
        return restTagFetchAllURL;
    }

    public void setRestTagFetchAllURL(String restTagFetchAllURL) {
        this.restTagFetchAllURL = restTagFetchAllURL;
    }

    public String getRestTagFetchByDocumentSyncIdURL() {
        return restTagFetchByDocumentSyncIdURL;
    }

    public void setRestTagFetchByDocumentSyncIdURL(String restTagFetchByDocumentSyncIdURL) {
        this.restTagFetchByDocumentSyncIdURL = restTagFetchByDocumentSyncIdURL;
    }

    public String getRestTagAttachByDocumentSyncIdURL() {
        return restTagAttachByDocumentSyncIdURL;
    }

    public void setRestTagAttachByDocumentSyncIdURL(String restTagAttachByDocumentSyncIdURL) {
        this.restTagAttachByDocumentSyncIdURL = restTagAttachByDocumentSyncIdURL;
    }

    public String getRestDocumentFetchAllURL() {
        return restDocumentFetchAllURL;
    }

    public void setRestDocumentFetchAllURL(String restDocumentFetchAllURL) {
        this.restDocumentFetchAllURL = restDocumentFetchAllURL;
    }

    public String getRestDocumentFetchLinkedDocsBySyncIdURL() {
        return restDocumentFetchLinkedDocsBySyncIdURL;
    }

    public void setRestDocumentFetchLinkedDocsBySyncIdURL(String restDocumentFetchLinkedDocsBySyncIdURL) {
        this.restDocumentFetchLinkedDocsBySyncIdURL = restDocumentFetchLinkedDocsBySyncIdURL;
    }

    public String getRestDocumentAttachLinkedDocsBySyncIdURL() {
        return restDocumentAttachLinkedDocsBySyncIdURL;
    }

    public void setRestDocumentAttachLinkedDocsBySyncIdURL(String restDocumentAttachLinkedDocsBySyncIdURL) {
        this.restDocumentAttachLinkedDocsBySyncIdURL = restDocumentAttachLinkedDocsBySyncIdURL;
    }

    public String getRestDocumentFetchAttachedDocsBySyncIdURL() {
        return restDocumentFetchAttachedDocsBySyncIdURL;
    }

    public void setRestDocumentFetchAttachedDocsBySyncIdURL(String restDocumentFetchAttachedDocsBySyncIdURL) {
        this.restDocumentFetchAttachedDocsBySyncIdURL = restDocumentFetchAttachedDocsBySyncIdURL;
    }

    public String getRestDocumentAttachAttachmentDocsBySyncIdURL() {
        return restDocumentAttachAttachmentDocsBySyncIdURL;
    }

    public void setRestDocumentAttachAttachmentDocsBySyncIdURL(String restDocumentAttachAttachmentDocsBySyncIdURL) {
        this.restDocumentAttachAttachmentDocsBySyncIdURL = restDocumentAttachAttachmentDocsBySyncIdURL;
    }

    public String getRestDocumentCreateURL() {
        return restDocumentCreateURL;
    }

    public void setRestDocumentCreateURL(String restDocumentCreateURL) {
        this.restDocumentCreateURL = restDocumentCreateURL;
    }

    public String getRestDocumentDeleteURL() {
        return restDocumentDeleteURL;
    }

    public void setRestDocumentDeleteURL(String restDocumentDeleteURL) {
        this.restDocumentDeleteURL = restDocumentDeleteURL;
    }

    public String getRestDocumentSearchURL() {
        return restDocumentSearchURL;
    }

    public void setRestDocumentSearchURL(String restDocumentSearchURL) {
        this.restDocumentSearchURL = restDocumentSearchURL;
    }

    public String getRestDocumentTypesFetchAllURL() {
        return restDocumentTypesFetchAllURL;
    }

    public void setRestDocumentTypesFetchAllURL(String restDocumentTypesFetchAllURL) {
        this.restDocumentTypesFetchAllURL = restDocumentTypesFetchAllURL;
    }

    public String getRestUserFetchAllURL() {
        return restUserFetchAllURL;
    }

    public void setRestUserFetchAllURL(String restUserFetchAllURL) {
        this.restUserFetchAllURL = restUserFetchAllURL;
    }

    public String getRestDocumentFetchPageByDocIdURL() {
        return restDocumentFetchPageByDocIdURL;
    }

    public void setRestDocumentFetchPageByDocIdURL(String restDocumentFetchPageByDocIdURL) {
        this.restDocumentFetchPageByDocIdURL = restDocumentFetchPageByDocIdURL;
    }

    public String getRestDocumentMarkAsReadURL() {
        return restDocumentMarkAsReadURL;
    }

    public void setRestDocumentMarkAsReadURL(String restDocumentMarkAsReadURL) {
        this.restDocumentMarkAsReadURL = restDocumentMarkAsReadURL;
    }

    public String getGlobalConstantTaggingCustomPrefix() {
        return globalConstantTaggingCustomPrefix;
    }

    public void setGlobalConstantTaggingCustomPrefix(String globalConstantTaggingCustomPrefix) {
        this.globalConstantTaggingCustomPrefix = globalConstantTaggingCustomPrefix;
    }

    public String getRestTagCreateURL() {
        return restTagCreateURL;
    }

    public void setRestTagCreateURL(String restTagCreateURL) {
        this.restTagCreateURL = restTagCreateURL;
    }

    public String getRestRecipientsFetchByDocumentSyncIdURL() {
        return restRecipientsFetchByDocumentSyncIdURL;
    }

    public void setRestRecipientsFetchByDocumentSyncIdURL(String restRecipientsFetchByDocumentSyncIdURL) {
        this.restRecipientsFetchByDocumentSyncIdURL = restRecipientsFetchByDocumentSyncIdURL;
    }

    public String getRestRecipientsAddByDocumentSyncIdURL() {
        return restRecipientsAddByDocumentSyncIdURL;
    }

    public void setRestRecipientsAddByDocumentSyncIdURL(String restRecipientsAddByDocumentSyncIdURL) {
        this.restRecipientsAddByDocumentSyncIdURL = restRecipientsAddByDocumentSyncIdURL;
    }

    public int getClientSocketTimeoutInSeconds() {
        return clientSocketTimeoutInSeconds;
    }

    public void setClientSocketTimeoutInSeconds(int clientSocketTimeoutInSeconds) {
        this.clientSocketTimeoutInSeconds = clientSocketTimeoutInSeconds;
    }

    public String getRestDocumentAddPagesURL() {
        return restDocumentAddPagesURL;
    }

    public void setRestDocumentAddPagesURL(String restDocumentAddPagesURL) {
        this.restDocumentAddPagesURL = restDocumentAddPagesURL;
    }

    public String getRestDocumentUpdateURL() {
        return restDocumentUpdateURL;
    }

    public void setRestDocumentUpdateURL(String restDocumentUpdateURL) {
        this.restDocumentUpdateURL = restDocumentUpdateURL;
    }

    public String getRestDocumentAddNotesURL() {
        return restDocumentAddNotesURL;
    }

    public void setRestDocumentAddNotesURL(String restDocumentAddNotesURL) {
        this.restDocumentAddNotesURL = restDocumentAddNotesURL;
    }

    public String getRestDocumentSetStatusURL() {
        return restDocumentSetStatusURL;
    }

    public void setRestDocumentSetStatusURL(String restDocumentSetStatusURL) {
        this.restDocumentSetStatusURL = restDocumentSetStatusURL;
    }

    public String getRestUserUpdateProfileImage() {
        return restUserUpdateProfileImage;
    }

    public void setRestUserUpdateProfileImage(String restUserUpdateProfileImage) {
        this.restUserUpdateProfileImage = restUserUpdateProfileImage;
    }

    public boolean isClientRecordFetchThrottleFlag() {
        return clientRecordFetchThrottleFlag;
    }

    public void setClientRecordFetchThrottleFlag(boolean clientRecordFetchThrottleFlag) {
        this.clientRecordFetchThrottleFlag = clientRecordFetchThrottleFlag;
    }

    public int getClientRecordFetchThrottlePageLimit() {
        return clientRecordFetchThrottlePageLimit;
    }

    public void setClientRecordFetchThrottlePageLimit(int clientRecordFetchThrottlePageLimit) {
        this.clientRecordFetchThrottlePageLimit = clientRecordFetchThrottlePageLimit;
    }

    public String getRestUserUpdateData() {
        return restUserUpdateData;
    }

    public void setRestUserUpdateData(String restUserUpdateData) {
        this.restUserUpdateData = restUserUpdateData;
    }

    public String getRestUserUpdateCredentials() {
        return restUserUpdateCredentials;
    }

    public void setRestUserUpdateCredentials(String restUserUpdateCredentials) {
        this.restUserUpdateCredentials = restUserUpdateCredentials;
    }

    public String getRestDocumentFetchallDocsToLinkURL() {
        return restDocumentFetchallDocsToLinkURL;
    }

    public void setRestDocumentFetchallDocsToLinkURL(String restDocumentFetchallDocsToLinkURL) {
        this.restDocumentFetchallDocsToLinkURL = restDocumentFetchallDocsToLinkURL;
    }


    public String getRestDocumentFetchallDocsToAttachURL() {
        return restDocumentFetchallDocsToAttachURL;
    }

    public void setRestDocumentFetchallDocsToAttachURL(String restDocumentFetchallDocsToAttachURL) {
        this.restDocumentFetchallDocsToAttachURL = restDocumentFetchallDocsToAttachURL;
    }

    public String getRestDocumentSetLocationURL() {
        return restDocumentSetLocationURL;
    }

    public void setRestDocumentSetLocationURL(String restDocumentSetLocationURL) {
        this.restDocumentSetLocationURL = restDocumentSetLocationURL;
    }

    public String getRestDocumentDeletePagesURL() {
        return restDocumentDeletePagesURL;
    }

    public void setRestDocumentDeletePagesURL(String restDocumentDeletePagesURL) {
        this.restDocumentDeletePagesURL = restDocumentDeletePagesURL;
    }

    public String getRestAppResourcesFetchAllURL() {
        return restAppResourcesFetchAllURL;
    }

    public void setRestAppResourcesFetchAllURL(String restAppResourcesFetchAllURL) {
        this.restAppResourcesFetchAllURL = restAppResourcesFetchAllURL;
    }

    public String getRestDocumentAttachmentFetchDocCollectionURL() {
        return restDocumentAttachmentFetchDocCollectionURL;
    }

    public void setRestDocumentAttachmentFetchDocCollectionURL(String restDocumentAttachmentFetchDocCollectionURL) {
        this.restDocumentAttachmentFetchDocCollectionURL = restDocumentAttachmentFetchDocCollectionURL;
    }

    public String getRestDocumentLinkedFetchDocCollectionURL() {
        return restDocumentLinkedFetchDocCollectionURL;
    }

    public void setRestDocumentLinkedFetchDocCollectionURL(String restDocumentLinkedFetchDocCollectionURL) {
        this.restDocumentLinkedFetchDocCollectionURL = restDocumentLinkedFetchDocCollectionURL;
    }
}
