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

    private static final String SUFFIX_APE = "ape";
    private static final String SUFFIX_AVI = "avi";
    private static final String SUFFIX_DOC = "doc";
    private static final String SUFFIX_HTML = "html";
    private static final String SUFFIX_MP3 = "mp3";
    private static final String SUFFIX_MP4 = "mp4";
    private static final String SUFFIX_PPT = "ppt";
    private static final String SUFFIX_TXT = "txt";
    private static final String SUFFIX_WAV = "wav";
    private static final String SUFFIX_WMV = "wmv";
    private static final String SUFFIX_XLS = "xls";
    private static final String SUFFIX_PDF = "pdf";
    private static final String SUFFIX_RM = "rm";
    private static final String SUFFIX_RMVB = "rmvb";
    private static final String SUFFIX_TAR = "tar";
    private static final String SUFFIX_BZ2 = "bz2";
    private static final String SUFFIX_GZ = "gz";
    private static final String SUFFIX_ZIP = "zip";
    private static final String SUFFIX_RAR = "rar";
    private static final String SUFFIX_XLSX = "xlsx";
    private static final String SUFFIX_DOCX = "docx";
    private static final String SUFFIX_PPTX = "pptx";
    private static final String SUFFIX_3GP = "3gp";
    private static final String SUFFIX_BMP = "bmp";
    private static final String SUFFIX_GIF = "gif";
    private static final String SUFFIX_PNG = "png";
    private static final String SUFFIX_ISO = "iso";
    private static final String SUFFIX_JPG = "jpg";
    private static final String SUFFIX_JPEG = "jpeg";

    public static int getFileIcon(String ext) {
        // Integer i = fileExtToIcons.get(ext.toLowerCase());
        // if (i != null) {
        //     return i;
        // } else {
        //     return R.mipmap.file_icon_default;
        // }
        switch (ext) {
            case SUFFIX_APE:
                return R.mipmap.suffix_ape;
            case SUFFIX_AVI:
                return R.mipmap.suffix_avi;
            case SUFFIX_DOC:
            case SUFFIX_DOCX:
                return R.mipmap.suffix_doc;
            case SUFFIX_HTML:
                return R.mipmap.suffix_html;
            case SUFFIX_MP3:
                return R.mipmap.suffix_mp3;
            case SUFFIX_MP4:
                return R.mipmap.suffix_mp4;
            case SUFFIX_PPT:
            case SUFFIX_PPTX:
                return R.mipmap.suffix_ppt;
            case SUFFIX_TXT:
                return R.mipmap.suffix_txt;
            case SUFFIX_WAV:
                return R.mipmap.suffix_wav;
            case SUFFIX_WMV:
                return R.mipmap.suffix_wmv;
            case SUFFIX_XLS:
            case SUFFIX_XLSX:
                return R.mipmap.suffix_xls;
            case SUFFIX_PDF:
                return R.mipmap.suffix_pdf;
            case SUFFIX_RM:
                return R.mipmap.suffix_rm;
            case SUFFIX_RMVB:
                return R.mipmap.suffix_rmvb;
            case SUFFIX_TAR:
                return R.mipmap.suffix_tar;
            case SUFFIX_BZ2:
                return R.mipmap.suffix_bz2;
            case SUFFIX_GZ:
                return R.mipmap.suffix_gz;
            case SUFFIX_ZIP:
                return R.mipmap.suffix_zip;
            case SUFFIX_RAR:
                return R.mipmap.suffix_rar;
            case SUFFIX_3GP:
                return R.mipmap.suffix_3gp;
            case SUFFIX_BMP:
                return R.mipmap.suffix_bmp;
            case SUFFIX_PNG:
                return R.mipmap.suffix_png;
            case SUFFIX_GIF:
                return R.mipmap.suffix_gif;
            case SUFFIX_JPG:
                return R.mipmap.suffix_jpg;
            case SUFFIX_JPEG:
                return R.mipmap.suffix_jpeg;
            case SUFFIX_ISO:
                return R.mipmap.suffix_iso;
            default:
                return R.mipmap.suffix_default;
        }
    }

    public void setIcon(FileInfo fileInfo, ImageView fileImage) {
        String filePath = fileInfo.filePath;
        long fileId = fileInfo.dbId;
        String extFromFilename = Util.getExtFromFilename(filePath);
        FileCategoryHelper.FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);

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
                if (!set) {
                    fileImage.setImageResource(fc == FileCategoryHelper.FileCategory.Picture ?
                              R.drawable.file_icon_picture : R.mipmap.video_default_icon);
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
