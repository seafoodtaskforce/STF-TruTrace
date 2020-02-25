package com.wwf.shrimp.application.client.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.util.List;

/**
 * Adapter for Tags that that are attached to a target document
 *
 * @author AleaActaEst
 */

public class SelectedTagListRecyclerViewAdapter extends RecyclerView.Adapter<SelectedTagListRecyclerViewAdapter.ViewHolder> {

    private List<TagItemSelectionDataHelper.TagDataCard> mDataArray;
    private SessionData globalVariable;

    public void setListAdapter(ShowTagListRecyclerViewAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    private ShowTagListRecyclerViewAdapter listAdapter;

    public SelectedTagListRecyclerViewAdapter(List<TagItemSelectionDataHelper.TagDataCard> dataset, SessionData globalVariable) {
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
    public SelectedTagListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_chosen_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText((mDataArray.get(position).getTagText()));
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add or remove the item from the other adapter
                //
                TagItemDataHelper.TagDataCard item
                        = new TagItemDataHelper.TagDataCard(mDataArray.get(position).getTagText(), mDataArray.get(position).getCustomPrefix(), true );
                item.setId(mDataArray.get(position).getId());

                listAdapter.resetItem(item);
                listAdapter.notifyDataSetChanged();

                // set the item data in the array
                mDataArray.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    public void addNewItem(TagItemSelectionDataHelper.TagDataCard item){
        mDataArray.add(item);
    }

    public void removeItem(TagItemSelectionDataHelper.TagDataCard item){
        mDataArray.remove(item);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        Button mCancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_chosenTag);
            mCancelButton = (Button)itemView.findViewById(R.id.buttonDeleteTag);
        }
    }

}
