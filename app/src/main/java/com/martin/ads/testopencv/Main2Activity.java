package com.martin.ads.testopencv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main2Activity extends Activity implements View.OnTouchListener {
    public static final String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "temp.jpg";
    public static final String TAG = "Main2Activity";
    ImageView imageView;
    private Bitmap bitmap;
    Bitmap b;
    private float s = 0;
    int radius = 5;
    int thickness = -1;
    Mat img;
    Mat firstMask;
    double GC_BGD = 0,  //!< an obvious background pixels
            GC_FGD = 1,  //!< an obvious foreground (object) pixel
            GC_PR_BGD = 2,  //!< a possible background pixel
            GC_PR_FGD = 3;

    Canvas canvas;
    Paint paint;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("opencv_java");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView = findViewById(R.id.img);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timg);
        bitmap = regularBitmap(bitmap);
        Log.e(TAG, " 提取bitmap bitmap.getWidth() = " + bitmap.getWidth() + "  bitmap.getHeight() = " + bitmap.getHeight());
        init(bitmap);

        imageView.setOnTouchListener(this);
    }

    /**
     * 规范bitmap的大小
     *
     * @param bitmap
     * @return
     */
    private Bitmap regularBitmap(Bitmap bitmap) {
        double scale = (double) bitmap.getWidth() / (double) bitmap.getHeight();

        double w;
        double h;

        if (scale > 1) {
            w = 800;
            h = w / scale;
        } else {
            h = 800;
            w = h * scale;
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) w, (int) h, true);
        scaledBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
        return scaledBitmap;
    }

    private void init(Bitmap bitmap) {

        imageView.setImageBitmap(bitmap);

        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.BLUE);
        firstMask = new Mat();

        Size size = new Size(bitmap.getWidth(), bitmap.getHeight());
        firstMask.create(size, CvType.CV_8UC1);
        firstMask.setTo(new Scalar(GC_PR_FGD));

        img = new Mat();

        Utils.bitmapToMat(bitmap, img);
    }


    public void onGrabCut(View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        grabcut();
                    }
                });
            }
        };
        thread.start();
    }

    private void setLblsInMask(Point p) {

        Imgproc.circle(firstMask, p, radius, new Scalar(GC_BGD), thickness);

    }

    public void onReset(View view) {

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timg);
        bitmap = regularBitmap(bitmap);
        Log.e(TAG, " 提取bitmap bitmap.getWidth() = " + bitmap.getWidth() + "  bitmap.getHeight() = " + bitmap.getHeight());
        init(bitmap);
    }

    private void grabcut() {

        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);

        //设置抠图范围的左上角和右下角
        int r = img.rows();
        int c = img.cols();
        Point p1 = new Point(10, 10);
        Point p2 = new Point(c - 10, r - 10);
        Rect rect = new Rect(p1, p2);

        //生成遮板
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        long time = System.currentTimeMillis();
        Log.e(TAG, "开始grabcut。。。。。。。");
        Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 1, Imgproc.GC_INIT_WITH_MASK);
        Log.e(TAG, "结束grabcut。。。。。。。执行时间为 ： " + ((System.currentTimeMillis() - time) / 1000) + "s");

        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

        //抠图
        Log.e(TAG, "取出前景。。。。");
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255, 255));
        img.copyTo(foreground, firstMask);
        //mat->bitmap
        b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(foreground, b);

        init(b);

        imageView.setImageBitmap(b);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (s == 0) {
            s = imageView.getWidth() * 1.0f / bitmap.getWidth();
            System.out.println(" imageView.getWidth() = " + imageView.getWidth() + " bitmap.getWidth() = " + bitmap.getWidth() + " s = " + s);
        }
        int x = (int) (event.getX() / s);
        int y = (int) (event.getY() / s);
        int type = event.getAction();
        switch (type) {
            case MotionEvent.ACTION_DOWN:
                setLblsInMask(new Point(x, y));
                canvas.drawCircle(x, y, radius, paint);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                setLblsInMask(new Point(x, y));
                break;
            case MotionEvent.ACTION_CANCEL:
                setLblsInMask(new Point(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                setLblsInMask(new Point(x, y));
                canvas.drawCircle(x, y, radius, paint);
                imageView.invalidate();
                break;
        }
        return true;
    }

    public void saveBitmap(View view) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.png";
        File file = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 10, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            System.out.println("保存成功 path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
