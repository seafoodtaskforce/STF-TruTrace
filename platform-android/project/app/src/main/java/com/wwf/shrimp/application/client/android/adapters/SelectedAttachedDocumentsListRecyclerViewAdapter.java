package com.wwf.shrimp.application.client.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemSelectionDataHelper;

import java.util.List;

/**
 * Adapter for Backing documents that are attached to a target document
 *
 * @author AleaActaEst
 */

public class SelectedAttachedDocumentsListRecyclerViewAdapter extends RecyclerView.Adapter<SelectedAttachedDocumentsListRecyclerViewAdapter.ViewHolder> {

    private List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> mDataArray;

    public void setListAdapter(ShowAttachedDocumentsListRecyclerViewAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    private ShowAttachedDocumentsListRecyclerViewAdapter listAdapter;

    public SelectedAttachedDocumentsListRecyclerViewAdapter(List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> dataset) {
        mDataArray = dataset;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public SelectedAttachedDocumentsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_attachment_chosen_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataArray.get(position).getAttachedDocText());
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add or remove the item from the other adapter
                //
                AttachedDocumentItemDataHelper.AttachedDocumentDataCard item
                        = new AttachedDocumentItemDataHelper.AttachedDocumentDataCard(
                                mDataArray.get(position).getAttachedDocText(),
                                mDataArray.get(position).getOwner(),
                                true );
                item.setId(mDataArray.get(position).getId());
                item.setSyncId(mDataArray.get(position).getSyncId());

                listAdapter.resetItem(item);
                listAdapter.notifyDataSetChanged();

                // set the item data in the array
                mDataArray.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    public void addNewItem(AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard item){
        mDataArray.add(item);
    }

    public void removeItem(AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard item){
        mDataArray.remove(item);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        Button mCancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_chosenDocumentAttachment);
            mCancelButton = (Button)itemView.findViewById(R.id.buttonDeleteDocumentAttachment);
        }
    }

}
