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
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;

import java.util.List;

/**
 * Adapter for Linking documents that are attached to a target document
 * @author AleaActaEst
 */

public class ShowLinkedDocumentsListRecyclerViewAdapter extends RecyclerView.Adapter<ShowLinkedDocumentsListRecyclerViewAdapter.ViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter {

    private List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> mDataArray;
    private SelectedLinkedDocumentsListRecyclerViewAdapter selectedAdapter;
    private Context mContext;
    private int selectedPos = RecyclerView.NO_POSITION;
    // global session data access
    private SessionData globalVariable = null;

    public void setSelectedAdapter(SelectedLinkedDocumentsListRecyclerViewAdapter selectedAdapter) {
        this.selectedAdapter = selectedAdapter;
    }

    public List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> getDataSet(){
        return mDataArray;
    }



    public ShowLinkedDocumentsListRecyclerViewAdapter(List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> dataset, Context context) {
        mDataArray = dataset;
        mContext = context;
        this.globalVariable = (SessionData) context.getApplicationContext();
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public ShowLinkedDocumentsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_link_list_recycler_view_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setSelected(selectedPos == position);
        holder.mTextView.setText(mDataArray.get(position).getLinkedDocText());
        holder.mTextViewOwner.setText(mDataArray.get(position).getOwner());
        holder.mTimestamp.setText(DateUtils.formatFullDateStringToSimpleDateTimeString(mDataArray.get(position).getTimestamp()));
        holder.mCheckBox.setChecked(mDataArray.get(position).isCheckState());
        holder.mTextViewCustomTag.setText(mDataArray.get(position).getCustomTag());
        holder.mCheckBox.setChecked(mDataArray.get(position).isCheckState());


        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox linkCheckBox = (CheckBox)v;
                /**Toast.makeText(v.getContext()
                        , "Checkbox Position clicked = " + position
                                + " Check Box status: " + linkCheckBox.isChecked()
                        , Toast.LENGTH_SHORT).show();
                */

                // set the item data in the array
                mDataArray.get(position).setCheckState(linkCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard item
                        = new LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard(
                                mDataArray.get(position).getId(),
                                mDataArray.get(position).getLinkedDocText(),
                                mDataArray.get(position).getOwner(),
                                mDataArray.get(position).getDocumentType(),
                                mDataArray.get(position).getDynamicFieldData()
                );


                item.setSyncId(mDataArray.get(position).getSyncId());

                if(linkCheckBox.isChecked()){
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

        //
        // Custom tag
        String formattedDocField = DocumentPOJOUtils.getFormattedDocFields(mDataArray.get(position).getDynamicFieldData(), globalVariable);
        if(formattedDocField != null && formattedDocField.length() > 0){
        //if(mDataArray.get(position).getCustomTag() != null && mDataArray.get(position).getCustomTag().length() > 0){
            holder.mTextViewCustomTag.setVisibility(View.VISIBLE);
            // show the data
            holder.mTextViewCustomTag.setText(formattedDocField);

        }else{
            holder.mTextViewCustomTag.setVisibility(View.GONE);
        }


    }



    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos < 0 || pos >= mDataArray.size())
            return null;

        String name = mDataArray.get(pos).getLinkedDocText();
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).getLinkedDocText().substring(0, 1);
    }


    public void resetItem(LinkedDocumentItemDataHelper.LinkedDocumentDataCard item){
        int index = mDataArray.indexOf(item);
        if(index == -1){
            return;
        }
        mDataArray.get(index).setCheckState(false);
    }

    public void setItem(LinkedDocumentItemDataHelper.LinkedDocumentDataCard item){
        int index = mDataArray.indexOf(item);
        if(index == -1){
            return;
        }
        mDataArray.get(index).setCheckState(true);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        TextView mTextViewOwner;
        TextView mTimestamp;
        CheckBox mCheckBox;
        TextView mTextViewCustomTag;
        View viewColoredBar;

        public ViewHolder(View itemView) {
            super(itemView);
            // ButterKnife.bind(this, itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_alphabet);
            mTextViewOwner = (TextView)itemView.findViewById(R.id.tv_owner);
            mTimestamp = (TextView)itemView.findViewById(R.id.tv_timestamp);
            mTextViewCustomTag = (TextView)itemView.findViewById(R.id.tv_custom_tag);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.checkBox);
            viewColoredBar = itemView.findViewById(R.id.colored_bar);


        }
    }

}

