package com.wwf.shrimp.application.client.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.viethoa.RecyclerViewFastScroller;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.util.List;

/**
 * Adapter for Tags list to choose from to attach to documents
 * @author AleaActaEst
 */

public class ShowTagListRecyclerViewAdapter extends RecyclerView.Adapter<ShowTagListRecyclerViewAdapter.ViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter {

    private List<TagItemDataHelper.TagDataCard> mDataArray;
    private SelectedTagListRecyclerViewAdapter selectedAdapter;
    private SessionData globalVariable;

    public void setSelectedAdapter(SelectedTagListRecyclerViewAdapter selectedAdapter) {
        this.selectedAdapter = selectedAdapter;
    }



    public ShowTagListRecyclerViewAdapter(List<TagItemDataHelper.TagDataCard> dataset, SessionData globalVariable) {
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
    public ShowTagListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.mTextView.setText(mDataArray.get(position).getTagText());
        holder.mCheckBox.setChecked(mDataArray.get(position).isCheckState());

        //
        // Click listeners
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox tagCheckBox  = null;
                ViewGroup row = (ViewGroup) v.getParent();
                for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
                    View view = row.getChildAt(itemPos);
                    if (view instanceof CheckBox) {
                        tagCheckBox = (CheckBox) view; //Found it!
                        break;
                    }
                }
                // set the item data in the array
                mDataArray.get(position).setCheckState(tagCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                TagItemSelectionDataHelper.TagDataCard item
                        = new TagItemSelectionDataHelper.TagDataCard(mDataArray.get(position).getId()
                                , mDataArray.get(position).getTagText()
                                , mDataArray.get(position).getCustomPrefix());
                item.setCustom(mDataArray.get(position).isCustom());


                if(tagCheckBox.isChecked()){
                    // remove the item
                    selectedAdapter.removeItem(item);
                    tagCheckBox.setChecked(false);

                }else{

                    // add a new item to the selected adapter list
                    selectedAdapter.addNewItem(item);
                    tagCheckBox.setChecked(true);
                }
                selectedAdapter.notifyDataSetChanged();

            }
        });
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox tagCheckBox = (CheckBox)v;
                /**Toast.makeText(v.getContext()
                        , "Checkbox Position clicked = " + position
                                + " Check Box status: " + tagCheckBox.isChecked()
                        , Toast.LENGTH_SHORT).show();
                 */
                // set the item data in the array
                mDataArray.get(position).setCheckState(tagCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                TagItemSelectionDataHelper.TagDataCard item
                        = new TagItemSelectionDataHelper.TagDataCard(mDataArray.get(position).getId()
                                    , mDataArray.get(position).getTagText()
                                    , mDataArray.get(position).getCustomPrefix());
                item.setCustom(mDataArray.get(position).isCustom());

                if(tagCheckBox.isChecked()){
                    // add a new item to the selected adapter list
                    selectedAdapter.addNewItem(item);
                }else{
                    // remove the item
                    selectedAdapter.removeItem(item);
                }
                selectedAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos < 0 || pos >= mDataArray.size())
            return null;

        String name = mDataArray.get(pos).getTagText();
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).getTagText().substring(0, 1);
    }

    public void resetItem(TagItemDataHelper.TagDataCard item){
        int index = mDataArray.indexOf(item);
        mDataArray.get(index).setCheckState(false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // @BindView(R.id.tv_alphabet)
        TextView mTextView;
        CheckBox mCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            // ButterKnife.bind(this, itemView);
            mTextView = (TextView)itemView.findViewById(R.id.tv_alphabet);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.checkBox_tag_item);
        }
    }
}

