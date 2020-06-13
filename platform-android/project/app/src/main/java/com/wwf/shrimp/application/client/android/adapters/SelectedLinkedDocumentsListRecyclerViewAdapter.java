package com.wwf.shrimp.application.client.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemSelectionDataHelper;

import java.util.List;

/**
 * Adapter for Linked documents that are attached to a target document
 *
 * @author AleaActaEst
 */

public class SelectedLinkedDocumentsListRecyclerViewAdapter extends RecyclerView.Adapter<SelectedLinkedDocumentsListRecyclerViewAdapter.ViewHolder> {

    private List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> mDataArray;

    public void setListAdapter(ShowLinkedDocumentsListRecyclerViewAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    private ShowLinkedDocumentsListRecyclerViewAdapter listAdapter;

    public SelectedLinkedDocumentsListRecyclerViewAdapter(List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> dataset) {
        mDataArray = dataset;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public SelectedLinkedDocumentsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_link_chosen_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataArray.get(position).getLinkedDocText());
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add or remove the item from the other adapter
                //
                LinkedDocumentItemDataHelper.LinkedDocumentDataCard item
                        = new LinkedDocumentItemDataHelper.LinkedDocumentDataCard(
                                mDataArray.get(position).getLinkedDocText(),
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

    public void addNewItem(LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard item){
        mDataArray.add(item);
    }

    public void removeItem(LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard item){
        mDataArray.remove(item);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        Button mCancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_chosenDocumentLink);
            mCancelButton = (Button)itemView.findViewById(R.id.buttonDeleteDocumentLink);
        }
    }

}
