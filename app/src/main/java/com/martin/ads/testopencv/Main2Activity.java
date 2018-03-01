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
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends Activity implements View.OnTouchListener {
    //    public static final String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "box.jpg";
    public static final String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tencent" + File.separator + "QQ_Images" + File.separator
            + "realbaby.jpg";
    public static final String TAG = "Main2Activity";
    ImageView imageView;
    private Bitmap bitmap;
    Bitmap b;
    private float s = 0;
    int radius = 10;
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
//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timg);
        bitmap = BitmapFactory.decodeFile(imgPath);
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
//        firstMask.setTo(new Scalar(GC_PR_BGD));//扣前景
        firstMask.setTo(new Scalar(GC_PR_FGD));//扣背景

        img = new Mat();

        Utils.bitmapToMat(bitmap, img);
    }

    /**
     * 线程执行
     *
     * @param view
     */
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

    /**
     * 添加种子点
     *
     * @param p
     */

    private void setLblsInMask(Point p) {
        count++;

//        Imgproc.circle(firstMask, p, radius, new Scalar(GC_FGD),0);
        Imgproc.circle(firstMask, p, radius, new Scalar(GC_BGD), 0);

    }

    /**
     * 重置
     *
     * @param view
     */
    public void onReset(View view) {

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timg);
        bitmap = regularBitmap(bitmap);
        Log.e(TAG, " 提取bitmap bitmap.getWidth() = " + bitmap.getWidth() + "  bitmap.getHeight() = " + bitmap.getHeight());
        init(bitmap);
    }

    /**
     * 决定是你了
     */
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
        Log.e(TAG, "描点个数为 ： " + count + "个");
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
        count = 0;

        imageView.setImageBitmap(b);
    }

    /**
     * 白色背景变为透明色
     *
     * @return
     */
    private Bitmap change2Transparent() {
        String s = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "base.jpg";
        String s1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "logo.jpg";
        Mat source = new Mat();
        Mat source1 = new Mat();
        Bitmap bitmap = BitmapFactory.decodeFile(s);
        Bitmap bitmap1 = BitmapFactory.decodeFile(s1);
        Utils.bitmapToMat(bitmap, source);
        Utils.bitmapToMat(bitmap1, source1);
        System.out.println("加载bitmap    ： bitmap.getWidth() = " + bitmap.getWidth() + "bitmap1.getWidth() = " + bitmap1.getWidth());
        Mat destination = source.clone();

        // to make the white region transparent
        Mat mask2 = new Mat();
        Mat dst = new Mat();


        Imgproc.cvtColor(source1, mask2, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask2, mask2, 230, 255, Imgproc.THRESH_BINARY_INV);
        List<Mat> planes = new ArrayList<Mat>();
        List<Mat> result = new ArrayList<Mat>();
        Mat result1 = new Mat();
        Mat result2 = new Mat();
        Mat result3 = new Mat();


        Core.split(source1, planes);

        Core.bitwise_and(planes.get(0), mask2, result1);
        Core.bitwise_and(planes.get(1), mask2, result2);
        Core.bitwise_and(planes.get(2), mask2, result3);

        result.add(result1);
        result.add(result2);
        result.add(result3);
        Core.merge(result, dst);
        //以上白色变透明


        //再把小图copy到大图
        Rect roi = new Rect(50, 50, 90, 62);//不能比原图大,及小
        Mat destinationROI = source.submat(roi);
        dst.copyTo(destinationROI, dst);

        System.out.println("生成 mat ： " + " dst.size().width = " + dst.size().width + " dst.size().height = " + dst.size().height);

        Utils.matToBitmap(dst, bitmap1);
        return bitmap;
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

    int count = 0;

    /**
     * 保存bitmap
     *
     * @param view
     */
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
