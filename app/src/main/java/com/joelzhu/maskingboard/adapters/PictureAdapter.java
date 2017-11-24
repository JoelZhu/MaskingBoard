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
    // View类型
    private final int ItemAdd = 0;
    private final int ItemPicture = 1;

    // 宿主Activity
    private Activity activity;

    // 文件数组
    private List<Uri> uris;

    // Item宽高
    private int width, height;

    /**
     * 适配器构造函数
     *
     * @param activity 素质Activity
     * @param uris 文件数组
     */
    public PictureAdapter(Activity activity, List<Uri> uris) {
        this.activity = activity;
        this.uris = uris;

        // 如果数组第一个不为空，增加空元素
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
        // 添加按钮和图片两种布局
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
        // 如果是第一个元素，设置为添加按钮布局，其他元素为图片布局
        return position == 0 ? ItemAdd : ItemPicture;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case ItemAdd:
                // 添加
                return getViewItemAdd(position, convertView, parent);

            case ItemPicture:
                // 图片
                return getViewItemPicture(position, convertView, parent);
        }
        return convertView;
    }

    @Override
    public void onCameraClick() {
        // 跳转拍照页面
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        activity.startActivity(cameraIntent);
    }

    @Override
    public void onGalleryClick() {
        // 跳转系统相册
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(galleryIntent, JZConsts.GALLERY_REQUEST_CODE);
    }

    @Override
    public void notifyDataSetChanged() {
        // 判断第一个是否是空元素，如果不是，增加空元素
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

    /**
     * 构建添加按钮布局
     *
     * @param position 位置
     * @param convertView 控件View
     * @param parent 父布局
     * @return 控件View
     */
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

    /**
     * 构建图片布局
     *
     * @param position 位置
     * @param convertView 控件View
     * @param parent 父布局
     * @return 控件View
     */
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