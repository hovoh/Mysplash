package com.wangdaye.mysplash.main.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.data.entity.unsplash.Collection;
import com.wangdaye.mysplash.common.data.entity.unsplash.Photo;
import com.wangdaye.mysplash.common.data.entity.unsplash.User;
import com.wangdaye.mysplash.common.i.model.CategoryModel;
import com.wangdaye.mysplash.common.i.model.LoadModel;
import com.wangdaye.mysplash.common.i.model.ScrollModel;
import com.wangdaye.mysplash.common.i.presenter.CategoryPresenter;
import com.wangdaye.mysplash.common.i.presenter.LoadPresenter;
import com.wangdaye.mysplash.common.i.presenter.ScrollPresenter;
import com.wangdaye.mysplash.common.ui.adapter.PhotoAdapter;
import com.wangdaye.mysplash.common.ui.adapter.multipleState.LargeErrorStateAdapter;
import com.wangdaye.mysplash.common.ui.adapter.multipleState.LargeLoadingStateAdapter;
import com.wangdaye.mysplash.common.ui.dialog.SelectCollectionDialog;
import com.wangdaye.mysplash.common.ui.widget.MultipleStateRecyclerView;
import com.wangdaye.mysplash.common.utils.AnimUtils;
import com.wangdaye.mysplash.common.utils.BackToTopUtils;
import com.wangdaye.mysplash.common.i.view.CategoryView;
import com.wangdaye.mysplash.common.i.view.LoadView;
import com.wangdaye.mysplash.common.i.view.ScrollView;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.main.model.widget.CategoryObject;
import com.wangdaye.mysplash.common.ui.widget.swipeRefreshView.BothWaySwipeRefreshLayout;
import com.wangdaye.mysplash.main.model.widget.LoadObject;
import com.wangdaye.mysplash.main.model.widget.ScrollObject;
import com.wangdaye.mysplash.main.presenter.widget.CategoryImplementor;
import com.wangdaye.mysplash.main.presenter.widget.LoadImplementor;
import com.wangdaye.mysplash.main.presenter.widget.ScrollImplementor;
import com.wangdaye.mysplash.main.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Category photos view.
 *
 * This view is used to show photos in
 * {@link com.wangdaye.mysplash.main.view.fragment.CategoryFragment}.
 *
 * */

public class CategoryPhotosView extends BothWaySwipeRefreshLayout
        implements CategoryView, LoadView, ScrollView,
        BothWaySwipeRefreshLayout.OnRefreshAndLoadListener, LargeErrorStateAdapter.OnRetryListener,
        SelectCollectionDialog.OnCollectionsChangedListener {

    @BindView(R.id.container_photo_list_recyclerView)
    MultipleStateRecyclerView recyclerView;

    private CategoryModel categoryModel;
    private CategoryPresenter categoryPresenter;

    private LoadModel loadModel;
    private LoadPresenter loadPresenter;

    private ScrollModel scrollModel;
    private ScrollPresenter scrollPresenter;

    private static class SavedState extends BaseSavedState {
        // data
        int category;
        String order;

        int page;
        List<Integer> pageList;

        boolean over;

        // life cycle.

        SavedState(CategoryPhotosView view, Parcelable superState) {
            super(superState);
            this.category = view.categoryModel.getPhotosCategory();
            this.order = view.categoryModel.getPhotosOrder();
            this.page = view.categoryModel.getPhotosPage();
            this.pageList = new ArrayList<>();
            this.pageList.addAll(view.categoryModel.getPageList());
            this.over = view.categoryModel.isOver();
        }

        private SavedState(Parcel in) {
            super(in);
            this.category = in.readInt();
            this.order = in.readString();
            this.page = in.readInt();

            this.pageList = new ArrayList<>();
            int[] pages = new int[in.readInt()];
            in.readIntArray(pages);
            pageList = new ArrayList<>(pages.length);
            for (int p : pages) {
                pageList.add(p);
            }

            this.over = in.readByte() != 0;
        }

        // interface.

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.category);
            out.writeString(this.order);
            out.writeInt(this.page);

            int[] pages = new int[pageList.size()];
            for (int i = 0; i < pages.length; i ++) {
                pages[i] = pageList.get(i);
            }
            out.writeInt(pages.length);
            out.writeIntArray(pages);

            out.writeByte(this.over ? (byte) 1 : (byte) 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public CategoryPhotosView(Context context) {
        super(context);
        this.initialize();
    }

    public CategoryPhotosView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    // init.

    @SuppressLint("InflateParams")
    private void initialize() {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_photo_list_2, null);
        addView(contentView);

        ButterKnife.bind(this, this);
        initModel();
        initPresenter();
        initView();
    }

    private void initModel() {
        this.categoryModel = new CategoryObject(
                getContext(),
                new PhotoAdapter(
                        getContext(),
                        new ArrayList<Photo>(Mysplash.DEFAULT_PER_PAGE),
                        this,
                        null));
        this.loadModel = new LoadObject(LoadModel.LOADING_STATE);
        this.scrollModel = new ScrollObject(true);
    }

    private void initPresenter() {
        this.categoryPresenter = new CategoryImplementor(categoryModel, this);
        this.loadPresenter = new LoadImplementor(loadModel, this);
        this.scrollPresenter = new ScrollImplementor(scrollModel, this);
    }

    private void initView() {
        setColorSchemeColors(ThemeManager.getContentColor(getContext()));
        setProgressBackgroundColorSchemeColor(ThemeManager.getRootColor(getContext()));
        setOnRefreshAndLoadListener(this);
        setPermitRefresh(false);
        setPermitLoad(false);

        int navigationBarHeight = DisplayUtils.getNavigationBarHeight(getResources());
        setDragTriggerDistance(
                BothWaySwipeRefreshLayout.DIRECTION_BOTTOM,
                navigationBarHeight + getResources().getDimensionPixelSize(R.dimen.normal_margin));

        int columnCount = DisplayUtils.getGirdColumnCount(getContext());
        recyclerView.setAdapter(categoryPresenter.getAdapter());
        if (columnCount > 1) {
            int margin = getResources().getDimensionPixelSize(R.dimen.normal_margin);
            recyclerView.setPadding(margin, margin, 0, 0);
        } else {
            recyclerView.setPadding(0, 0, 0, 0);
        }
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(
                new LargeLoadingStateAdapter(getContext(), 56),
                MultipleStateRecyclerView.STATE_LOADING);
        recyclerView.setAdapter(
                new LargeErrorStateAdapter(
                        getContext(), 56,
                        R.drawable.feedback_no_photos,
                        getContext().getString(R.string.feedback_load_failed_tv),
                        getContext().getString(R.string.feedback_click_retry),
                        this),
                MultipleStateRecyclerView.STATE_ERROR);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setState(MultipleStateRecyclerView.STATE_LOADING);

        categoryPresenter.getAdapter().setRecyclerView(recyclerView);
    }

    // save instance.

    @Override
    public Parcelable onSaveInstanceState() {
        return new SavedState(this, super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        categoryPresenter.setCategory(ss.category);
        categoryPresenter.setOrder(ss.order);
        categoryPresenter.setPage(ss.page);
        categoryPresenter.setPageList(ss.pageList);
        categoryPresenter.setOver(ss.over);
    }

    // control.

    /**
     * Set activity for the adapter in this view.
     *
     * @param a Container activity.
     * */
    public void setActivity(MainActivity a) {
        categoryPresenter.setActivityForAdapter(a);
        categoryPresenter.getAdapter().setOnDownloadPhotoListener(a);
        loadPresenter.bindActivity(a);
    }

    public List<Photo> loadMore(List<Photo> list, int headIndex, boolean headDirection) {
        if ((headDirection && categoryPresenter.getAdapter().getRealItemCount() < headIndex)
                || (!headDirection && categoryPresenter.getAdapter().getRealItemCount() < headIndex + list.size())) {
            return new ArrayList<>();
        }

        if (!headDirection && categoryPresenter.canLoadMore()) {
            categoryPresenter.loadMore(getContext(), false);
        }
        if (!recyclerView.canScrollVertically(1) && categoryPresenter.isLoading()) {
            setLoadingCategory(true);
        }

        if (headDirection) {
            if (headIndex == 0) {
                return new ArrayList<>();
            } else {
                return categoryPresenter.getAdapter().getPhotoData().subList(0, headIndex - 1);
            }
        } else {
            if (categoryPresenter.getAdapter().getRealItemCount() == headIndex + list.size()) {
                return new ArrayList<>();
            } else {
                return categoryPresenter.getAdapter()
                        .getPhotoData()
                        .subList(
                                headIndex + list.size(),
                                categoryPresenter.getAdapter().getRealItemCount() - 1);
            }
        }
    }

    public boolean isNormalState() {
        return loadPresenter.getLoadState() == LoadModel.NORMAL_STATE;
    }

    // photo.

    public void updatePhoto(Photo photo, boolean refreshView) {
        categoryPresenter.getAdapter().updatePhoto(recyclerView, photo, refreshView, false);
    }

    /**
     * Get the photos from the adapter in this view.
     *
     * @return Photos in adapter.
     * */
    public List<Photo> getPhotos() {
        return categoryPresenter.getAdapter().getPhotoData();
    }

    /**
     * Set photos to the adapter in this view.
     *
     * @param list Photos that will be set to the adapter.
     * */
    public void setPhotos(List<Photo> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        categoryPresenter.getAdapter().setPhotoData(list);
        if (list.size() == 0) {
            initRefresh();
        } else {
            loadPresenter.setNormalState();
        }
    }

    // query.

    public void setCategory(int id) {
        categoryPresenter.setCategory(id);
    }

    public String getOrder() {
        return categoryPresenter.getOrder();
    }

    public void setOrder(String order) {
        categoryPresenter.setOrder(order);
    }

    // HTTP request.

    public void initRefresh() {
        categoryPresenter.initRefresh(getContext());
    }

    public void cancelRequest() {
        categoryPresenter.cancelRequest();
    }

    // back to top.

    public boolean needPagerBackToTop() {
        return scrollPresenter.needBackToTop();
    }

    public void pagerScrollToTop() {
        scrollPresenter.scrollToTop();
    }

    // interface.

    // on refresh an load listener.

    @Override
    public void onRefresh() {
        categoryPresenter.refreshNew(getContext(), false);
    }

    @Override
    public void onLoad() {
        categoryPresenter.loadMore(getContext(), false);
    }

    // on retry listener.

    @Override
    public void onRetry() {
        categoryPresenter.initRefresh(getContext());
    }

    // on scroll listener.

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollPresenter.autoLoad(dy);
        }
    };

    // on collections changed listener.

    @Override
    public void onAddCollection(Collection c) {
        // do nothing.
    }

    @Override
    public void onUpdateCollection(Collection c, User u, Photo p) {
        categoryPresenter.getAdapter().updatePhoto(recyclerView, p, true, false);
    }

    // view.

    // category view.

    @Override
    public void setRefreshingCategory(boolean refreshing) {
        setRefreshing(refreshing);
    }

    @Override
    public void setLoadingCategory(boolean loading) {
        setLoading(loading);
    }

    @Override
    public void setPermitRefreshing(boolean permit) {
        setPermitRefresh(permit);
    }

    @Override
    public void setPermitLoading(boolean permit) {
        setPermitLoad(permit);
    }

    @Override
    public void initRefreshStart() {
        loadPresenter.setLoadingState();
    }

    @Override
    public void requestPhotosSuccess() {
        loadPresenter.setNormalState();
    }

    @Override
    public void requestPhotosFailed(String feedback) {
        if (categoryPresenter.getAdapter().getRealItemCount() > 0) {
            loadPresenter.setNormalState();
        } else {
            loadPresenter.setFailedState();
        }
    }

    // load view.

    @Override
    public void animShow(View v) {
        AnimUtils.animShow(v);
    }

    @Override
    public void animHide(final View v) {
        AnimUtils.animHide(v);
    }

    @Override
    public void setLoadingState(@Nullable MysplashActivity activity, int old) {
        if (activity != null) {
            DisplayUtils.setNavigationBarStyle(
                    activity, false, activity.hasTranslucentNavigationBar());
        }
        setPermitRefresh(false);
        setPermitLoad(false);
        recyclerView.setState(MultipleStateRecyclerView.STATE_LOADING);
    }

    @Override
    public void setFailedState(@Nullable MysplashActivity activity, int old) {
        setPermitRefresh(false);
        setPermitLoad(false);
        recyclerView.setState(MultipleStateRecyclerView.STATE_ERROR);
    }

    @Override
    public void setNormalState(@Nullable MysplashActivity activity, int old) {
        if (activity != null) {
            DisplayUtils.setNavigationBarStyle(
                    activity, true, activity.hasTranslucentNavigationBar());
        }
        setPermitRefresh(true);
        setPermitLoad(true);
        recyclerView.setState(MultipleStateRecyclerView.STATE_NORMALLY);
    }

    // scroll view.

    @Override
    public void scrollToTop() {
        BackToTopUtils.scrollToTop(recyclerView);
    }

    @Override
    public void autoLoad(int dy) {
        if (recyclerView.getLayoutManager() != null) {
            int[] lastVisibleItems = ((StaggeredGridLayoutManager) recyclerView.getLayoutManager())
                    .findLastVisibleItemPositions(null);
            int totalItemCount = categoryPresenter.getAdapter().getRealItemCount();
            if (categoryPresenter.canLoadMore()
                    && lastVisibleItems[lastVisibleItems.length - 1] >= totalItemCount - 10
                    && totalItemCount > 0
                    && dy > 0) {
                categoryPresenter.loadMore(getContext(), false);
            }
            if (!recyclerView.canScrollVertically(-1)) {
                scrollPresenter.setToTop(true);
            } else {
                scrollPresenter.setToTop(false);
            }
            if (!recyclerView.canScrollVertically(1) && categoryPresenter.isLoading()) {
                setLoading(true);
            }
        }
    }

    @Override
    public boolean needBackToTop() {
        return !scrollPresenter.isToTop()
                && loadPresenter.getLoadState() == LoadObject.NORMAL_STATE;
    }
}