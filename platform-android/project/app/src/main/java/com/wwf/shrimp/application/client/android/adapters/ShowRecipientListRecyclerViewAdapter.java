package com.wwf.shrimp.application.client.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.viethoa.RecyclerViewFastScroller;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientSelectionDataHelper;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.util.List;

/**
 * Adapter for Recipients documents that are attached to a target document
 * @author AleaActaEst
 */

public class ShowRecipientListRecyclerViewAdapter extends RecyclerView.Adapter<ShowRecipientListRecyclerViewAdapter.ViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter {

    private List<RecipientDataHelper.RecipientCard> mDataArray;
    private SelectedRecipientListRecyclerViewAdapter selectedAdapter;
    private SessionData globalVariable;

    public void setSelectedAdapter(SelectedRecipientListRecyclerViewAdapter selectedAdapter) {
        this.selectedAdapter = selectedAdapter;
    }



    public ShowRecipientListRecyclerViewAdapter(List<RecipientDataHelper.RecipientCard> dataset, SessionData globalVariable) {
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
    public ShowRecipientListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipient_list_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.mTextViewRecipientName.setText(mDataArray.get(position).getUserName());
        holder.mTextViewRecipientOrganization.setText(mDataArray.get(position).getOrganizationName());
        holder.mCheckBox.setChecked(mDataArray.get(position).isCheckState());

        //
        // click listeners
        holder.mTextViewRecipientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox recipientCheckBox  = null;
                ViewGroup row = (ViewGroup) v.getParent().getParent();
                for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
                    View view = row.getChildAt(itemPos);
                    if (view instanceof CheckBox) {
                        recipientCheckBox = (CheckBox) view; //Found it!
                        break;
                    }
                }
                // set the item data in the array
                mDataArray.get(position).setCheckState(recipientCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                RecipientSelectionDataHelper.RecipientCard item
                        = new RecipientSelectionDataHelper.RecipientCard(
                                mDataArray.get(position).getId(),
                                mDataArray.get(position).getOrganizationName(),
                                mDataArray.get(position).getUserName());


                if(recipientCheckBox.isChecked()){
                    // remove the item
                    selectedAdapter.removeItem(item);
                    recipientCheckBox.setChecked(false);

                }else{

                    // add a new item to the selected adapter list
                    selectedAdapter.addNewItem(item);
                    recipientCheckBox.setChecked(true);
                }
                selectedAdapter.notifyDataSetChanged();

            }
        });
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox recipientCheckBox = (CheckBox)v;
                /**Toast.makeText(v.getContext()
                        , "Checkbox Position clicked = " + position
                                + " Check Box status: " + recipientCheckBox.isChecked()
                        , Toast.LENGTH_SHORT).show();
                 */

                // set the item data in the array
                mDataArray.get(position).setCheckState(recipientCheckBox.isChecked());
                // add or remove the item from the other adapter
                //
                RecipientSelectionDataHelper.RecipientCard item
                        = new RecipientSelectionDataHelper.RecipientCard(
                                mDataArray.get(position).getId(),
                                mDataArray.get(position).getOrganizationName(),
                                mDataArray.get(position).getUserName()
                        );

                if(recipientCheckBox.isChecked()){
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

        String name = mDataArray.get(pos).getUserName();
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).getUserName().substring(0, 1);
    }

    public int getPositionByRecipientName(String recipientName){
        int result = -1;
        RecipientDataHelper.RecipientCard recipient = new RecipientDataHelper.RecipientCard(recipientName, "",true);

        result = mDataArray.indexOf(recipient);
        return result;
    }

    public void resetItem(RecipientDataHelper.RecipientCard item){
        int index = mDataArray.indexOf(item);
        mDataArray.get(index).setCheckState(false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // @BindView(R.id.tv_alphabet)
        TextView mTextViewRecipientName;
        TextView mTextViewRecipientOrganization;
        CheckBox mCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            // ButterKnife.bind(this, itemView);
            mTextViewRecipientName = (TextView)itemView.findViewById(R.id.textViewRecipientName);
            mTextViewRecipientOrganization = (TextView)itemView.findViewById(R.id.textViewRecipientOrganization);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.checkBox);
        }
    }
}

