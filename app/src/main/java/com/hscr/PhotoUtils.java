package com.hscr;

import android.app.Activity;
import android.content.Intent;

import com.lling.photopicker.PhotoPickerActivity;

/**
 * Created by yhr on 2016-12-24 0024.
 *
 */

public class PhotoUtils {

    public static void getIntentForPicker(Activity context, int max, int requestCode, boolean showCamera){
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, showCamera);
        intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_MULTI);
        intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, max);
        context.startActivityForResult(intent, requestCode);
    }


}
