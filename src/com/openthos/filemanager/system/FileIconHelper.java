package com.openthos.filemanager.system;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.openthos.filemanager.R;

import java.util.HashMap;
public class FileIconHelper implements FileIconLoader.IconLoadFinishListener {
    private static HashMap<ImageView, ImageView> imageFrames = new HashMap<>();
    private static HashMap<String, Integer> fileExtToIcons = new HashMap<>();
    private FileIconLoader mIconLoader;
    private Context mContext;

    static {
        addItem(new String[] {
            "mp3"
        }, R.mipmap.music_default_bg);
        addItem(new String[] {
            "wma"
        }, R.mipmap.music_default_bg);
        addItem(new String[] {
            "wav"
        }, R.mipmap.music_default_bg);
        addItem(new String[] {
            "mid"
        }, R.mipmap.music_default_bg);
        addItem(new String[] {
                "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf"
        }, R.mipmap.video_default_icon);
        addItem(new String[] {
                "jpg", "jpeg", "gif", "png", "bmp", "wbmp"
        }, R.mipmap.category_icon_picture);
        addItem(new String[] {
                "txt", "log", "xml", "ini", "lrc"
        }, R.mipmap.file_icon_txt);
        addItem(new String[] {
                "doc", "ppt", "docx", "pptx", "xsl", "xslx",
        }, R.mipmap.file_icon_office);
        addItem(new String[] {
            "pdf"
        }, R.mipmap.file_icon_pdf);
        addItem(new String[] {
            "zip"
        }, R.mipmap.file_icon_zip);
        addItem(new String[] {
            "mtz"
        }, R.mipmap.file_icon_theme);
        addItem(new String[] {
            "rar"
        }, R.mipmap.file_icon_rar);
    }

    public FileIconHelper(Context context) {
        mIconLoader = new FileIconLoader(context, this);
        mContext = context;
    }

    private static void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                fileExtToIcons.put(ext.toLowerCase(), resId);
            }
        }
    }

    public static int getFileIcon(String ext) {
        Integer i = fileExtToIcons.get(ext.toLowerCase());
        if (i != null) {
            return i;
        } else {
            return R.mipmap.file_icon_default;
        }

    }

    public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame) {
        String filePath = fileInfo.filePath;
        long fileId = fileInfo.dbId;
        String extFromFilename = Util.getExtFromFilename(filePath);
        FileCategoryHelper.FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
        fileImageFrame.setVisibility(View.GONE);

        boolean set;
        int id = getFileIcon(extFromFilename);
        fileImage.setImageResource(id);

        mIconLoader.cancelRequest(fileImage);
        switch (fc) {
            case Apk:
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                break;
            case Picture:
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                if (!set) {
                    Bitmap fileIcon = getImageThumbnail(filePath,
                          (int)mContext.getResources().getDimension(R.dimen.image_thumbnail_size),
                          (int)mContext.getResources().getDimension(R.dimen.image_thumbnail_size));
                    fileImage.setImageBitmap(fileIcon);
                    set = true;
                }
                break;
            case Video:
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                if (set)
                    fileImageFrame.setVisibility(View.VISIBLE);
                else {
                    fileImage.setImageResource(fc == FileCategoryHelper.FileCategory.Picture ?
                              R.drawable.file_icon_picture : R.mipmap.video_default_icon);
                    imageFrames.put(fileImage, fileImageFrame);
                    set = true;
                }
                break;
            default:
                set = true;
                break;
        }

        if (!set)
            fileImage.setImageResource(R.mipmap.file_icon_default);
    }

    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                                 ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    @Override
    public void onIconLoadFinished(ImageView view) {
        ImageView frame = imageFrames.get(view);
        if (frame != null) {
            frame.setVisibility(View.VISIBLE);
            imageFrames.remove(view);
        }
    }
}
