package edavletshin.com.vkpostpublisher;

import android.graphics.Bitmap;

public class Global {

  public static void clear(){
    img = null;
  }

  public static Bitmap getImg() {
    return img;
  }

  public static void setImg(Bitmap img) {
    Global.img = img;
  }

  protected static Bitmap img;

}