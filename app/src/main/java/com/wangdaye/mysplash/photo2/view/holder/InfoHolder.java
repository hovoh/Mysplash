package com.wangdaye.mysplash.photo2.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.data.entity.unsplash.Photo;
import com.wangdaye.mysplash.common.ui.adapter.PhotoInfoAdapter2;
import com.wangdaye.mysplash.common.ui.widget.NumberAnimTextView;
import com.wangdaye.mysplash.common.utils.helper.NotificationHelper;
import com.wangdaye.mysplash.photo2.view.activity.PhotoActivity2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Info holder.
 * */

public class InfoHolder extends PhotoInfoAdapter2.ViewHolder {

    @BindView(R.id.item_photo_2_info_container)
    LinearLayout container;

    @BindView(R.id.item_photo_2_info_views)
    NumberAnimTextView views;

    @BindView(R.id.item_photo_2_info_downloads)
    NumberAnimTextView downloads;

    @BindView(R.id.item_photo_2_info_likes)
    NumberAnimTextView likes;

    private PhotoActivity2 a;
    private boolean enableAnim;

    public static final int TYPE_INFO = 4;

    public InfoHolder(PhotoActivity2 a, View itemView, int marginHorizontal, int columnCount) {
        super(itemView, marginHorizontal, columnCount);
        ButterKnife.bind(this, itemView);

        views.setDuration(1000);
        downloads.setDuration(1000);
        likes.setDuration(1000);

        views.getPaint().setFakeBoldText(true);
        downloads.getPaint().setFakeBoldText(true);
        likes.getPaint().setFakeBoldText(true);

        this.a = a;
        this.enableAnim = false;

        if (marginHorizontal > 0 && columnCount == PhotoInfoAdapter2.COLUMN_COUNT_HORIZONTAL) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) container.getLayoutParams();
            params.setMarginStart(marginHorizontal);
            params.setMarginEnd(marginHorizontal);
            container.setLayoutParams(params);
        }
    }

    @Override
    protected void onBindView(PhotoActivity2 a, Photo photo) {
        views.setEnableAnim(enableAnim);
        downloads.setEnableAnim(enableAnim);
        likes.setEnableAnim(enableAnim);

        views.setNumberString(String.valueOf(photo.views));
        downloads.setNumberString(String.valueOf(photo.downloads));
        likes.setNumberString(String.valueOf(photo.likes));
    }

    public void setEnableAnim(boolean enable) {
        enableAnim = enable;
    }

    @Override
    protected void onRecycled() {
        // do nothing.
    }

    // interface.

    @OnClick(R.id.item_photo_2_info_viewsContainer)
    void clickViews() {
        NotificationHelper.showSnackbar(
                a.getString(R.string.feedback_views) + " : " + views.getText());
    }

    @OnClick(R.id.item_photo_2_info_downloadsContainer)
    void clickDownloads() {
        NotificationHelper.showSnackbar(
                a.getString(R.string.feedback_downloads) + " : " + downloads.getText());
    }

    @OnClick(R.id.item_photo_2_info_likesContainer)
    void clickLikes() {
        NotificationHelper.showSnackbar(
                a.getString(R.string.feedback_likes) + " : " + likes.getText());
    }
}
