package com.wwf.shrimp.application.client.android.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.viethoa.RecyclerViewFastScroller;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.utils.DateUtils;

import java.util.List;

/**
 * Adapter for Backing documents that are attached to a target document
 * @author AleaActaEst
 */

public class ShowAttachedDocumentsListRecyclerViewAdapter extends RecyclerView.Adapter<ShowAttachedDocumentsListRecyclerViewAdapter.ViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter {

    private List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> mDataArray;
    private SelectedAttachedDocumentsListRecyclerViewAdapter selectedAdapter;
    private Context mContext;

    public void setSelectedAdapter(SelectedAttachedDocumentsListRecyclerViewAdapter selectedAdapter) {
        this.selectedAdapter = selectedAdapter;
    }

    public List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> getDataSet(){
        return mDataArray;
    }

    public ShowAttachedDocumentsListRecyclerViewAdapter(List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> dataset, Context context) {
        mDataArray = dataset;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public ShowAttachedDocumentsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_attachment_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataArray.get(position).getAttachedDocText());
        holder.mTextViewOwner.setText(mDataArray.get(position).getOwner());
        holder.mTimestamp.setText(DateUtils.formatFullDateStringToSimpleDateTimeString(mDataArray.get(position).getTimestamp()));
        holder.mCheckBox.setChecked(mDataArray.get(position).isCheckState());
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox attachmentCheckBox = (CheckBox)v;
                // set the item data in the array
                mDataArray.get(position).setCheckState(attachmentCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard item
                        = new AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard(
                                mDataArray.get(position).getId(),
                                mDataArray.get(position).getAttachedDocText(),
                                mDataArray.get(position).getOwner(),
                        mDataArray.get(position).getDocumentType());
                item.setSyncId(mDataArray.get(position).getSyncId());

                if(attachmentCheckBox.isChecked()){
                    // add a new item to the selected adapter list
                    selectedAdapter.addNewItem(item);
                }else{
                    // remove the item
                    selectedAdapter.removeItem(item);
                }
                selectedAdapter.notifyDataSetChanged();
            }
        });

        //long colorIndex = mDataArray.get(position).getDocumentType().getId();
        //int colorId = MappingUtilities.documentTypeIDToRColorMap.get(colorIndex);
        // ContextCompat.getColor(context, colorId);
        // String color = context.getResources().getString(MappingUtilities.documentTypeIDToRColorMap.get(colorIndex));
        //holder.viewColoredBar.setBackgroundColor(ContextCompat.getColor(mContext, colorId));
        holder.viewColoredBar.setBackgroundColor(Color.parseColor(mDataArray.get(position).getDocumentType().getHexColorCode()));
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos < 0 || pos >= mDataArray.size())
            return null;

        String name = mDataArray.get(pos).getAttachedDocText();
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).getAttachedDocText().substring(0, 1);
    }


    public void resetItem(AttachedDocumentItemDataHelper.AttachedDocumentDataCard item){
        int index = mDataArray.indexOf(item);
        if(index == -1){
            return;
        }
        mDataArray.get(index).setCheckState(false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        TextView mTextViewOwner;
        TextView mTimestamp;
        CheckBox mCheckBox;
        View viewColoredBar;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_alphabet);
            mTextViewOwner = (TextView)itemView.findViewById(R.id.tv_owner);
            mTimestamp = (TextView)itemView.findViewById(R.id.tv_timestamp);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.checkBox);
            viewColoredBar = itemView.findViewById(R.id.colored_bar);


        }
    }

}

