package com.wzb.smartcard.view;

import android.util.SparseArray;
import android.view.View;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date May 3, 2017 10:54:53 AM
 */
public class BaseViewHolder {
	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}
