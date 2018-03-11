package com.example.newbies.bluetoothdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 *
 * @author NewBies
 * @date 2018/3/10
 */
public class ServiceAdapter extends BaseExpandableListAdapter{

    private Context context;
    private List<String> group;
    private List<List<String>> child;
    private OnItemClikCallBack onItemClikCallBack;

    public ServiceAdapter(Context context, List<String> group, List<List<String>> child){
        this.context = context;
        this.group = group;
        this.child = child;
    }

    /**
     * 组件点击的回调接口
     */
    public interface OnItemClikCallBack{

        void onGroupItemClick(int groupPosition);

        void onChildItemClick(int groupPosition, int childPosition);
    }

    public void setOnItemClikCallBack(OnItemClikCallBack onItemClikCallBack){
        this.onItemClikCallBack = onItemClikCallBack;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    /**
     * 获取父项的数量
     * @param groupPosition
     * @return
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return child.get(groupPosition).size();
    }

    /**
     * 获得某个父项
     * @param groupPosition
     * @return
     */
    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    /**
     * 获取某个子项
     * @param groupPosition
     * @param childPosition
     * @return
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(groupPosition).get(childPosition);
    }

    /**
     * 获得某个父项的id
     * @param groupPosition
     * @return
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * 获得某个父项的某个子项的id
     * @param groupPosition
     * @param childPosition
     * @return
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * 按函数的名字来理解应该是是否具有稳定的id，这个方法目前一直都是返回false，没有去改动过
     * @return
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * 获得父项显示的view
     * @param groupPosition
     * @param isExpanded
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.service_item, null);
        }
        convertView.setTag(R.layout.service_item, groupPosition);
        convertView.setTag(R.layout.child_item, -1);
        TextView text = convertView.findViewById(R.id.service);
        text.setText(group.get(groupPosition));
//        text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClikCallBack.onGroupItemClick(groupPosition);
//            }
//        });
        return convertView;
    }

    /**
     * 获得子项显示的view
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.child_item, null);
        }
        convertView.setTag(R.layout.service_item, groupPosition);
        convertView.setTag(R.layout.child_item, childPosition);
        TextView text = convertView.findViewById(R.id.child);
        text.setText(child.get(groupPosition).get(childPosition));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClikCallBack.onChildItemClick(groupPosition,childPosition);
            }
        });
        return convertView;
    }

    /**
     * 子项是否可选中，如果需要设置子项的点击事件，需要返回true
     * @param groupPosition
     * @param childPosition
     * @return
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
