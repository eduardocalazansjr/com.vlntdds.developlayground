package com.vlntdds.developlayground.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.vlntdds.developlayground.R;
import com.vlntdds.developlayground.permissions.RuntimePermissionsActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Eduardo C. on 10/05/2016
 * Edit Fragment to let the user draw on the picture and save/discard
 */

@SuppressWarnings({"deprecation", "SuspiciousNameCombination"})
public class PhotoEditFragment extends Fragment {

    @BindView(R.id.photoView) PhotoEditView photoView;
    @BindView(R.id.top_border) View top_border;
    @BindView(R.id.btn_save) ImageButton btn_save;
    @BindView(R.id.btn_cancel) ImageView btn_cancel;

    public static Fragment newInstance(byte[] bitmapByteArray, int rotation, CameraFragment.ImageParameters parameters) {
        Fragment f = new PhotoEditFragment();
        Bundle args = new Bundle();
        args.putByteArray("photo_array", bitmapByteArray);
        args.putInt("photo_rotation", rotation);
        args.putParcelable("photo_info", parameters);
        f.setArguments(args);
        return f;
    }

    public PhotoEditFragment() {}

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.camera_editfragment, c, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);
        ButterKnife.bind(this.getActivity());

        int rotation = getArguments().getInt("photo_rotation");
        byte[] data = getArguments().getByteArray("photo_array");
        CameraFragment.ImageParameters params = getArguments().getParcelable("photo_info");
        if (params == null) return;

        params.Portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (params.Portrait)
            top_border.getLayoutParams().height = params.CoverHeight;
        else
            top_border.getLayoutParams().width = params.CoverWidth;

        rotatePhoto(rotation, data);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoto();
            }
        });
    }

    /* Check Permissions before save photo */
    private void savePhoto() {
        RuntimePermissionsActivity.startActivity(PhotoEditFragment.this, 1, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onActivityResult(int req, int res, Intent d) {
        if (Activity.RESULT_OK != res) return;

        if (req == 1 && d != null) {
            View v = getView();
            if (d.getBooleanExtra(RuntimePermissionsActivity.REQUESTED_PERMISSION, false) && v != null)
            {
                Bitmap b = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
                Uri photoUri = savePicture(getActivity(), b);
                ((CameraActivity) getActivity()).returnPhotoUri(photoUri);
            }
        } else {
            super.onActivityResult(req, res, d);
        }
    }

    public static Uri savePicture(Context context, Bitmap bitmap) {
        int cropHeight;
        if (bitmap.getHeight() > bitmap.getWidth()) cropHeight = bitmap.getWidth();
        else                                        cropHeight = bitmap.getHeight();

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, cropHeight, cropHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                ""
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(
                mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg"
        );

        // Saving the bitmap
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            FileOutputStream stream = new FileOutputStream(mediaFile);
            stream.write(out.toByteArray());
            stream.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // Mediascanner need to scan for the image saved
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(mediaFile);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);

        return fileContentUri;
    }

    private void rotatePhoto(int r, byte[] data) {
        Bitmap b = decodeSampledBitmapFromByte(getActivity(), data);
        DisplayMetrics display = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(display);
        float factor;

        if (r != 0) {
            Bitmap b2 = b;

            Matrix m = new Matrix();
            m.postRotate(r);
            b = Bitmap.createBitmap(b2, 0, 0, b2.getWidth(), b2.getHeight(), m, false);
            b2.recycle();
        }

        if (b.getWidth() > b.getHeight())
            factor = display.heightPixels / (float) b.getHeight();
        else
            factor = display.widthPixels / (float) b.getWidth();

        b = Bitmap.createScaledBitmap(b, (int)(b.getWidth() * factor),(int)(b.getHeight() * factor), false);
        photoView.mBitmap = b;
        photoView.setImageBitmap(b);
        photoView.mCanvas = new Canvas(b);
    }

    private static Bitmap decodeSampledBitmapFromByte(Context context, byte[] bitmapBytes) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int reqWidth, reqHeight;
        Point point = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(point);
            reqWidth = point.x;
            reqHeight = point.y;
        } else {
            reqWidth = display.getWidth();
            reqHeight = display.getHeight();
        }


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inMutable = true;
        options.inBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inScaled = true;
        options.inDensity = options.outWidth;
        options.inTargetDensity = reqWidth * options.inSampleSize;
        options.inJustDecodeBounds = false; // If set to true, the decoder will return null (no bitmap), but the out... fields will still be set, allowing the caller to query the bitmap without having to allocate the memory for its pixels.
        options.inPurgeable = true;         // Tell to gc that whether it needs free memory, the Bitmap can be cleared
        options.inInputShareable = true;    // Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future

        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int initialInSampleSize = computeInitialSampleSize(options, reqWidth, reqHeight);

        int roundedInSampleSize;
        if (initialInSampleSize <= 8) {
            roundedInSampleSize = 1;
            while (roundedInSampleSize < initialInSampleSize) {
                // Shift one bit to left
                roundedInSampleSize <<= 1;
            }
        } else {
            roundedInSampleSize = (initialInSampleSize + 7) / 8 * 8;
        }

        return roundedInSampleSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final double height = options.outHeight;
        final double width = options.outWidth;

        final long maxNumOfPixels = reqWidth * reqHeight;
        final int minSideLength = Math.min(reqHeight, reqWidth);

        int lowerBound = (maxNumOfPixels < 0) ? 1 :
                (int) Math.ceil(Math.sqrt(width * height / maxNumOfPixels));
        int upperBound = (minSideLength < 0) ? 128 :
                (int) Math.min(Math.floor(width / minSideLength),
                        Math.floor(height / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if (maxNumOfPixels < 0 && minSideLength < 0) {
            return 1;
        } else if (minSideLength < 0) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
