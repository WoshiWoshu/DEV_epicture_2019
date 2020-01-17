package com.example.mainactivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ActionMenuView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.databinding.DataBindingUtil;

import com.example.mainactivity.databinding.FragmentNotificationsBinding;
import com.example.mainactivity.databinding.ItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.*;


public class ListAdapter extends BaseAdapter implements Filterable {

    private List<Photo> mData;
    private List<Photo> mPhotoFilterList;
    private ValueFilter valueFilter;
    private LayoutInflater inflater;

    public ListAdapter(List<Photo> cancel_type)
    {
        mData = cancel_type;
        mPhotoFilterList  = cancel_type;
    }

    @Override
    public int getCount()
    {
        return mData.size();
    }

    @Override
    public Photo getItem(int position)
    {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public Filter getFilter()
    {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();
            List<Photo> filterList = new ArrayList<>();

            if (constraint != null && constraint.length() > 0) {
                for (int i = 0; i < mPhotoFilterList.size(); i++) {
                    if ((mPhotoFilterList.get(i).title.toUpperCase()).
                            contains(constraint.toString().toUpperCase())) {
                        filterList.add(mPhotoFilterList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mPhotoFilterList.size();
                results.values = mPhotoFilterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            mData = (List<Photo>) results.values;
            notifyDataSetChanged();
        }

    }

    public static abstract class TheCompany
            extends Activity implements ActionMenuView.OnMenuItemClickListener {

        public static float screen_width = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.item);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            screen_width = metrics.widthPixels;
        }

    }

    boolean isImageFitToScreen;


    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        if (inflater == null) {
            inflater = (LayoutInflater)parent.getContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        final ItemBinding opItemBinding =
                DataBindingUtil.inflate(inflater, R.layout.item, parent, false);
        opItemBinding.title.setText(mData.get(position).title);
        opItemBinding.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    opItemBinding.photo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    opItemBinding.photo.setAdjustViewBounds(true);
                } else {
                    isImageFitToScreen=true;
                    opItemBinding.photo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    opItemBinding.photo.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });
        Picasso.with(parent.getContext())
                .load("https://i.imgur.com/" + mData.get(position).id + ".jpg")
                .into(opItemBinding.photo);
        return opItemBinding.getRoot();
    }

}
