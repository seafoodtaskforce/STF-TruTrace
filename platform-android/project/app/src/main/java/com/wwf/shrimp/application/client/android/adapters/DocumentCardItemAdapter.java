package com.wwf.shrimp.application.client.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldDefinition;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an adapter for the document_card_layout visualization.
 * @author AleaActaEst
 */

public class DocumentCardItemAdapter extends RecyclerView.Adapter<DocumentCardItemAdapter.MyViewHolder> {

    private List<DocumentCardItem> dataSet;
    private Context context;
    // global session data access
    private SessionData globalVariable = null;




    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewGroupTypeName;
        TextView textViewUserName;
        TextView textViewDocumentType;
        TextView textViewTimestamp;
        TextView textViewCustomTag;
        View viewColoredBar;
        ImageView imageViewDocumentNotRead;
        ImageView imageViewDocumentTagged;
        ImageView imageViewDocumentLinked;
        ImageView imageViewDocumentAttached;
        ImageView imageViewDocumentRecipients;
        ImageView imageViewDocumentInfo;
        ImageView imageViewSyncStatus;
        ProgressBar progressBarBackendSync;
        TextView textViewDocumentStatus;



        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewGroupTypeName = (TextView) itemView.findViewById(R.id.textViewGroupTypeName);
            this.textViewDocumentType = (TextView) itemView.findViewById(R.id.textViewDocumentType);
            this.textViewUserName = (TextView) itemView.findViewById(R.id.textViewUsername);
            this.textViewDocumentStatus = (TextView) itemView.findViewById(R.id.textViewDocumentStatus);

            this.textViewTimestamp = (TextView) itemView.findViewById(R.id.textViewTimestamp);
            this.textViewCustomTag = (TextView) itemView.findViewById(R.id.textViewCustomTag);
            this.viewColoredBar = itemView.findViewById(R.id.colored_bar);
            this.imageViewDocumentNotRead = (ImageView)itemView.findViewById(R.id.imageViewDocumentNotRead);
            this.imageViewDocumentTagged = (ImageView)itemView.findViewById(R.id.imageViewDocumentTagged);
            this.imageViewDocumentLinked = (ImageView)itemView.findViewById(R.id.imageViewDocumentLinked);
            this.imageViewDocumentAttached = (ImageView)itemView.findViewById(R.id.imageViewDocumentAttached);
            this.imageViewDocumentRecipients = (ImageView)itemView.findViewById(R.id.imageViewDocumentRecipients);
            this.imageViewDocumentInfo = (ImageView)itemView.findViewById(R.id.imageViewDocumentInfo);
            this.imageViewSyncStatus = (ImageView)itemView.findViewById(R.id.imageViewSyncStatus);
            this.progressBarBackendSync = (ProgressBar)itemView.findViewById(R.id.progressBarBackendSyncing);

        }
    }

    public DocumentCardItemAdapter(List<DocumentCardItem> data, Context context) {

        this.dataSet = data;
        this.context = context;
        this.globalVariable = (SessionData) context.getApplicationContext();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_card_layout, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewGroupTypeName = holder.textViewGroupTypeName;
        TextView textViewDocumentType = holder.textViewDocumentType;
        TextView textViewUserName = holder.textViewUserName;
        TextView textViewDocumentStatus = holder.textViewDocumentStatus;
        TextView textViewTimestamp = holder.textViewTimestamp;
        TextView textViewCustomTag = holder.textViewCustomTag;
        View viewColoredBar = holder.viewColoredBar;
        ImageView imageViewDocumentNotRead = holder.imageViewDocumentNotRead;
        ImageView imageViewDocumentTagged = holder.imageViewDocumentTagged;
        ImageView imageViewDocumentLinked = holder.imageViewDocumentLinked;
        ImageView imageViewDocumentAttached = holder.imageViewDocumentAttached;
        ImageView imageViewDocumentRecipients = holder.imageViewDocumentRecipients;
        ImageView imageViewDocumentInfo = holder.imageViewDocumentInfo;
        ImageView imageViewSyncStatus = holder.imageViewSyncStatus;
        ProgressBar progressBarBackendSync = holder.progressBarBackendSync;

        textViewGroupTypeName.setText(""+dataSet.get(listPosition).getGroupType());
        //int stringDocTypeResource = context.getResources().getIdentifier(dataSet.get(listPosition).getDocumentType().getName()
        //                , "string"
        //                , context.getPackageName());
        //context.getResources().getString(stringDocTypeResource);

        String documentType = globalVariable.getInternationalizedResourceString(dataSet.get(listPosition).getDocumentType().getName());
        textViewDocumentType.setText(documentType);

        textViewUserName.setText(dataSet.get(listPosition).getUsername());

        textViewTimestamp.setText(DateUtils.formatDateTimeToString(dataSet.get(listPosition).getCreationTimestamp()));
        if(!dataSet.get(listPosition).isWasRead() && !dataSet.get(listPosition).getUsername().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
            textViewTimestamp.setTypeface(null, Typeface.BOLD);
            textViewUserName.setTypeface(null, Typeface.BOLD);
            textViewGroupTypeName.setTypeface(null, Typeface.BOLD);
            imageViewDocumentNotRead.setVisibility(View.VISIBLE);
        }else{
            textViewTimestamp.setTypeface(null, Typeface.NORMAL);
            textViewUserName.setTypeface(null, Typeface.NORMAL);
            textViewGroupTypeName.setTypeface(null, Typeface.NORMAL);
            imageViewDocumentNotRead.setVisibility(View.INVISIBLE);
        }

        //
        // status
        // textViewDocumentStatus.setText(dataSet.get(listPosition).getStatus());
        textViewDocumentStatus.setText(
                DocumentPOJOUtils.getTranslatedStatus(globalVariable, dataSet.get(listPosition).getStatus())
        );


        // tag icon visibility
        //
        if(dataSet.get(listPosition).getTags().size() > 0){
            String uri = "@drawable/ic_tag_document_active";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentTagged.setImageDrawable(res);

            //
            // Custom tag
            String customTag = DocumentPOJOUtils.getCustomTag(dataSet.get(listPosition).getTags());
            //if(customTag != null && customTag.length() > 0){
            //    textViewCustomTag.setVisibility(View.VISIBLE);
            //     textViewCustomTag.setText(customTag);
            //}else{
            //    textViewCustomTag.setVisibility(View.GONE);
            //}
            textViewCustomTag.setVisibility(View.GONE);

        }else{
            String uri = "@drawable/ic_tag_document_inactive";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentTagged.setImageDrawable(res);
            textViewCustomTag.setVisibility(View.GONE);
        }



        //
        // linked documents visibility
        if(dataSet.get(listPosition).getLinkedDocuments().size() > 0){
            String uri = "@drawable/ic_link_document_active";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentLinked.setImageDrawable(res);
            imageViewDocumentLinked.setVisibility(View.VISIBLE);
        }else{
            String uri = "@drawable/ic_link_document_inactive";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentLinked.setImageDrawable(res);
        }

        //
        // attached documents visibility
        if(dataSet.get(listPosition).getAttachedDocuments().size() > 0){
            String uri = "@drawable/ic_attach_documents_active";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentAttached.setImageDrawable(res);
            imageViewDocumentAttached.setVisibility(View.VISIBLE);
        }else{
            String uri = "@drawable/ic_attach_documents_inactive";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentAttached.setImageDrawable(res);
        }

        //
        // recipients documents visibility
        if(dataSet.get(listPosition).getRecipients().size() > 0){
            String uri = "@drawable/ic_group_add_black_24dp";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentRecipients.setImageDrawable(res);
            imageViewDocumentRecipients.setVisibility(View.VISIBLE);
        }else{
            String uri = "@drawable/ic_group_add_inactive";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentRecipients.setImageDrawable(res);
        }

        //
        // doc info fields visibility
        if(dataSet.get(listPosition).getDynamicFieldData().size() > 0){
            String uri = "@drawable/ic_info_24dp";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentInfo.setImageDrawable(res);
            imageViewDocumentInfo.setVisibility(View.VISIBLE);


            //
            // Custom tag <TODO refactor to dof fields>
            String formattedDocField = DocumentPOJOUtils.getFormattedDocFields(dataSet.get(listPosition).getDynamicFieldData(), globalVariable);
            if(formattedDocField != null && formattedDocField.length() > 0){
                textViewCustomTag.setVisibility(View.VISIBLE);
                textViewCustomTag.setText(formattedDocField);
            }else{
                textViewCustomTag.setVisibility(View.GONE);
            }
        }else{
            String uri = "@drawable/ic_info_24dp_inactive";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewDocumentInfo.setImageDrawable(res);
        }

        //
        // Sync status documents visibility
        if(dataSet.get(listPosition).isBackendSynced()){
            String uri = "@drawable/ic_all_synced";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewSyncStatus.setImageDrawable(res);
            imageViewSyncStatus.setVisibility(View.VISIBLE);
        }else{
            String uri = "@drawable/ic_all_cached";

            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            imageViewSyncStatus.setImageDrawable(res);
        }

        // backend sync visibility
        if(!dataSet.get(listPosition).isBackendSynced()){
            progressBarBackendSync.setVisibility(View.VISIBLE);
            progressBarBackendSync.getIndeterminateDrawable().setColorFilter(Color.RED
                    , android.graphics.PorterDuff.Mode.MULTIPLY);

        }else{
            progressBarBackendSync.setVisibility(View.INVISIBLE);
        }

        //long colorIndex = dataSet.get(listPosition).getDocumentType().;
       // int colorId = MappingUtilities.documentTypeIDToRColorMap.get(colorIndex);

        // ContextCompat.getColor(context, colorId);
        // String color = context.getResources().getString(MappingUtilities.documentTypeIDToRColorMap.get(colorIndex));
        if(dataSet.get(listPosition).getTypeHEXColor() != null) {
            viewColoredBar.setBackgroundColor(Color.parseColor(dataSet.get(listPosition).getTypeHEXColor()));
        }
        // viewColoredBar.setBackgroundColor(Color.parseColor(dataSet.get(listPosition).getTypeHEXColor()));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void addDataItem(DocumentCardItem dataItem){

        dataSet.add(0, dataItem);
    }

    public List<DocumentCardItem> getDataSet() {
        return dataSet;
    }

    public DocumentCardItem findCardItemBySyncId(String syncId){
        for(int i=0; i< dataSet.size(); i++){
            if(dataSet.get(i).getSyncID().equals(syncId)){
                return dataSet.get(i);
            }
        }
        return null;
    }

    public int replaceCardItem(DocumentCardItem cardItem){
        for(int i=0; i< dataSet.size(); i++){
            if(dataSet.get(i).getSyncID().equals(cardItem.getSyncID())){
                dataSet.set(i, cardItem);
                return i;
            }
        }
        return -1;
    }

    public DocumentCardItem findCardItemById(long id){
        for(int i=0; i< dataSet.size(); i++){
            if(dataSet.get(i).getId() == id){
                return dataSet.get(i);
            }
        }
        return null;
    }

    public int findCardItemPositionBySyncId(String syncId){
        for(int i=0; i< dataSet.size(); i++){
            if(dataSet.get(i).getSyncID().equals(syncId)){
                return i;
            }
        }
        return -1;
    }

}
