package com.jerry.nurse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.jerry.nurse.R;
import com.jerry.nurse.constant.ServiceConstant;
import com.jerry.nurse.model.GroupInfo;
import com.jerry.nurse.model.GroupListResult;
import com.jerry.nurse.net.FilterStringCallback;
import com.jerry.nurse.util.DensityUtil;
import com.jerry.nurse.util.L;
import com.jerry.nurse.util.ProgressDialogManager;
import com.jerry.nurse.util.SPUtil;
import com.jerry.nurse.view.RecycleViewDivider;
import com.jerry.nurse.view.TitleBar;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

import static com.jerry.nurse.constant.ServiceConstant.RESPONSE_SUCCESS;

public class GroupListActivity extends BaseActivity {

    @Bind(R.id.tb_contact_list)
    TitleBar mTitleBar;

    @Bind(R.id.rv_contact_list)
    RecyclerView mRecyclerView;


    private ProgressDialogManager mProgressDialogManager;

    private List<GroupInfo> mGroupInfos;

    private GroupAdapter mAdapter;

    private String mRegisterId;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, GroupListActivity.class);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_contact_list;
    }

    @Override
    public void init(Bundle savedInstanceState) {

        mProgressDialogManager = new ProgressDialogManager(this);
        mRegisterId = (String) SPUtil.get(this, SPUtil.REGISTER_ID, "");
        getGroupList(mRegisterId);
    }


    /**
     * 获取群信息
     *
     * @param registerId
     */
    private void getGroupList(String registerId) {
        mProgressDialogManager.show();
        OkHttpUtils.get().url(ServiceConstant.GET_GROUP_LIST)
                .addParams("RegisterId", registerId)
                .build()
                .execute(new FilterStringCallback(mProgressDialogManager) {

                    @Override
                    public void onFilterResponse(String response, int id) {
                        GroupListResult result = new Gson().fromJson(response, GroupListResult.class);
                        if (result.getCode() == RESPONSE_SUCCESS) {
                            mGroupInfos = result.getBody();
                            L.i("初始化读取到了" + mGroupInfos.size() + "个群");
                            if (mGroupInfos == null) {
                                mGroupInfos = new ArrayList();
                            }
                            setListData();
                            MainActivity.updateGroupInfoData(mGroupInfos);
                        }
                    }
                });
    }

    private void setListData() {
        // 设置间隔线
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this,
                LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(this, 0.5f),
                getResources().getColor(R.color.divider_line)));

        mAdapter = new GroupAdapter(this, R.layout.item_contact, mGroupInfos);
        mRecyclerView.setAdapter(mAdapter);

    }

    class GroupAdapter extends CommonAdapter<GroupInfo> {

        public GroupAdapter(Context context, int layoutId, List<GroupInfo> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, final GroupInfo groupInfo, int position) {
            holder.setImageResource(R.id.iv_avatar, R.drawable.icon_nurse_class);
            holder.setText(R.id.tv_nickname, groupInfo.getHXNickName());
            holder.getView(R.id.rl_contact).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ChatGroupActivity.getIntent(GroupListActivity.this, groupInfo.getHXGroupId());
                    startActivity(intent);
                }
            });
        }
    }
}
