package com.jerry.nurse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jerry.nurse.R;
import com.jerry.nurse.model.Announcement;
import com.jerry.nurse.model.LoginInfo;
import com.jerry.nurse.util.CommonAdapter;
import com.jerry.nurse.util.RecyclerViewDecorationUtil;
import com.jerry.nurse.util.ViewHolder;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class AnnouncementActivity extends BaseActivity {

    @Bind(R.id.rv_announcement)
    RecyclerView mRecyclerView;

    private AnnouncementAdapter mAdapter;

    private List<Announcement> mAnnouncements;

    private LoginInfo mLoginInfo;

    private int mCurrentPage = 1;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, AnnouncementActivity.class);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_announcement;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mLoginInfo = DataSupport.findFirst(LoginInfo.class);
        // 设置间隔线
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewDecorationUtil.addItemDecoration(this, mRecyclerView);
        mAnnouncements = new ArrayList<>();
        mAdapter = new AnnouncementAdapter(this, R.layout.item_announcement, mAnnouncements);
        mRecyclerView.setAdapter(mAdapter);
        // 设置可以下拉加载更多
//        mRecyclerView.setLoadingMoreEnabled(false);
//        // 列表加载的监听
//        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
//            /**
//             * 下拉刷新
//             */
//            @Override
//            public void onRefresh() {
//                L.i("下拉刷新");
//                getAnnouncement(++mCurrentPage, mLoginInfo.getHospitalId(), mLoginInfo.getDepartmentId());
//            }
//
//            /**
//             * 加载更多
//             */
//            @Override
//            public void onLoadMore() {
//                L.i("加载更多");
//
//            }
//        });

        // 获取公告数据
        getAnnouncement(mCurrentPage, mLoginInfo.getHospitalId(), mLoginInfo.getDepartmentId());
    }

    /**
     * 获取公告列表咨询
     *
     * @param page
     * @param hospitalId
     * @param officeId
     */
    private void getAnnouncement(int page, String hospitalId, String officeId) {
//        // 检测院内权限
//        if (!OfficeFragment.checkPermission()) {
//            hospitalId = "";
//            officeId = "";
//        } else {
//            if (hospitalId == null) {
//                hospitalId = "";
//            }
//            if (officeId == null) {
//                officeId = "";
//            }
//        }
//
//        OkHttpUtils.get().url(ServiceConstant.GET_ANNOUNCEMENT)
//                .addParams("pageNumber", String.valueOf(page))
//                .addParams("HospitalId", hospitalId)
//                .addParams("DepartmentId", officeId)
//                .build()
//                .execute(new FilterStringCallback() {
//
//                    @Override
//                    protected void onFilterError(Call call, Exception e, int id) {
//                        mRecyclerView.refreshComplete();
//                        //从数据库中获取数据
//                        mAnnouncements = DataSupport.findAll(Announcement.class);
//                        if (mAnnouncements != null) {
//                            setAnnouncements();
//                        }
//                    }
//
//                    @Override
//                    public void onFilterResponse(String response, int id) {
//                        mRecyclerView.refreshComplete();
//                        AnnouncementsResult result = new Gson().fromJson(response, AnnouncementsResult.class);
//                        if (result.getCode() == RESPONSE_SUCCESS) {
//                            List<Announcement> announcements = result.getBody();
//                            if (announcements == null || announcements
//                                    .size() == 0) {
//                                announcements = new ArrayList<>();
//                                T.showShort(AnnouncementActivity.this, "没有更多数据了");
//                            }
//                            // 添加的公告集合中
//                            updateAnnouncements(announcements);
//                            if (mAnnouncements.size() > 0) {
//                                //添加新数据到数据库
//                                DataSupport.deleteAll(Announcement.class);
//                                DataSupport.saveAll(result.getBody());
//                            }
//                        } else {
//                            T.showShort(AnnouncementActivity.this, result.getMsg());
//                        }
//                    }
//                });
        mAnnouncements = DataSupport.findAll(Announcement.class);
        if (mAnnouncements != null) {
            setAnnouncements();
        }
    }

    /**
     * 加载失败时，读取本地数据
     */
    private void setAnnouncements() {
        mAdapter.setDatas(mAnnouncements);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 加载成功后，添加数据并更新显示
     *
     * @param announcements
     */
    private void updateAnnouncements(List<Announcement> announcements) {
        mAnnouncements.addAll(announcements);
        mAdapter.setDatas(mAnnouncements);
        mAdapter.notifyDataSetChanged();
    }

    class AnnouncementAdapter extends CommonAdapter<Announcement> {

        public AnnouncementAdapter(Context context, int layoutId, List<Announcement> datas) {
            super(context, layoutId, datas);
        }

        @Override
        public void convert(ViewHolder holder, final Announcement announcement) {
            holder.setText(R.id.tv_title, announcement.getTitle());
            holder.setText(R.id.tv_institution, announcement.getAgency());
            holder.setText(R.id.tv_time, announcement.getNoticeTime());
            holder.getView(R.id.rl_announcement).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击跳转详情
                    Intent intent = AnnouncementDetailActivity.getIntent(AnnouncementActivity.this, announcement);
                    startActivity(intent);
                }
            });
        }
    }
}
