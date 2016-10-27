package com.openthos.filemanager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.openthos.filemanager.R;
import com.openthos.filemanager.component.MyImageView;
import com.openthos.filemanager.component.NativeImageLoader;
import com.openthos.filemanager.utils.L;

public class ChildAdapter extends BaseAdapter {
    private Point mPoint = new Point(0, 0);
//    private HashMap<Integer, Boolean> mSelectMap = new HashMap<>();
    private GridView mGridView;
    private List<String> childPathList;
    protected LayoutInflater mInflater;

    public ChildAdapter(Context context, List<String> childPathList, GridView mGridView) {
        this.childPathList = childPathList;
        this.mGridView = mGridView;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return childPathList.size();
    }

    @Override
    public Object getItem(int position) {
        return childPathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        String path = childPathList.get(position);
        L.e("path",path);
        String iconName = path.substring(path.lastIndexOf("/")+1);
        L.e("iconName",iconName);
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.grid_child_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.child_image);
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.tv_icon_name);

            viewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {
                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                }
            });

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mImageView.setImageResource(R.mipmap.pictures_no);
        }
        viewHolder.mImageView.setTag(path);

        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint,
                        new NativeImageLoader.NativeImageCallBack() {
                            @Override
                            public void onImageLoader(Bitmap bitmap, String path) {
                                ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
                                if(bitmap != null && mImageView != null){
                                    mImageView.setImageBitmap(bitmap);
                                }
                            }
                        }
        );

        if(bitmap != null){
            viewHolder.mImageView.setImageBitmap(bitmap);
        }else{
            viewHolder.mImageView.setImageResource(R.mipmap.pictures_no);
        }
        viewHolder.mTextView.setText(iconName);
        return convertView;
    }

//    private void addAnimation(View view){
//        float [] vaules = new float[]
//        {0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
//        AnimatorSet set = new AnimatorSet();
//        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
//                ObjectAnimator.ofFloat(view, "scaleY", vaules));
//        set.setDuration(150);
//        set.start();
//    }

//    public List<Integer> getSelectItems(){
//        List<Integer> childPathList = new ArrayList<Integer>();
//        for(Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet().iterator();
//            it.hasNext();){
//            Map.Entry<Integer, Boolean> entry = it.next();
//            if(entry.getValue()){
//                childPathList.add(entry.getKey());
//            }
//        }
//
//        return childPathList;
//    }

    class ViewHolder{
        public MyImageView mImageView;
        public TextView mTextView;
    }
}
