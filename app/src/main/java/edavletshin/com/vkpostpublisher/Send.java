package edavletshin.com.vkpostpublisher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;

public class Send extends AppCompatActivity {

    String photo_id = "";
    Button cancelandrepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        Bitmap bmp = Global.getImg();
        final VKRequest uploadRequest = VKApi.uploadWallPhotoRequest(new VKUploadImage(bmp, VKImageParameters.pngImage()),0,0);
        cancelandrepeat = (Button) findViewById(R.id.btncancel_repeat);
        cancelandrepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelandrepeat.getText().equals(getResources().getString(R.string.cancel))){
                    uploadRequest.cancel();
                    finish();
                } else {
                    finish();
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelandrepeat.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(Send.this, R.anim.fadeinup);
                cancelandrepeat.startAnimation(animation);
            }
        }, 1500);
        uploadRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    JSONArray uploadResponse = response.json.getJSONArray("response");

                    String owner_id = uploadResponse.getJSONObject(0).getString("owner_id");
                    String media_id = uploadResponse.getJSONObject(0).getString("id");


                    photo_id += ","+"photo" + owner_id + "_" + media_id;


                    VKRequest vkRequest = (VKApi.wall().post(VKParameters.from("attachments",photo_id)));
                    vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            Global.clear();
                            findViewById(R.id.progressbar).setVisibility(View.GONE);
                            ((TextView)findViewById(R.id.pubtext)).setText(getResources().getString(R.string.publicated));
                            findViewById(R.id.done).setVisibility(View.VISIBLE);
                            findViewById(R.id.animfortick).setVisibility(View.VISIBLE);
                            Animation animation = AnimationUtils.loadAnimation(Send.this, R.anim.moveright);
                            findViewById(R.id.animfortick).startAnimation(animation);
                            findViewById(R.id.background).setVisibility(View.VISIBLE);
                            cancelandrepeat.setBackground(getResources().getDrawable(R.drawable.bacground_for_sendbtn));
                            cancelandrepeat.setText(getResources().getString(R.string.repeat));
                            cancelandrepeat.setTextColor(Color.parseColor("#ffffff"));
                        }

                        @Override
                        public void onError(VKError error) {

                        }
                    });

                } catch (JSONException e) {
                }
            }

            @Override
            public void onError(VKError error) {
            }
        });
    }
}
