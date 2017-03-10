package com.openthos.filemanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import com.openthos.filemanager.system.FileCategoryHelper;
import com.openthos.filemanager.system.FileIconHelper;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class IconHolder {

    private static final int MAX_CACHE = 500;

    private static final int MSG_LOAD = 1;
    private static final int MSG_LOADED = 2;

    private final Map<String, Drawable> mAppIcons;
    private final WeakHashMap<ImageView, Loadable> mRequests;
    private final Context mContext;
    private boolean mNeedAlbumUpdate = true;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private static IconHolder mIconHolder;

    private static class Loadable {
        private Context context;
        private String filePath;
        WeakReference<ImageView> imageView;
        Drawable drawable;

        public Loadable(Context context, ImageView view, String filePath) {
            this.context = context.getApplicationContext();
            this.filePath = filePath;
            this.imageView = new WeakReference<ImageView>(view);
            this.drawable = null;
        }

        public boolean load() {
            return (drawable = loadDrawable(filePath)) != null;
        }

        private Drawable loadDrawable(String filePath) {
            FileCategoryHelper.FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
            switch (fc) {
                case Apk:
                    return getAppDrawable(filePath);
                case Picture:
                    return getImageDrawable(filePath);
                case Video:
                    return getVideoDrawable(filePath);
            }
            return null;
        }

        /**
         * Method that returns the main icon of the app
         *
         * @param String The FilePath
         * @return Drawable The drawable or null if cannot be extracted
         */
        private Drawable getAppDrawable(String filePath) {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath,
                    PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                final ApplicationInfo appInfo = packageInfo.applicationInfo;
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
                return pm.getDrawable(appInfo.packageName, appInfo.icon, appInfo);
            }
            return null;
        }

        /**
         * Method that returns a thumbnail of the picture
         *
         * @param file The path to the file
         * @return Drawable The drawable or null if cannot be extracted
         */
        private Drawable getImageDrawable(String file) {
            Bitmap thumb = ThumbnailUtils.createImageThumbnail(file,
                    ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL);
            if (thumb == null) {
                return null;
            }
            return new BitmapDrawable(context.getResources(), thumb);
        }

        /**
         * Method that returns a thumbnail of the video
         *
         * @param file The path to the file
         * @return Drawable The drawable or null if cannot be extracted
         */
        private Drawable getVideoDrawable(String file) {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file,
                    ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL);
            if (thumb == null) {
                return null;
            }
            return new BitmapDrawable(context.getResources(), thumb);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOADED:
                    processResult((Loadable) msg.obj);
                    break;
            }
        }

        private void processResult(Loadable loadable) {
            ImageView view = loadable.imageView.get();
            if (view == null) {
                return;
            }

            Loadable requestedForImageView = mRequests.get(view);
            if (requestedForImageView != loadable) {
                return;
            }

            if (loadable.drawable != null) {
                mAppIcons.put(loadable.filePath, loadable.drawable);
            }
            view.setImageDrawable(loadable.drawable);
            thumbnailBackground(view, loadable.filePath);
        }
    };

    /**
     * Constructor of <code>IconHolder</code>.
     *
     * @param useThumbs If thumbs of images, videos, apps, ... should be returned
     * instead of the default icon.
     */
    private IconHolder(Context context) {
        super();
        this.mContext = context;
        this.mRequests = new WeakHashMap<ImageView, Loadable>();
        this.mAppIcons = new LinkedHashMap<String, Drawable>(MAX_CACHE, .75F, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Entry<String, Drawable> eldest) {
                return size() > MAX_CACHE;
            }
        };
    }

    public static synchronized IconHolder getIconHolder(Context context) {
        if (mIconHolder == null) {
            mIconHolder = new IconHolder(context);
        }
        return mIconHolder;
    }

    /**
     * Method that returns a drawable reference of a icon.
     *
     * @param resid The resource identifier
     * @return Drawable The drawable icon reference
     */
    public Drawable getDrawable(String resid) {
        String extFromFilename = Util.getExtFromFilename(resid);
        Drawable dw = mContext.getResources().getDrawable(
                                                    FileIconHelper.getFileIcon(extFromFilename));
        return dw;
    }

    /**
     * Method that returns a drawable reference of a FileSystemObject.
     *
     * @param iconView View to load the drawable into
     * @param filePath The file path
     * @param defaultIcon Drawable to be used in case no specific one could be found
     * @return Drawable The drawable reference
     */
    public void loadDrawable(ImageView iconView, String filePath) {
        if (mAppIcons.containsKey(filePath)) {
            iconView.setImageDrawable(mAppIcons.get(filePath));
            thumbnailBackground(iconView, filePath);
            return;
        }

        if (mWorkerThread == null) {
            mWorkerThread = new HandlerThread("IconHolderLoader");
            mWorkerThread.start();
            mWorkerHandler = new WorkerHandler(mWorkerThread.getLooper());
        }
        Loadable previousForView = mRequests.get(iconView);
        if (previousForView != null) {
            mWorkerHandler.removeMessages(MSG_LOAD, previousForView);
        }

        Loadable loadable = new Loadable(mContext, iconView, filePath);
        mRequests.put(iconView, loadable);
        iconView.setImageDrawable(getDrawable(filePath));

        mWorkerHandler.obtainMessage(MSG_LOAD, loadable).sendToTarget();
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD:
                    Loadable l = (Loadable) msg.obj;
                    if (l.load()) {
                        mHandler.obtainMessage(MSG_LOADED, l).sendToTarget();
                    }
                    break;
            }
        }
    }

    /**
     * Shut down worker thread
     */
    private void shutdownWorker() {
        if (mWorkerThread != null) {
            mWorkerThread.getLooper().quit();
            mWorkerHandler = null;
            mWorkerThread = null;
        }
    }

    /**
     * Free any resources used by this instance
     */
    public void cleanup() {
        mRequests.clear();
        mAppIcons.clear();
        shutdownWorker();
    }

    private void thumbnailBackground(ImageView iconView, String filePath) {
        FileCategoryHelper.FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
        switch (fc) {
            case Picture:
            case Video:
                iconView.setBackground(mContext.getResources().
                                       getDrawable(R.drawable.thumbnail_shape));
                break;
            default:
                iconView.setBackground(null);
                break;
        }
    }
}
