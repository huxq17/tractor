package com.andbase.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andbase.demo.R;
import com.andbase.demo.bean.GamesBean;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 2144 on 2016/3/30.
 */
public class DownLoadListAdapter extends BaseRecyclerAdapter<DownLoadListAdapter.ListHolder> {
    private List<GamesBean> mList = new ArrayList<>();
    private Context mContext;

    public DownLoadListAdapter(Context context, List<GamesBean> list) {
        if (list != null) {
            mList = list;
        }
        mContext = context;
    }

    public void setData(List<GamesBean> list) {
        if (list != null) {
            mList = list;
        }
    }

    @Override
    public ListHolder getViewHolder(View view) {
        return new ListHolder(view, false);
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloadlist, parent, false);
        ListHolder listHolder = new ListHolder(view, true);
        return listHolder;
    }

    @Override
    public void onBindViewHolder(ListHolder holder, int position, boolean isItem) {
        GamesBean gamesBean = mList.get(position);
        if (gamesBean != null) {
            holder.pbProcess.setProgress(gamesBean.progress);
            holder.tvName.setText(gamesBean.name);
            holder.tvSpeed.setText(gamesBean.speed);
            holder.tvDownSize.setText(gamesBean.downloadSize);
            holder.btInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @Override
    public int getAdapterItemCount() {
        return mList.size();
    }

    public class ListHolder extends RecyclerView.ViewHolder {
        public Button btInstall;
        public TextView tvName, tvDownSize, tvSpeed;
        public ProgressBar pbProcess;

        public ListHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                btInstall = (Button) itemView.findViewById(R.id.bt_install);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvDownSize = (TextView) itemView.findViewById(R.id.tv_downsize);
                tvSpeed = (TextView) itemView.findViewById(R.id.tv_downspeed);
                pbProcess = (ProgressBar) itemView.findViewById(R.id.pb_process);
            }
        }
    }

}
