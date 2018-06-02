package com.flipboard.bottomsheet.commons;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import flipboard.bottomsheet.commons.R;

import static com.flipboard.bottomsheet.commons.ImagePickerSheetView.ImagePickerTile.CAMERA;
import static com.flipboard.bottomsheet.commons.ImagePickerSheetView.ImagePickerTile.PICKER;

/**
 * A Sheet view for displaying recent images or options to pick or take pictures.
 */
@SuppressLint("ViewConstructor")
public class ImagePickerSheetView extends FrameLayout {

    /**
     * Callback for whenever a tile is selected in the sheet.
     */
    public interface OnTileSelectedListener {
        /**
         * @param selectedTile The selected tile, in the form of an {@link ImagePickerTile}
         */
        void onTileSelected(ImagePickerTile selectedTile);
    }

    /**
     * Interface for providing an image given the {@link ImageView} and {@link Uri}.
     */
    public interface ImageProvider {
        /**
         * This is called when the underlying adapter is ready to show an image
         *
         * @param imageView ImageView target. If you care about memory leaks and performance, DO NOT
         *                  HOLD ON TO THIS INSTANCE!
         * @param imageUri Uri for the image.
         * @param size Destination size of the image (it's a square, so assume this is the height
         *             and width).
         */
        void onProvideImage(ImageView imageView, Uri imageUri, int size);
    }

    /**
     * Backing class for image tiles in the grid.
     */
    public static class ImagePickerTile {


        protected ImagePickerSheetImageView view;

        public ImagePickerSheetImageView getView() {
            return view;
        }

        public void setView(ImagePickerSheetImageView view) {
            this.view = view;
        }

        public boolean isChecked() {
            return view.isChecked();
        }

        public void setChecked(boolean checked) {
            this.view.setChecked(checked);
        }

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int PICKER = 3;

        @IntDef({IMAGE, CAMERA, PICKER})
        public @interface TileType {}

        @IntDef({CAMERA, PICKER})
        public @interface SpecialTileType {}

        protected final Uri imageUri;
        protected final @TileType int tileType;

        ImagePickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        ImagePickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        protected ImagePickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        /**
         * @return The image Uri backing this tile. Can be null if this is a placeholder for the
         *         camera or picker tiles.
         */
        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        /**
         * @return The {@link TileType} of this tile: either {@link #IMAGE}, {@link #CAMERA}, or
         *         {@link #PICKER}
         */
        @TileType
        public int getTileType() {
            return tileType;
        }

        /**
         * Indicates whether or not this represents an image tile option. If it is, you can safely
         * retrieve the represented image's file Uri via {@link #getImageUri()}
         *
         * @return True if this is a camera option, false if not.
         */
        public boolean isImageTile() {
            return tileType == IMAGE;
        }

        /**
         * Indicates whether or not this represents the camera tile option. If it is, you should do
         * something to facilitate taking a picture, such as firing a camera intent or using your
         * own.
         *
         * @return True if this is a camera option, false if not.
         */
        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        /**
         * Indicates whether or not this represents the picker tile option. If it is, you should do
         * something to facilitate retrieving a picture from some other provider, such as firing an
         * image pick intent or retrieving it yourself.
         *
         * @return True if this is a picker tile, false if not.
         */
        public boolean isPickerTile() {
            return tileType == PICKER;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isPickerTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }
    }

//    protected final TextView titleView;
    public int[] lastCheckedImg = {0,1};
    public final GridView tileGrid1;
    protected final GridView tileGrid2;
    protected Adapter adapter1;
    protected Adapter adapter2;
    protected int thumbnailSize;
    protected final int originalGridPaddingTop;

    // Values provided by the builder
    protected int maxItems = 51;
    protected ImageProvider imageProvider;
    protected boolean showCameraOption = true;
    protected boolean showPickerOption = true;
    protected Drawable cameraDrawable = null;
    protected Drawable pickerDrawable = null;
    protected @LayoutRes int tileLayout = R.layout.sheet_image_grid_item;



    protected ImagePickerSheetView(final Builder builder) {
        super(builder.context);

        inflate(getContext(), R.layout.grid_sheet_view1, this);

        // Set up the grid
        tileGrid1 = (GridView) findViewById(R.id.grid1);
        tileGrid2 = (GridView) findViewById(R.id.grid2);

        // Set up the title
//        titleView = (TextView) findViewById(R.id.title);
        originalGridPaddingTop = tileGrid1.getPaddingTop();
//        setTitle(builder.title);

        // Hook up the remaining builder fields
        if (builder.onTileSelectedListener != null) {
            tileGrid1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                    setUnchecked();
                    builder.onTileSelectedListener.onTileSelected(adapter1.getItem(position));
                    adapter1.getItem(position).setChecked(true);
                    lastCheckedImg[0] = 0;
                    lastCheckedImg[1] = position;
                }
            });
        }
        if (builder.onTileSelectedListener != null) {
            tileGrid2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                    setUnchecked();
                    builder.onTileSelectedListener.onTileSelected(adapter2.getItem(position));
                    adapter2.getItem(position).setChecked(true);
                    lastCheckedImg[0] = 1;
                    lastCheckedImg[1] = position;
                }
            });
        }
        maxItems = builder.maxItems;
        imageProvider = builder.imageProvider;
        showCameraOption = builder.showCameraOption;
        showPickerOption = builder.showPickerOption;
        cameraDrawable = builder.cameraDrawable;
        pickerDrawable = builder.pickerDrawable;
        tileLayout = builder.tileLayout;



        ViewCompat.setElevation(this, Util.dp2px(getContext(), 16f));
    }

    void setUnchecked(){

        if (lastCheckedImg[0]==0){
            adapter1.getItem(lastCheckedImg[1]).setChecked(false);
        } else {
            adapter2.getItem(lastCheckedImg[1]).setChecked(false);
        }
    }

//    public void setTitle(@StringRes int titleRes) {
//        setTitle(getResources().getString(titleRes));
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//        if (!TextUtils.isEmpty(title)) {
//            titleView.setText(title);
//        } else {
//            titleView.setVisibility(GONE);
//            // Add some padding to the top to account for the missing title
//            tileGrid.setPadding(tileGrid.getPaddingLeft(), originalGridPaddingTop + spacing, tileGrid.getPaddingRight(), tileGrid.getPaddingBottom());
//        }
//    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.adapter1 = new Adapter(getContext(),true);
        this.adapter2 = new Adapter(getContext(),false);
        tileGrid1.setAdapter(this.adapter1);
        tileGrid2.setAdapter(this.adapter2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Necessary for showing elevation on 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new Util.ShadowOutline(w, h));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        tileGrid1.setNumColumns(tileGrid1.getAdapter().getCount());
        tileGrid2.setNumColumns(tileGrid1.getAdapter().getCount());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Simple adapter that shows a grid of {@link ImagePickerSheetImageView}s that hold either a thumbnail of
     * the local image or placeholder for camera/picker actions.
     */
    private class Adapter extends BaseAdapter {

        private List<ImagePickerTile> tiles = new ArrayList<>();
        final LayoutInflater inflater;
        private final ContentResolver resolver;

        public Adapter(Context context,boolean ger) {
            inflater = LayoutInflater.from(context);

            if (ger) {
                tiles.add(new ImagePickerTile(CAMERA));
            }
            if (!ger) {
                tiles.add(new ImagePickerTile(PICKER));
            }

            // Add local images, in descending order of date taken
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.MIME_TYPE
            };
            resolver = context.getContentResolver();

            final Cursor cursor = resolver
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                            null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext() && count < maxItems) {
                    String imageLocation = cursor.getString(1);
                    File imageFile = new File(imageLocation);
                    if (imageFile.exists()) {
                        if (ger&&(cursor.getPosition()%2==0))
                            tiles.add(new ImagePickerTile(Uri.fromFile(imageFile)));
                        else if (!ger&&(cursor.getPosition()%2!=0))
                            tiles.add(new ImagePickerTile(Uri.fromFile(imageFile)));
                    }
                    ++count;
                }
                count+=2;
                cursor.close();
                int k;
                if (count%2==0){
                    k = (count/2*84)+((count/2-1)*8);
                } else {
                    k = ((count/2+1)*84)+((count/2)*8);
                }
                tileGrid1.setLayoutParams(new LinearLayout.LayoutParams(
                        Float.valueOf(k*context.getResources().getDisplayMetrics().density).intValue(),
                        LinearLayout.LayoutParams.MATCH_PARENT,1));;
                tileGrid2.setLayoutParams(new LinearLayout.LayoutParams(
                        Float.valueOf(k*context.getResources().getDisplayMetrics().density).intValue(),
                        LinearLayout.LayoutParams.MATCH_PARENT,1));
            }
        }

        @Override
        public int getCount() {
            return tiles.size();
        }

        @Override
        public ImagePickerTile getItem(int position) {
            return tiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View recycled, @NonNull ViewGroup parent) {
            ImageView thumb;

            if (recycled == null) {
                if (tileLayout == 0) {
                    thumb = (ImageView) inflater.inflate(R.layout.sheet_image_grid_item, parent, false);
                } else {
                    thumb = (ImageView) inflater.inflate(tileLayout, parent, false);
                    if (!(thumb instanceof ImageView)) {
                        throw new IllegalArgumentException("Tile layout must have an ImageView as root view.");
                    }
                }
            } else {
                thumb = (ImageView) recycled;
            }

            ImagePickerTile tile = tiles.get(position);
            tile.setView((ImagePickerSheetImageView)thumb);
//            thumb.setMinimumWidth(thumbnailSize);
//            thumb.setMinimumHeight(thumbnailSize);
//            thumb.setMaxHeight(thumbnailSize);
//            thumb.setMaxWidth(thumbnailSize);

            if (tile.imageUri != null) {
                imageProvider.onProvideImage(thumb, tile.imageUri, thumbnailSize);
            } else {
                thumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (tile.isCameraTile()) {
                    thumb.setBackgroundResource(R.drawable.white_blue);
                    thumb.setImageResource(R.mipmap.ic_photopicker_camera);
                } else if (tile.isPickerTile()) {
                    thumb.setBackgroundResource(R.drawable.white_blue);
                    thumb.setImageResource(R.mipmap.ic_photopicker_albums);
                }
            }

            return thumb;
        }
    }

    public static class Builder {

        Context context;
        int maxItems = 51;
        String title = null;
        OnTileSelectedListener onTileSelectedListener;
        ImageProvider imageProvider;
        boolean showCameraOption = true;
        boolean showPickerOption = true;
        Drawable cameraDrawable = null;
        Drawable pickerDrawable = null;
        @LayoutRes int tileLayout = 0;

        public Builder(@NonNull Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required READ_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }
            this.context = context;
        }

        /**
         * Sets the max number of tiles to show in the image picker. Default is 25 from local
         * storage and the two custom tiles.
         *
         * @param maxItems Max number of tiles to show
         * @return This builder instance
         */
        public Builder setMaxItems(int maxItems) {
            this.maxItems = 100;
            return this;
        }

        /**
         * Sets a title via String resource ID.
         *
         * @param title String resource ID
         * @return This builder instance
         */
        public Builder setTitle(@StringRes int title) {
            return setTitle(context.getString(title));
        }

        /**
         * Sets a title for this sheet. If the title param is null, then no title will be shown and
         * the title view will be hidden.
         *
         * @param title The title String
         * @return This builder instance
         */
        public Builder setTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets a listener for when a tile is selected.
         *
         * @param onTileSelectedListener Listener instance
         * @return This builder instance
         */
        public Builder setOnTileSelectedListener(OnTileSelectedListener onTileSelectedListener) {
            this.onTileSelectedListener = onTileSelectedListener;
            return this;
        }

        /**
         * Sets a provider for providing images.
         *
         * @param imageProvider Provider instance
         * @return This builder instance
         */
        public Builder setImageProvider(ImageProvider imageProvider) {
            this.imageProvider = imageProvider;
            return this;
        }

        /**
         * Sets a boolean to indicate whether or not to show a camera option.
         *
         * @param showCameraOption True to show the option, or false to hide the option
         * @return This builder instance
         */
        public Builder setShowCameraOption(boolean showCameraOption) {
            this.showCameraOption = showCameraOption;
            return this;
        }

        /**
         * Sets a boolean to indicate whether or not to show the picker option.
         *
         * @param showPickerOption True to show the option, or false to hide the option.
         * @return This builder instance
         */
        public Builder setShowPickerOption(boolean showPickerOption) {
            this.showPickerOption = showPickerOption;
            return this;
        }

        /**
         * Sets a drawable resource ID for the camera option tile. Default is to use the material
         * design version included in the library.
         *
         * @param resId Camera drawable resource ID
         * @return This builder instance
         */
        public Builder setCameraDrawable(@DrawableRes int resId) {
            return setCameraDrawable(ResourcesCompat.getDrawable(context.getResources(), resId, null));
        }

        /**
         * Sets a drawable for the camera option tile. Default is to use the material design
         * version included in the library.
         *
         * @param cameraDrawable Camera drawable instance
         * @return This builder instance
         */
        public Builder setCameraDrawable(@Nullable Drawable cameraDrawable) {
            this.cameraDrawable = cameraDrawable;
            return this;
        }

        /**
         * Sets a drawable resource ID for the picker option tile. Default is to use the material
         * design version included in the library.
         *
         * @param resId Picker drawable resource ID
         * @return This builder instance
         */
        public Builder setPickerDrawable(@DrawableRes int resId) {
            return setPickerDrawable(ResourcesCompat.getDrawable(context.getResources(), resId, null));
        }

        /**
         * Sets a layout for the image tile which MUST have an ImageView as root view. Default is to
         * use plain image view included in the library.
         *
         * @param tileLayout Tile layout resource ID
         * @return This builder instance
         */
        public Builder setTileLayout(@LayoutRes int tileLayout) {
            this.tileLayout = tileLayout;
            return this;
        }

        /**
         * Sets a drawable for the picker option tile. Default is to use the material design
         * version included in the library.
         *
         * @param pickerDrawable Picker drawable instance
         * @return This builder instance
         */
        public Builder setPickerDrawable(Drawable pickerDrawable) {
            this.pickerDrawable = pickerDrawable;
            return this;
        }

        @CheckResult
        public ImagePickerSheetView create() {
            if (imageProvider == null) {
                throw new IllegalStateException("Must provide an ImageProvider!");
            }
            return new ImagePickerSheetView(this);
        }
    }

}
