package com.joelzhu.maskingboard.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.activities.CameraActivity;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.views.JZAddButton;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends BaseAdapter implements JZAddButton.OnButtonClickListener {
    private final int ItemAdd = 0;
    private final int ItemPicture = 1;

    private Activity activity;
    private List<Uri> uris;

    // Item宽度
    private int width;
    // Item高度
    private int height;

    public PictureAdapter(Activity activity, List<Uri> uris) {
        this.activity = activity;
        this.uris = uris;

        if (uris.size() == 0 || uris.get(0) != Uri.EMPTY)
            this.uris.add(0, Uri.EMPTY);

        // 根据屏幕大小计算item的长宽
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            width = windowManager.getDefaultDisplay().getWidth() / 3;
            height = (int) (width * 1.5);
        }

    }

    @Override
    public int getCount() {
        return uris.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return uris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ItemAdd : ItemPicture;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case ItemAdd:
                return getViewItemAdd(position, convertView, parent);

            case ItemPicture:
                return getViewItemPicture(position, convertView, parent);
        }
        return convertView;
    }

    @Override
    public void onCameraClick() {
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        activity.startActivity(cameraIntent);
    }

    @Override
    public void onGalleryClick() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(galleryIntent, JZConsts.GALLERY_REQUEST_CODE);
    }

    @Override
    public void notifyDataSetChanged() {
        if (uris.get(0) != Uri.EMPTY)
            uris.add(0, Uri.EMPTY);

        super.notifyDataSetChanged();
    }

    private class PictureViewHolder {
        ImageView imageView;
    }

    private class AddViewHolder {
        JZAddButton addView;
    }

    private View getViewItemAdd(int position, View convertView, ViewGroup parent) {
        AddViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_add, parent, false);

            viewHolder = new AddViewHolder();
            viewHolder.addView = (JZAddButton) convertView.findViewById(R.id.itemAdd_addButton);

            convertView.setLayoutParams(new GridView.LayoutParams(width, height));

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AddViewHolder) convertView.getTag();
        }

        viewHolder.addView.setOnButtonClickListener(this);

        return convertView;
    }

    private View getViewItemPicture(int position, View convertView, ViewGroup parent) {
        PictureViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_picture, parent, false);

            viewHolder = new PictureViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.itemPicture_imageView);

            convertView.setLayoutParams(new GridView.LayoutParams(width, height));

            convertView.setTag(R.id.image_tag, viewHolder);
        } else {
            viewHolder = (PictureViewHolder) convertView.getTag(R.id.image_tag);
        }

        Glide.with(activity).load(uris.get(position)).into(viewHolder.imageView);

        return convertView;
    }
}