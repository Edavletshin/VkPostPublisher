package edavletshin.com.vkpostpublisher;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.StickerView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edavletshin.com.vkpostpublisher.util.CustomEditText;
import edavletshin.com.vkpostpublisher.util.RoundedBackgroundSpan;

import static com.vk.sdk.VKSdk.LoginState.LoggedOut;

public class MainScreen extends AppCompatActivity {

    private int text_state = 0;
    private static final int REQUEST_STORAGE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = REQUEST_STORAGE + 1;
    private static final int REQUEST_LOAD_IMAGE = REQUEST_IMAGE_CAPTURE + 1;
    private Uri cameraImageUri = null;


    final Object object = new Object();

    TabLayout tabLayout;
    long back_pressed = 0;
    ImageView trash;
    long stickerTouchedTime = 0;
    float stickerLastX = 0;
    float stickerLastY = 0;
    MenuSheetView menuSheetView;
    protected BottomSheetLayout bottomSheetLayout;
    AppCompatTextView copyText;
    int currentItem = 0;
    Button sendButton;
    boolean flag = false;
    Bitmap lastImg = null;
    ImagePickerSheetView sheetView;
    FrameLayout fram;
    ImageView mainImg;
    StickerView stickerView;
    StickerView mainstickerView;
    CustomEditText wazzap;
    ExpandableLayout exLayout;
    CheckableImageButton[] imgBtns = new CheckableImageButton[9];
    File sd = Environment.getExternalStorageDirectory();
    File file;

    @Override
    protected void onStop() {
        super.onStop();
        if (sheetView!=null){
            if (sheetView.getVisibility()==View.VISIBLE)
                sheetView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                keyboard(true);
            }
        }, 100);
        if (!exLayout.isExpanded())
            exLayout.expand();
        wazzap.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                keyboard(true);
            }
        }, 100);
        if (!exLayout.isExpanded())
            exLayout.expand();
        wazzap.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sheetView!=null){
            if (sheetView.getVisibility()==View.VISIBLE)
                sheetView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VKSdk.wakeUpSession(this,LoginState);
        initBeachAndStars();
        initIds();
        initButtons();

    }

    private void initBeachAndStars() {

        File sd = Environment.getExternalStorageDirectory();
        File file = new File(sd,"beach.jpg");

        if (file.length()==0) {

            Display display = getWindowManager().getDefaultDisplay();
            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.bg_beach_center);

            FileOutputStream out = null;

            try {
                out = new FileOutputStream(file);

                Bitmap.createBitmap(bitmapOrg,0 ,0, display.getWidth(), display.getHeight()).compress(Bitmap.CompressFormat.JPEG,80, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        file = new File(sd,"stars.jpg");

        if (file.length()==0) {

            Display display = getWindowManager().getDefaultDisplay();
            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.bg_stars_center);

            FileOutputStream out = null;

            try {
                out = new FileOutputStream(file);
                Bitmap bitmapOrg1 = Bitmap.createBitmap(bitmapOrg,0 ,0, display.getWidth()*2,display.getHeight()*2);

                Matrix matrix = new Matrix();
                matrix.postScale((float)display.getWidth()/bitmapOrg1.getWidth(),(float)display.getHeight()/bitmapOrg1.getHeight());
                Bitmap.createBitmap(bitmapOrg,0 ,0, bitmapOrg1.getWidth(),bitmapOrg1.getHeight(),matrix,true).compress(Bitmap.CompressFormat.JPEG,100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initIds() {

        trash = (ImageView) findViewById(R.id.trash);
        bottomSheetLayout = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheetLayout.setPeekOnDismiss(true);
        bottomSheetLayout.addOnSheetDismissedListener(new OnSheetDismissedListener() {
            @Override
            public void onDismissed(BottomSheetLayout bottomSheetLayout) {
                    if (exLayout.isExpanded()) {
                        if (currentItem!=8)
                            keyboard(true);
                        else {
                            if (sheetView!=null)
                                if (sheetView.getVisibility()!=View.VISIBLE)
                                    sheetView.setVisibility(View.VISIBLE);
                        }
                    }
            }
        });
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.post));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.history));
        (((LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(0)).getChildAt(1)).setScaleY(-1);
        (((LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(1)).getChildAt(1)).setScaleY(-1);

        copyText = (AppCompatTextView)findViewById(R.id.copyText);
        fram = (FrameLayout) findViewById(R.id.fram);
        sendButton = (Button) findViewById(R.id.sendButton);
        imgBtns[0] = (CheckableImageButton) findViewById(R.id.cib1);
        ((CheckableImageButton) findViewById(R.id.cib1)).setChecked(true);
        imgBtns[1] = (CheckableImageButton) findViewById(R.id.cib2);
        imgBtns[2] = (CheckableImageButton) findViewById(R.id.cib3);
        imgBtns[3] = (CheckableImageButton) findViewById(R.id.cib4);
        imgBtns[4] = (CheckableImageButton) findViewById(R.id.cib5);
        imgBtns[5] = (CheckableImageButton) findViewById(R.id.cib6);
        imgBtns[6] = (CheckableImageButton) findViewById(R.id.cib7);
        imgBtns[7] = (CheckableImageButton) findViewById(R.id.cib8);
        imgBtns[8] = (CheckableImageButton) findViewById(R.id.cib9);

        mainImg = (ImageView) findViewById(R.id.mainimg);
        stickerView = (StickerView) findViewById(R.id.sticker_view);
        mainstickerView = (StickerView) findViewById(R.id.main_stickers);
        wazzap = (CustomEditText) findViewById(R.id.wazzap);
        wazzap.setOnHideKeyboardListener(new CustomEditText.HideKeyBoard() {
            @Override
            public void onBackPressed() {
                exLayout.collapse();
            }
        });
        exLayout = (ExpandableLayout)findViewById(R.id.expandable_layout);
        exLayout.expand(true);
    }

    @Override
    public void onBackPressed() {
        if (sheetView!=null){
            if (sheetView.getVisibility()==View.VISIBLE) {
                sheetView.setVisibility(View.GONE);
                exLayout.collapse();
            } else {
                if (back_pressed + 2000 > System.currentTimeMillis())
                    super.onBackPressed();
                else
                    Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода",
                            Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    private void initButtons() {

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                RelativeLayout.LayoutParams params;
                RelativeLayout rel = (RelativeLayout) findViewById(R.id.centerelative);
                RelativeLayout backrel = (RelativeLayout) findViewById(R.id.backrel);
                switch (tab.getPosition()) {
                    case 0:
                        params = (RelativeLayout.LayoutParams) rel.getLayoutParams();
                        params.height = getWindowManager().getDefaultDisplay().getWidth() ;
                        rel.setLayoutParams(params);
                        params = (RelativeLayout.LayoutParams) backrel.getLayoutParams();
                        params.addRule(RelativeLayout.BELOW, R.id.toolbar);
                        params.addRule(RelativeLayout.ABOVE, R.id.kek);
                        backrel.setLayoutParams(params);
                        params = (RelativeLayout.LayoutParams) trash.getLayoutParams();
                        params.bottomMargin = 16*Float.valueOf(getResources().getDisplayMetrics().density).intValue();
                        trash.setLayoutParams(params);
                        break;
                    case 1:
                        params = (RelativeLayout.LayoutParams) rel.getLayoutParams() ;
                        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        rel.setLayoutParams(params);
                        params = (RelativeLayout.LayoutParams) backrel.getLayoutParams();
                        params.addRule(RelativeLayout.BELOW, 0);
                        params.addRule(RelativeLayout.ABOVE, R.id.frame);
                        backrel.setLayoutParams(params);
                        params = (RelativeLayout.LayoutParams) trash.getLayoutParams();
                        params.bottomMargin = 72*Float.valueOf(getResources().getDisplayMetrics().density).intValue();
                        trash.setLayoutParams(params);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getTabAt(1).select();
        tabLayout.getTabAt(0).select();
        mainstickerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mainstickerView.getCurrentSticker()!=null){
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        stickerLastX = mainstickerView.getCurrentSticker().getMappedBound().centerX();
                        stickerLastY = mainstickerView.getCurrentSticker().getMappedBound().centerY();
                        stickerTouchedTime = System.currentTimeMillis();
                    }
                    if ((((stickerLastX+100<mainstickerView.getCurrentSticker().getMappedBound().centerX())||(stickerLastX-100>mainstickerView.getCurrentSticker().getMappedBound().centerX()))
                            ||
                            ((stickerLastY+100<mainstickerView.getCurrentSticker().getMappedBound().centerY())||(stickerLastY-100>mainstickerView.getCurrentSticker().getMappedBound().centerY())))
                            &&
                            (stickerTouchedTime+1000<System.currentTimeMillis())){
                        if (trash.getVisibility()==View.GONE) {
                            trash.setVisibility(View.VISIBLE);
                            Animation animation = AnimationUtils.loadAnimation(MainScreen.this, R.anim.show);
                            trash.startAnimation(animation);
                            if (currentItem==0){
                                trash.setBackground(getResources().getDrawable(R.drawable.trash_background_whitebg));
                            } else {
                                trash.setBackground(getResources().getDrawable(R.drawable.trash_background));
                            }

                        }
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        trash.setImageResource(R.mipmap.ic_fab_trash);
                        mainstickerView.getCurrentSticker().setAlpha(255);
                        if (trash.getVisibility()==View.VISIBLE) {
                            if ((Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerX() - (trash.getX()+50f))).doubleValue(),2.0) + Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerY() - (trash.getY()+50f))).doubleValue(),2.0)) < 10000.0) {
                                mainstickerView.remove(mainstickerView.getCurrentSticker());
                            }
                            Animation animation = AnimationUtils.loadAnimation(MainScreen.this, R.anim.hide);
                            trash.startAnimation(animation);
                            trash.setVisibility(View.GONE);
                        }
                    }
                    if (trash.getVisibility()==View.VISIBLE){
                        if ((Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerX()-(trash.getX()+50f))).doubleValue(),2.0)+Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerY()-(trash.getY()+50f))).doubleValue(),2.0))<15000.0){
                            mainstickerView.getCurrentSticker().setAlpha(
                                    Double.valueOf((Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerX()-(trash.getX()+50f))).doubleValue(),2.0)+Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerY()-(trash.getY()+50f))).doubleValue(),2.0))/15000.0*255.0).intValue()
                            );
                            if ((Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerX()-(trash.getX()+50f))).doubleValue(),2.0)+Math.pow(Float.valueOf(Math.abs(mainstickerView.getCurrentSticker().getMappedBound().centerY()-(trash.getY()+50f))).doubleValue(),2.0))<10000.0){
                                trash.setImageResource(R.mipmap.ic_fab_trash_released);
                            }else if (trash.getDrawable()!=getResources().getDrawable(R.mipmap.ic_fab_trash)) {
                                trash.setImageResource(R.mipmap.ic_fab_trash);
                            }
                        } else {
                            mainstickerView.getCurrentSticker().setAlpha(255);
                        }
                    }
                    }
                return false;
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyboard(false);
                exLayout.collapse();
                wazzap.setCursorVisible(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Bitmap btm = stickerView.createBitmap();
                        if (tabLayout.getSelectedTabPosition()==0) {
                            Matrix matrix = new Matrix();
                            matrix.postScale((float) 1080 / btm.getWidth(), (float) 1080 / btm.getHeight());
                            Global.setImg(Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true));
                        } else {
                            Matrix matrix = new Matrix();
                            if (btm.getWidth()<btm.getHeight()) {
                                matrix.postScale((float) 1080 / btm.getWidth(), (float)  1080 / btm.getWidth());
                                Global.setImg(Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true));
                            } else {
                                matrix.postScale((float) 1080 / btm.getHeight(), (float) 1080 / btm.getHeight());
                                Global.setImg(Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true));
                            }
                        }
                        Intent intent = new Intent(MainScreen.this,Send.class);
                        ActivityOptions options = ActivityOptions.makeCustomAnimation(MainScreen.this, R.anim.fadein, R.anim.fadeout);
                        startActivity(intent, options.toBundle());
                    }
                }, 100);
            }
        });

        findViewById(R.id.textStyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text_state++;
                if (text_state==3)
                    text_state=0;
                switch (text_state){
                    case 0:
                        copyText.setText("");
                        if (currentItem!=0)
                            wazzap.setTextColor(Color.parseColor("#FFFFFF"));
                        else
                            wazzap.setTextColor(Color.parseColor("#2c2d2e"));
                        wazzap.setBackground(getResources().getDrawable(android.R.color.transparent));
                        break;
                    case 1:
                        newLineForLongWords();
                        wazzap.setTextColor(Color.parseColor("#2c2d2e"));
                        if (currentItem!=0) {
                            span(Color.parseColor("#FFFFFF"));
                        } else {
                            span(Color.parseColor("#DDDDDD"));
                        }

                        break;
                    case 2:

                        wazzap.setTextColor(Color.parseColor("#FFFFFF"));

                        if (currentItem!=0) {
                            span(Color.parseColor("#40FFFFFF"));
                        } else {
                            span(Color.parseColor("#000000"));
                        }



                        break;
                }
            }
        });

        for (int i=0;i<9;i++) {
            imgBtns[i].setOnClickListener(colorListener);
        }



        wazzap.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {


                if (charSequence.toString().contains("  ")){
                    String temp = wazzap.getText().toString();
                    wazzap.setText("");
                    wazzap.append(temp.replaceAll("  ", " "));
                }



                if (text_state==1){
                    newLineForLongWords();
                    if (currentItem!=0) {
                        span(Color.parseColor("#FFFFFF"));
                    } else {
                        span(Color.parseColor("#DDDDDD"));
                    }
                }

                if (text_state==2){
                    newLineForLongWords();
                    if (currentItem!=0) {
                        span(Color.parseColor("#40FFFFFF"));
                    } else {
                        span(Color.parseColor("#000000"));
                    }
                }
                if (charSequence.length()==0)
                    sendButton.setEnabled(false);
                else
                    sendButton.setEnabled(true);

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }


        });

        wazzap.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                exLayout.expand(true);
                if (b&& sheetView!=null)
                    if (sheetView.getVisibility()!=View.GONE)
                        sheetView.setVisibility(View.GONE);
            }
        });
        wazzap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                wazzap.setCursorVisible(true);
                exLayout.expand(true);
                if (sheetView!=null)
                    if (sheetView.getVisibility()!=View.GONE)
                        sheetView.setVisibility(View.GONE);
                return false;
            }
        });

        findViewById(R.id.stickers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuSheet(MenuSheetView.MenuType.GRID);
            }
        });

        imgBtns[6].setBackground(new BitmapDrawable(getResources(), ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.thumb_beach),8)));
        imgBtns[7].setBackground(new BitmapDrawable(getResources(), ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.thumb_stars),8)));

    }

    void newLineForLongWords(){
        for (String word:Arrays.asList(wazzap.getText().toString().replace("\n"," ").split(" "))){
            if (wazzap.getPaint().measureText(word)/getResources().getDisplayMetrics().density>312) {
                boolean flajok = true;
                StringBuilder temp = new StringBuilder(word);
                Paint paint = wazzap.getPaint();
                while (flajok) {
                    temp.deleteCharAt(temp.length() - 1);
                    float width = paint.measureText(temp.toString());
                    if (313 > width / getResources().getDisplayMetrics().density) {
                        flajok = false;
                    }
                }
                StringBuilder temp1 = new StringBuilder(wazzap.getText());
                temp1.insert(temp1.indexOf(temp.toString())+temp.length(),"\n");
                wazzap.setText("");
                wazzap.append(temp1);
            }
        }
    }

    void span(int backgroundColor){
        copyText.setText("");
        List<String> list = Arrays.asList(wazzap.getText().toString().split(" "));
        if (list.get(0).length()!=0) {
            for (int i = 0; i < list.size(); i++) {
                SpannableString staffNameSpan;
                staffNameSpan = new SpannableString(list.get(i) + " ");
                SpannableStringBuilder builder = new SpannableStringBuilder();
                RoundedBackgroundSpan tagSpan = new RoundedBackgroundSpan(backgroundColor,getResources().getDisplayMetrics().density);
                staffNameSpan.setSpan(tagSpan, 0, staffNameSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(staffNameSpan);
                copyText.append(builder);
            }
        }
    }

    VKCallback<VKSdk.LoginState> LoginState = new VKCallback<VKSdk.LoginState>() {
        @Override
        public void onResult(VKSdk.LoginState res) {
                if (res == LoggedOut) {
                    startActivity(new Intent(MainScreen.this, Login.class));
                } else {
                    //initTowns();
                    //initNavigationView();
                   // initID();
            }
        }

        @Override
        public void onError(VKError error) {
            Toast.makeText(MainScreen.this, error.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    void keyboard(boolean opcl){
        View view = MainScreen.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!opcl)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            else
                imm.showSoftInput(view, 0);
        }
    }

    View.OnClickListener colorListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId()!=R.id.cib1&&text_state!=1)
                wazzap.setTextColor(Color.parseColor("#FFFFFF"));

            ((CheckableImageButton)view).setChecked(true);

            if (flag && view.getId()!=R.id.cib9){

                if (sheetView.getVisibility()==View.VISIBLE)
                    sheetView.setVisibility(View.GONE);
                lastImg = drawableToBitmap(mainImg.getDrawable());

                wazzap.requestFocus();
                if (!exLayout.isExpanded())
                    keyboard(true);
                flag = false;
            }

            for (int i=0;i<9;i++)
                if (imgBtns[i]!=view)
                    imgBtns[i].setChecked(false);

            if((view.getId()!=R.id.cib7)&&(findViewById(R.id.imageView2).getVisibility()==View.VISIBLE)){
                findViewById(R.id.imageView2).setVisibility(View.GONE);
                findViewById(R.id.imageView3).setVisibility(View.GONE);
            }

            switch (view.getId()){
                case R.id.cib1:
                    currentItem=0;
                    wazzap.setTextColor(Color.parseColor("#2c2d2e"));
                    mainImg.setImageDrawable(getResources().getDrawable(R.color.vk_white));
                    break;
                case R.id.cib2:
                    currentItem=1;
                    mainImg.setImageResource(R.drawable.gradient_blue);
                    break;
                case R.id.cib3:
                    currentItem=2;
                    mainImg.setImageResource(R.drawable.gradient_green);
                    break;
                case R.id.cib4:
                    currentItem=3;
                    mainImg.setImageResource(R.drawable.gradient_orange);
                    break;
                case R.id.cib5:
                    currentItem=4;
                    mainImg.setImageResource(R.drawable.gradient_red);
                    break;
                case R.id.cib6:
                    currentItem=5;
                    mainImg.setImageResource(R.drawable.gradient_purple);
                    break;
                case R.id.cib7:
                    currentItem=6;
                    file = new File(sd,"beach.jpg");
                    if (file.length()==0)
                        initBeachAndStars();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    BitmapDrawable btm = new BitmapDrawable(bitmap);
                    mainImg.setImageDrawable(btm);
                    findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
                    findViewById(R.id.imageView3).setVisibility(View.VISIBLE);


                    break;
                case R.id.cib8:
                    currentItem=7;
                    file = new File(sd,"stars.jpg");
                    if (file.length()==0)
                        initBeachAndStars();
                    options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    btm = new BitmapDrawable(bitmap);
                    mainImg.setImageDrawable(btm);



                    break;
                case R.id.cib9:
                    currentItem=8;

                    keyboard(false);
                    exLayout.expand(true);
                    if (sheetView!=null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sheetView.setVisibility(View.VISIBLE);
                            }
                        }, 50);
                    }

                    if (!flag) {
                        initImages();
                        flag = true;
                        break;
                    }


            }

            if (text_state==1){
                wazzap.setTextColor(Color.parseColor("#2c2d2e"));
                if (currentItem!=0) {
                    span(Color.parseColor("#FFFFFF"));
                } else {
                    span(Color.parseColor("#DDDDDD"));
                }
            }

            if (text_state==2){
                wazzap.setTextColor(Color.parseColor("#FFFFFF"));
                if (currentItem!=0) {
                    span(Color.parseColor("#40FFFFFF"));
                } else {
                    span(Color.parseColor("#000000"));
                }
            }
        }
    };

    void initImages(){


        if (sheetView==null) {
            sheetView = new ImagePickerSheetView.Builder(this)
                    .setMaxItems(102)
                    .setShowCameraOption(createCameraIntent() != null)
                    .setShowPickerOption(createPickIntent() != null)
                    .setImageProvider(new ImagePickerSheetView.ImageProvider() {
                        @Override
                        public void onProvideImage(final ImageView imageView, Uri imageUri, int size) {
                            synchronized (object) {
                                Glide.with(MainScreen.this)
                                        .load(imageUri).asBitmap()
                                        .centerCrop()
                                        .into(new SimpleTarget<Bitmap>(168, 168) {
                                            @Override
                                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                                imageView.setBackground(new BitmapDrawable(getResources(), ImageHelper.getRoundedCornerBitmap(resource, 8)));
                                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.buttonbackground));
                                            }
                                        });
                            }
                        }
                    })
                    .setOnTileSelectedListener(new ImagePickerSheetView.OnTileSelectedListener() {
                        @Override
                        public void onTileSelected(ImagePickerSheetView.ImagePickerTile selectedTile) {
                            if (selectedTile.isCameraTile()) {
                                dispatchTakePictureIntent();
                            } else if (selectedTile.isPickerTile()) {
                                startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
                            } else if (selectedTile.isImageTile()) {
                                showSelectedImage(selectedTile.getImageUri());
                            } else {
                                genericError();
                            }
                        }
                    })
                    .create();
            fram.addView(sheetView);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((ImagePickerSheetView.ImagePickerTile)sheetView.tileGrid1.getAdapter().getItem(1)).setChecked(true);
                        Glide.with(MainScreen.this)
                                .load(((ImagePickerSheetView.ImagePickerTile) sheetView.tileGrid1.getAdapter().getItem(1)).getImageUri())
                                .crossFade()
                                .fitCenter()
                                .into(mainImg);

                    } catch (Exception e) {
                        Log.e("huina kaka9 to", e.toString());
                    }
                }
            }, 100);
        } else {
            if (lastImg!=null){
                mainImg.setImageBitmap(lastImg);
            }
        }
    }

    private void showMenuSheet(final MenuSheetView.MenuType menuType) {
        if (menuSheetView==null) {
            menuSheetView =
                    new MenuSheetView(MainScreen.this, menuType, "Стикеры", new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            Bitmap btm = ((BitmapDrawable)item.getIcon()).getBitmap();

                            mainstickerView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), btm)));

                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }
                            return true;
                        }
                    });

        }
        bottomSheetLayout.showWithSheetView(menuSheetView);
        keyboard(false);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraImageUri = Uri.fromFile(imageFile);
        return imageFile;
    }

    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }

    @Nullable
    private Intent createCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            return takePictureIntent;
        } else {
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = createCameraIntent();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent != null) {
            // Create the File where the photo should go
            try {
                File imageFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                // Error occurred while creating the File
                genericError("Could not create imageFile for camera");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (sheetView!=null)
                if (sheetView.getVisibility()!=View.GONE)
                    sheetView.setVisibility(View.GONE);
            Uri selectedImage = null;
            if (requestCode == REQUEST_LOAD_IMAGE && data != null) {
                selectedImage = data.getData();
                if (selectedImage == null) {
                    genericError();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Do something with imagePath
                selectedImage = cameraImageUri;
            }

            if (selectedImage != null) {
                showSelectedImage(selectedImage);
            } else {
                genericError();
            }
        }
    }

    private void showSelectedImage(Uri selectedImageUri) {
        mainImg.setImageDrawable(null);
        Glide.with(this)
                .load(selectedImageUri)
                .crossFade()
                .fitCenter()
                .into(mainImg);
    }

    private void genericError() {
        genericError(null);
    }

    private void genericError(String message) {
        Toast.makeText(this, message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
