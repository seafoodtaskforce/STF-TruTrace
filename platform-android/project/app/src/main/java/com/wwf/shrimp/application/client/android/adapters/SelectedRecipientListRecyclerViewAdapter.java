package com.wwf.shrimp.application.client.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientSelectionDataHelper;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.util.List;

/**
 * Adapter for Recipients (Users)  that are attached to a target document
 * @author AleaActaEst
 */

public class SelectedRecipientListRecyclerViewAdapter extends RecyclerView.Adapter<SelectedRecipientListRecyclerViewAdapter.ViewHolder> {

    private List<RecipientSelectionDataHelper.RecipientCard> mDataArray;
    private SessionData globalVariable;

    public void setListAdapter(ShowRecipientListRecyclerViewAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    private ShowRecipientListRecyclerViewAdapter listAdapter;

    public SelectedRecipientListRecyclerViewAdapter(List<RecipientSelectionDataHelper.RecipientCard> dataset, SessionData globalVariable) {
        mDataArray = dataset;
        this.globalVariable = globalVariable;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public SelectedRecipientListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipient_chosen_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText((mDataArray.get(position).getUserName()));
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add or remove the item from the other adapter
                //
                RecipientDataHelper.RecipientCard item
                        = new RecipientDataHelper.RecipientCard(mDataArray.get(position).getUserName(), mDataArray.get(position).getOrganizationName(),true );
                item.setId(mDataArray.get(position).getId());

                listAdapter.resetItem(item);
                listAdapter.notifyDataSetChanged();

                // set the item data in the array
                mDataArray.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    public void addNewItem(RecipientSelectionDataHelper.RecipientCard item){
        mDataArray.add(item);
    }

    public void removeItem(RecipientSelectionDataHelper.RecipientCard item){
        mDataArray.remove(item);
    }

    public void clear(){
        mDataArray.clear();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        Button mCancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_chosenRecipient);
            mCancelButton = (Button)itemView.findViewById(R.id.buttonDeleteRecipient);
        }
    }

}
