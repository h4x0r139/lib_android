package cn.yinxm.lib.view.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cn.yinxm.lib.utils.log.LogUtil;

/**
 * description : 用于{@RecyclerView}实现列表展示功能， <b>注意:不需要继承此类，直接使用即可<b/>
 * 如果列表中的item只有一种布局，通过{@link RecyclerAdapter#itemLayoutId(int)}传入布局文件资源ID即可
 * 然后再通过{@link RecyclerAdapter#viewHolderFactory(BaseViewHolderFactory)}方法传入{@code ViewHolder}
 * 的工厂类即可。
 * 同时也支持多布局类型列表，通过调用{@link RecyclerAdapter#multiTypeSupport(MultiTypeSupport)}方法
 * 传入{@link MultiTypeSupport}的实现类即可
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecyclerAdapter<T, VH extends BaseViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private final ArrayList<T> mItems;
    private OnItemClickListener<T> mOnItemClickListener;
    private final LayoutInflater mInflater;
    private int mItemLayoutId;
    private BaseViewHolderFactory<VH> mFactory;
    private MultiTypeSupport<T> mMultiTypeSupport;

    public RecyclerAdapter(Context context) {
        mItems = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    public RecyclerAdapter<T, VH> itemLayoutId(int itemLayoutId) {
        mItemLayoutId = itemLayoutId;
        return this;
    }

    public RecyclerAdapter<T, VH> viewHolderFactory(BaseViewHolderFactory<VH> factory) {
        mFactory = factory;
        return this;
    }

    public RecyclerAdapter<T, VH> multiTypeSupport(MultiTypeSupport<T> support) {
        mMultiTypeSupport = support;
        return this;
    }

    public void update(List<T> list) {
        mItems.clear();
        if (list != null) {
            mItems.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void add(T item) {
        add(mItems.size(), item);
    }

    public void add(int index, T item) {
        if (item == null) {
            return;
        }
        mItems.add(index, item);
        notifyItemInserted(index);
    }

    public void addAll(List<T> list) {
        addAll(mItems.size(), list);
    }

    public void addAll(int start, List<T> items) {
        if (items == null) {
            return;
        }

        mItems.addAll(start, items);
        notifyItemRangeInserted(start, items.size());
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void remove(int position, int count) {
        if (position < 0 || position >= mItems.size()) {
            return;
        }

        ListIterator<T> iterator = mItems.listIterator(position);
        int countToRemove = count;
        while (iterator.hasNext() && countToRemove > 0) {
            iterator.next();
            iterator.remove();
            countToRemove--;
        }

        notifyItemRangeRemoved(position, count);
    }

    public void moveItem(int from, int to) {
        Util.move(mItems, from, to);
        notifyItemMoved(from, to);
    }

    @Override
    public int getItemViewType(int position) {
        if (mMultiTypeSupport != null) {
            return mMultiTypeSupport.getItemViewType(mItems, position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public VH onCreateViewHolder(final ViewGroup parent, int viewType) {
        int layoutId;
        if (mMultiTypeSupport != null) {
            layoutId = mMultiTypeSupport.getLayoutId(viewType);
        } else {
            layoutId = mItemLayoutId;
        }
        View itemView = mInflater.inflate(layoutId, parent, false);
        final VH viewHolder = mFactory.createViewHolder(itemView, viewType);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    try {
                        int position = viewHolder.getAdapterPosition();
                        if (position < 0 || mItems.size() == 0 || position >= mItems.size()) {
                            Log.e(LogUtil.TAG, "item click error p=" + position + ", s=" + mItems.size());
                            return;
                        }
                        mOnItemClickListener.onItemClick(parent, position, mItems.get(position));
                    } catch (Exception e) {
                        LogUtil.e(e);
                    }
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.onRefreshItemView(payloads);
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        T t = mItems.get(position);
        holder.onBindItemView(t, position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public RecyclerAdapter<T, VH> itemClickListener(OnItemClickListener<T> listener) {
        mOnItemClickListener = listener;
        return this;
    }
}
