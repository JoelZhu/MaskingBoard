package com.joelzhu.maskingboard.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.adapters.PictureAdapter;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 *
 * @author JoelZhu
 */
public class MainActivity extends BaseActivity {
    // 适配器
    private PictureAdapter adapter;

    // 图片文件数组
    private List<Uri> uris;

    @Override
    protected LayoutAttrs setLayoutAttributes() {
        return new LayoutAttrs.Builder()
                .layout(R.layout.activity_main)
                .title(R.string.app_name)
                .hasToolbar(true)
                .create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化数组
        uris = new ArrayList<>();

        // 配置GridView
        GridView gridView = (GridView) findViewById(R.id.main_gridView);
        adapter = new PictureAdapter(this, uris);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 更新文件数组
        uris.clear();
        File file = new File(JZFileUtils.getFileDir());
        for (File childFile : file.listFiles()) {
            uris.add(Uri.fromFile(childFile));
        }
        // 通知适配器数据修改
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case JZConsts.GALLERY_REQUEST_CODE:
                    // 相册
                    String filePath = JZFileUtils.getFilePathFromUri(MainActivity.this, data.getData());
                    Intent intent = new Intent(this, MaskingActivity.class);
                    intent.putExtra(JZConsts.ExtraPictureUri, filePath);
                    intent.putExtra(JZConsts.ExtraRotateDegree, 0);
                    startActivity(intent);
                    break;
            }
        }
    }
}