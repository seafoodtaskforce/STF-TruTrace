package com.wwf.shrimp.application.client.android.services.impl;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.exceptions.ConfigurationException;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;
import com.wwf.shrimp.application.client.android.services.ConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by AleaActaEst on 09/11/2017.
 */

/**
 * Configuration Services based on Java Properties
 */
public class PropertiesConfigurationService implements ConfigurationService {

    /**
     * Android context value
     */
    private Context context;

    /**
     * Configuration properties source
     */
    Properties properties = new Properties();

    /**
     * Logging tag used to identify the module
     */
    private static final String TAG = "ConfigurationService";

    /**
     * Main constructor
     * @param context - the context to initialize this instance with
     */
    public PropertiesConfigurationService(Context context){
        this.context = context;
    }


    @Override
    public String readConfigurationProperty(String name) throws ConfigurationException {
        return properties.getProperty(name);
    }

    @Override
    public ConfigurationData readAllProperties() throws ConfigurationException {
        ConfigurationData configData = new ConfigurationData();

        // read all the data properties
        //

        // server
        configData.setServerURL(readConfigurationProperty("server.backend.api.url"));
        configData.setApplicationInfixURL(readConfigurationProperty("server.backend.api.application.url"));
        // security
        configData.setRestLoginURL(readConfigurationProperty("rest.api.security.login"));
        configData.setRestLogoutURL(readConfigurationProperty("rest.api.security.logout"));
        // organization

        // tags
        configData.setRestTagFetchAllURL(readConfigurationProperty("rest.api.tag.fetchall"));
        configData.setRestTagFetchByDocumentSyncIdURL(readConfigurationProperty("rest.api.tag.fetch.by.document.syncid"));
        configData.setRestTagAttachByDocumentSyncIdURL(readConfigurationProperty("rest.api.tag.attach.by.sync"));
        configData.setRestTagCreateURL(readConfigurationProperty("rest.api.tag.create"));

        // documents
        configData.setRestDocumentFetchAllURL(readConfigurationProperty("rest.api.document.fetchall"));
        configData.setRestDocumentFetchLinkedDocsBySyncIdURL(readConfigurationProperty("rest.api.document.linked.fetch.by.syncid"));
        configData.setRestDocumentAttachLinkedDocsBySyncIdURL(readConfigurationProperty("rest.api.document.linked.attach.by.syncid"));
        configData.setRestDocumentFetchallDocsToLinkURL(readConfigurationProperty("rest.api.document.linked.fetch.all.to.link"));
        configData.setRestDocumentFetchallDocsToAttachURL(readConfigurationProperty("rest.api.document.attachment.fetch.all.to.attach"));
        configData.setRestDocumentFetchAttachedDocsBySyncIdURL(readConfigurationProperty("rest.api.document.attached.fetch.by.syncid"));
        configData.setRestDocumentAttachAttachmentDocsBySyncIdURL(readConfigurationProperty("rest.api.document.attachment.attach.by.syncid"));
        configData.setRestDocumentSearchURL(readConfigurationProperty("rest.api.document.search"));
        configData.setRestDocumentCreateURL(readConfigurationProperty("rest.api.document.create"));
        configData.setRestDocumentUpdateURL(readConfigurationProperty("rest.api.document.update"));
        configData.setRestDocumentDeleteURL(readConfigurationProperty("rest.api.document.delete"));
        configData.setRestDocumentDeletePagesURL(readConfigurationProperty("rest.api.document.delete.pages"));
        configData.setRestDocumentTypesFetchAllURL(readConfigurationProperty("rest.api.document.alltypes"));
        configData.setRestDocumentFetchPageByDocIdURL(readConfigurationProperty("rest.api.document.fetch.page.by.docid"));
        configData.setRestDocumentFetchPageThumbnailByDocIdURL(readConfigurationProperty("rest.api.document.fetch.page.thumbnail.by.docid"));
        configData.setRestDocumentMarkAsReadURL(readConfigurationProperty("rest.api.document.mark.as.read"));
        configData.setRestRecipientsAddByDocumentSyncIdURL(readConfigurationProperty("rest.api.document.recipients.add.by.syncid"));
        configData.setRestRecipientsFetchByDocumentSyncIdURL(readConfigurationProperty("rest.api.document.recipients.fetch.by.syncid"));
        configData.setRestDocumentAddPagesURL(readConfigurationProperty("rest.api.POST.document.add.pages"));
        configData.setRestDocumentAddNotesURL(readConfigurationProperty("rest.api.POST.document.add.notes"));
        configData.setRestDocumentSetStatusURL(readConfigurationProperty("rest.api.POST.document.set.status"));
        configData.setRestDocumentSetLocationURL(readConfigurationProperty("rest.api.POST.document.set.location"));
        configData.setRestDocumentAttachmentFetchDocCollectionURL(readConfigurationProperty("rest.api.document.attachment.fetch.attach.doc.collection"));
        configData.setRestDocumentLinkedFetchDocCollectionURL(readConfigurationProperty("rest.api.document.linked.fetch.link.doc.collection"));
        configData.setRestDocumentFetchRecipientsForUserURL(readConfigurationProperty("rest.api.document.recipients.fetch.by.for.user"));

        // users
        configData.setRestUserFetchAllURL(readConfigurationProperty("rest.api.user.fetchall"));
        configData.setRestUserFetchProfileImage(readConfigurationProperty("rest.api.user.fetch.profile.image"));
        configData.setRestUserUpdateProfileImage(readConfigurationProperty("rest.api.user.update.profile.image"));
        configData.setRestUserUpdateData(readConfigurationProperty("rest.api.user.update.data"));
        configData.setRestUserUpdateCredentials(readConfigurationProperty("rest.api.user.update.credentials"));
        configData.setRestUserUpdateProfile(readConfigurationProperty("rest.api.user.update.profile"));
        configData.setWebUserRegistrationURL(readConfigurationProperty("url.user.registration"));


        // notifications
        configData.setRestNotificationFetchByUserName(readConfigurationProperty("rest.api.notifications.fetch.by.user"));


        // global variables
        configData.setGlobalConstantTaggingCustomPrefix(readConfigurationProperty("tagging.custom.prefix"));
        configData.setGlobalConstantGalleryImageRemovalPrefix(readConfigurationProperty("gallery.image.removal.prefix"));
        configData.setClientSocketTimeoutInSeconds(Integer.parseInt(readConfigurationProperty("client.socket.timeout.seconds")));
        configData.setClientRecordFetchThrottleFlag(Boolean.parseBoolean(readConfigurationProperty("client.record.throttle.flag")));
        configData.setClientRecordFetchThrottlePageLimit(Integer.parseInt(readConfigurationProperty("client.record.throttle.page.size")));

        // application resources
        configData.setRestAppResourcesFetchAllURL(readConfigurationProperty("rest.api.app.resources.fetch.all"));

        return configData;
    }

    @Override
    public void open() throws ConfigurationException {
        Resources resources = context.getResources();
        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            properties.load(rawResource);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file.");
        }
    }

    @Override
    public void close() throws ConfigurationException {
        //
        // in this case do nothing
    }
}
