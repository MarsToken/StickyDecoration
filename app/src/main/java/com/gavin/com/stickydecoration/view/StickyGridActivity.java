package com.gavin.com.stickydecoration.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gavin.com.library.StickyDecoration;
import com.gavin.com.library.listener.GroupListener;
import com.gavin.com.library.listener.OnGroupClickListener;
import com.gavin.com.stickydecoration.R;
import com.gavin.com.stickydecoration.model.City;
import com.gavin.com.stickydecoration.util.CityUtil;
import com.gavin.com.stickydecoration.util.DensityUtil;
import com.gavin.com.stickydecoration.view.widget.MyRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 文字悬浮
 */
public class StickyGridActivity extends AppCompatActivity {
    // TODO: gavin 2018/2/9 已知问题： notifyItemRemoved notifyItemRangeChanged时，界面渲染闪烁问题

    MyRecyclerView mRecyclerView;

    RecyclerView.Adapter mAdapter;
    List<City> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticky_recycler_view);
        initView();
    }

    private Map<Integer, Integer> map = new HashMap<>();
    private int index = 0;

    private void initView() {
        mRecyclerView = findViewById(R.id.rv);
        //模拟数据
        dataList.addAll(CityUtil.getCityList());
        dataList.addAll(CityUtil.getCityList());

        //------------- StickyDecoration 使用部分  ----------------
        for (int i = 1; i < dataList.size(); i++) {
            if (!dataList.get(i).getProvince().equals(dataList.get(i - 1).getProvince())) {
                int lastPos = i - 1;
                int realPos = lastPos + index;
                int weight = (3 - (realPos + 1) % 3) % 3;
                map.put(lastPos, weight);
                index += weight;
            } else {
                map.put(i - 1, 0);
            }
//            if (position > 0) { // pos=1
//                if (!dataList.get(position).getProvince().equals(dataList.get(position - 1).getProvince())) {
//                    map.put(position - 1, position % 3);
//                } else {
//                    map.put(position - 1, 0);
//                }
//            } else if (dataList.size() >= 2) {
//                if (!dataList.get(0).getProvince().equals(dataList.get(1).getProvince())) {
//                    map.put(0, 2);
//                } else {
//                    map.put(0, 0);
//                }
//            } else {
//                map.put(position, 0);
//            }
        }
        StickyDecoration decoration = StickyDecoration.Builder
                .init(new GroupListener() {
                    @Override
                    public String getGroupName(int position) {
                        //组名回调
                        if (dataList.size() > position && position > -1) {
                            //获取组名，用于判断是否是同一组
                            return dataList.get(position).getProvince();
                        }
                        return null;
                    }
                })
                .setGroupBackground(Color.parseColor("#48BDFF"))
                .setGroupHeight(DensityUtil.dip2px(this, 35))
                .setDivideColor(Color.parseColor("#EE96BC"))
                .setDivideHeight(DensityUtil.dip2px(this, 2))
                .setGroupTextColor(Color.BLACK)
                .setGroupTextSize(DensityUtil.sp2px(this, 15))
                .setTextSideMargin(DensityUtil.dip2px(this, 10))
                .setOnClickListener(new OnGroupClickListener() {
                    @Override
                    public void onClick(int position, int id) {                                 //Group点击事件
                        String content = "onGroupClick --> " + dataList.get(position).getProvince();
                        Toast.makeText(StickyGridActivity.this, content, Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        //------------- StickyDecoration 使用部分  ----------------
        //下面是平时的RecyclerView操作

        RecyclerView.LayoutManager manager;
        manager = new GridLayoutManager(this, 3);
        decoration.resetSpan(mRecyclerView, (GridLayoutManager) manager);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(decoration);

        mAdapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
                return new Holder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
                Holder holder = (Holder) viewHolder;
                holder.mTextView.setText(dataList.get(position).getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(StickyGridActivity.this, "item click " + dataList.get(position).getName(), Toast.LENGTH_LONG).show();
                    }
                });
                if (null != map.get(viewHolder.getAdapterPosition())) {
                    int weight = map.get(viewHolder.getAdapterPosition());
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.mViewSpace.getLayoutParams();
                    layoutParams.weight = weight;
                    System.out.println("position = " + position + ",weight=" + weight);
                    holder.mViewSpace.setLayoutParams(layoutParams);
                }
            }

            @Override
            public int getItemCount() {
                return dataList.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView mTextView;
        View mViewSpace;

        public Holder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv);
            mViewSpace = itemView.findViewById(R.id.space);
        }
    }

    // ---------  忽略下面的代码  --------------

    final int position = 3;

    public void onAdd(View v) {
        int previousSize = dataList.size();
        List<City> list = CityUtil.getCityList();
        dataList.addAll(list);
        mAdapter.notifyItemRangeInserted(previousSize, list.size());
        mAdapter.notifyItemRangeChanged(previousSize, list.size());
    }

    public void onDelete(View v) {
        dataList.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, dataList.size() - 3);

    }

    public void onDeleteLast(View v) {
        int endPosition = dataList.size() - 1;
        dataList.remove(endPosition);
        mAdapter.notifyItemRemoved(endPosition);
        mAdapter.notifyItemChanged(endPosition);
    }

    public void onRefresh(View v) {
        dataList.clear();
        dataList.addAll(CityUtil.getRandomCityList());
        mAdapter.notifyDataSetChanged();
    }

    public void onClean(View v) {
        dataList.clear();
        mAdapter.notifyDataSetChanged();
    }
}
