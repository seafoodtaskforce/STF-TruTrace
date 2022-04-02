package com.wwf.shrimp.application.opennotescanner;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.CameraVisionProcessingUtils;
import com.wwf.shrimp.application.opennotescanner.helpers.OpenNoteMessage;
import com.wwf.shrimp.application.opennotescanner.helpers.PreviewFrame;
import com.wwf.shrimp.application.opennotescanner.helpers.Quadrilateral;
import com.wwf.shrimp.application.opennotescanner.helpers.ScannedDocument;
import com.wwf.shrimp.application.opennotescanner.views.HUDCanvasView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by allgood on 05/03/16.
 * Updated by AleaActaEst with
 *      - throttling and background crop and continous image capture
 *      - dog ear correction
 */
public class ImageProcessor extends Handler {

    private static final double DOG_EAR_ROUNDING_FACTOR = 1.125;

    private static final String TAG = "ImageProcessor";
    private final Handler mUiHandler;
    private final OpenNoteScannerActivity mMainActivity;
    private boolean mBugRotate;
    private boolean colorMode=true;
    private boolean filterMode=false;
    private double colorGain = 1.5;       // contrast
    private double colorBias = 0;         // bright
    private int colorThresh = 110;        // threshold
    private Size mPreviewSize;
    private Point[] mPreviewPoints;
    private ResultPoint[] qrResultPoints;

    //
    // throttle variable
    long lastThrottleTime = 0;
    long throttleTime = 200000;
    long startThrottleTime;
    boolean startOfCycle = true;
    int cycleCount = 0;

    //
    // Global Values
    SessionData globalVariable;


    public ImageProcessor (Looper looper , Handler uiHandler , OpenNoteScannerActivity mainActivity, long startThrottleTime, SessionData globalSession) {
        super(looper);
        mUiHandler = uiHandler;
        mMainActivity = mainActivity;

        this.startThrottleTime = 2*startThrottleTime;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        mBugRotate = sharedPref.getBoolean("bug_rotate",false);

        // global data
        this.globalVariable = globalSession;
    }

    public void setThrottle(){
        lastThrottleTime = System.currentTimeMillis();
        mMainActivity.getHUD().clear();
        mMainActivity.invalidateHUD();
        cycleCount = 0;
    }

    public void handleMessage ( Message msg ) {

        if (msg.obj.getClass() == OpenNoteMessage.class) {

            OpenNoteMessage obj = (OpenNoteMessage) msg.obj;

            String command = obj.getCommand();

            Log.d(TAG, "Message Received: " + command + " - " + obj.getObj().toString() );

            if ( command.equals("previewFrame")) {
                processPreviewFrame((PreviewFrame) obj.getObj());
            } else if ( command.equals("pictureTaken")) {
                processPicture((Mat) obj.getObj());
            } else if ( command.equals("colorMode")) {
                colorMode=(Boolean) obj.getObj();
            } else if ( command.equals("filterMode")) {
                filterMode=(Boolean) obj.getObj();
            }
        }
    }

    private void processPreviewFrame( PreviewFrame previewFrame ) {


        Result[] results = {};

        Mat frame = previewFrame.getFrame();


        /**
        try {
            results = zxing(frame);
        } catch (ChecksumException | FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        boolean qrOk = false;
        String currentQR=null;

        for (Result result: results) {
            String qrText = result.getText();
            if ( Utils.isMatch(qrText, "^P.. V.. S[0-9]+") && checkQR(qrText)) {
                Log.d(TAG, "QR Code valid: " + result.getText());
                qrOk = true;
                currentQR = qrText;
                qrResultPoints = result.getResultPoints();
                break;
            } else {
                Log.d(TAG, "QR Code ignored: " + result.getText());
            }
        }
        */


        boolean autoMode = previewFrame.isAutoMode();
        boolean previewOnly = previewFrame.isPreviewOnly();

        // if ( detectPreviewDocument(frame) && ( (!autoMode && !previewOnly ) || ( autoMode && qrOk ) ) ) {
        if ( detectPreviewDocument(frame)
                && ( (!autoMode && !previewOnly ))
                && !CameraVisionProcessingUtils.isPageLimitReached(globalVariable, mMainActivity)) {

            if(startOfCycle){
                startOfCycle = false;
                try {
                    Thread.sleep(startThrottleTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mMainActivity.waitSpinnerVisible();
            // check if we need to throttle the start


            mMainActivity.requestPicture();

            //
            //if (qrOk) {
            //    pageHistory.put(currentQR, new Date().getTime() / 1000);
            //    Log.d(TAG, "QR Code scanned: " + currentQR);
            //}
            //
            globalVariable.setCurrDocPagesCameraSnapCount(globalVariable.getCurrDocPagesCameraSnapCount()+1);
        }
        frame.release();

        mMainActivity.setImageProcessorBusy(false);

    }

    public void processPicture( Mat picture ) {

        long startTime = System.currentTimeMillis();
        Mat img = Imgcodecs.imdecode(picture, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        picture.release();

        Log.d(TAG, "processPicture - imported image " + img.size().width + "x" + img.size().height);

        if (mBugRotate) {
            Core.flip(img, img, 1 );
            Core.flip(img, img, 0 );
        }

        ScannedDocument doc = detectDocument(img);
        mMainActivity.saveDocument(doc);

        doc.release();
        picture.release();

        mMainActivity.setImageProcessorBusy(false);
        mMainActivity.setAttemptToFocus(false);
        mMainActivity.waitSpinnerInvisible();
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "processPicture - time span " +(endTime - startTime));


    }


    public ScannedDocument detectDocument(Mat inputRgba) {
        long startTime = System.currentTimeMillis();
        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        Log.d(TAG, "processPicture - Detecting Doc...");
        ScannedDocument sd = new ScannedDocument(inputRgba);

        Log.d(TAG, "processPicture - Detecting Doc - Finished Detecting Doc...");

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        Mat doc;

        if (quad != null) {
            Log.d(TAG, "processPicture - Detecting Doc - DOC DETECTED");
            MatOfPoint c = quad.contour;

            sd.quadrilateral = quad;
            sd.previewPoints = mPreviewPoints;
            sd.previewSize = mPreviewSize;

            doc = fourPointTransform(inputRgba, quad.points);

        } else {
            doc = new Mat( inputRgba.size() , CvType.CV_8UC4 );
            inputRgba.copyTo(doc);
        }

        // enhanceDocument(doc);


        long endTime = System.currentTimeMillis();
        Log.d(TAG, "processPicture detectDocument - Detecting Doc - time span " +(endTime - startTime));
        return sd.setProcessed(doc);
    }


    private HashMap<String,Long> pageHistory = new HashMap<>();

    private boolean checkQR(String qrCode) {

        return ! ( pageHistory.containsKey(qrCode) &&
                pageHistory.get(qrCode) > new Date().getTime()/1000-15) ;

    }

    private boolean detectPreviewDocument(Mat inputRgba) {

        /**
        if(System.currentTimeMillis() - this.lastThrottleTime <= this.throttleTime){
            Log.d(TAG,  "Document Box: Throttling " + (System.currentTimeMillis() - this.lastThrottleTime));
            return false;
        }
         */



        Log.d(TAG,  "Document Box: Detecting Preview Document");

        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        mPreviewPoints = null;
        mPreviewSize = inputRgba.size();

        if (quad != null) {

            Point[] rescaledPoints = new Point[4];

            double ratio = inputRgba.size().height / 500;

            for ( int i=0; i<4 ; i++ ) {
                int x = Double.valueOf(quad.points[i].x*ratio).intValue();
                int y = Double.valueOf(quad.points[i].y*ratio).intValue();
                if (mBugRotate) {
                    rescaledPoints[(i+2)%4] = new Point( Math.abs(x- mPreviewSize.width), Math.abs(y- mPreviewSize.height));
                } else {
                    rescaledPoints[i] = new Point(x, y);
                }
            }

            mPreviewPoints = rescaledPoints;

            Log.d(TAG,  "Document Box: Drawing");
            drawDocumentBox(mPreviewPoints, mPreviewSize);

            Log.d(TAG, "Document Box: Quad Points "
                                + quad.points[0].toString()
                                + " , " + quad.points[1].toString()
                                + " , " + quad.points[2].toString()
                                + " , " + quad.points[3].toString());

            return true;

        }

        mMainActivity.getHUD().clear();
        mMainActivity.invalidateHUD();

        return false;

    }

    private void drawDocumentBox(Point[] points, Size stdSize) {

        Path path = new Path();

        HUDCanvasView hud = mMainActivity.getHUD();

        // ATTENTION: axis are swapped

        float previewWidth = (float) stdSize.height;
        float previewHeight = (float) stdSize.width;

        path.moveTo( previewWidth - (float) points[0].y, (float) points[0].x );
        path.lineTo( previewWidth - (float) points[1].y, (float) points[1].x );
        path.lineTo( previewWidth - (float) points[2].y, (float) points[2].x );
        path.lineTo( previewWidth - (float) points[3].y, (float) points[3].x );
        path.close();

        PathShape newBox = new PathShape(path , previewWidth , previewHeight);

        Paint paint = new Paint();
        paint.setColor(Color.argb(64, 0, 255, 0));

        Paint border = new Paint();
        border.setColor(Color.rgb(0, 255, 0));
        border.setStrokeWidth(5);

        hud.clear();
        hud.addShape(newBox, paint, border);
        mMainActivity.invalidateHUD();


    }

    private Quadrilateral getQuadrilateral( List<MatOfPoint> contours , Size srcSize ) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width,height);
        Log.v(TAG, "Crop Size: " + width + " x " + height);


        for ( MatOfPoint c: contours ) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            // approximate the perimeter of the polygon
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);


            // get all the points of the polygon
            Point[] points = approx.toArray();
            //Point[] points = c2f.toArray();

            //Convert back to MatOfPoint
            MatOfPoint polyPoints = new MatOfPoint( points );

            // get bounding rect of contour
            // but only if the number of poly-points is < 6
            if(polyPoints.toArray().length < 6) {
                Rect rect = Imgproc.boundingRect(polyPoints);
                double areaBeforeApprox = Imgproc.contourArea(approx);
                double areaBoundedRect = rect.area();
                if(areaBeforeApprox*DOG_EAR_ROUNDING_FACTOR > areaBoundedRect) {
                    Point[] boundedRectPoints = com.wwf.shrimp.application.opennotescanner.helpers.Utils.convertRectToPointArray(rect);

                    // select biggest 4 angles polygon from the rounded shapes
                    if (boundedRectPoints.length == 4) {
                        // if (points.length == 4) {
                        // Point[] foundPoints = sortPoints(points);
                        Point[] foundPoints = sortPoints(boundedRectPoints);
                        if (insideArea(foundPoints, size)) {
                            return new Quadrilateral(c, foundPoints);
                        }
                    }
                }
            }
        }

        /**

        for ( MatOfPoint c: contours ) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            // select biggest 4 angles polygon
            if (points.length == 4) {
                Point[] foundPoints = sortPoints(points);

                if (insideArea(foundPoints, size)) {
                    return new Quadrilateral( c , foundPoints );
                }
            }
        }
        */
        return null;
    }

    private Point[] sortPoints( Point[] src ) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null , null , null , null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal difference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal difference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    /**
     * Sort the crop points for the background crop
     * @param src - the list of points to sort
     * @return - the sorted list
     */
    private Point[] sortCropPoints( List<Point> src ) {
        Point[] resultPoints = { null , null , null , null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        resultPoints[0] = Collections.min(src, sumComparator);

        // bottom-right corner = maximal sum
        resultPoints[2] = Collections.max(src, sumComparator);

        // top-right corner = minimal diference
        resultPoints[1] = Collections.min(src, diffComparator);

        // bottom-left corner = maximal diference
        resultPoints[3] = Collections.max(src, diffComparator);

        return resultPoints ;
    }

    private boolean insideArea(Point[] rp, Size size) {

        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();
        int baseMeasure = height/4;

        int bottomPos = height-baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width/2-baseMeasure;
        int rightPos = width/2+baseMeasure;

        return (
                rp[0].x <= leftPos && rp[0].y <= topPos
                        && rp[1].x >= rightPos && rp[1].y <= topPos
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos

        );
    }

    private void enhanceDocument( Mat src ) {
        if (colorMode && filterMode) {
            src.convertTo(src,-1, colorGain , colorBias);
            Mat mask = new Mat(src.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(src,mask,Imgproc.COLOR_RGBA2GRAY);

            Mat copy = new Mat(src.size(), CvType.CV_8UC3);
            src.copyTo(copy);

            Imgproc.adaptiveThreshold(mask,mask,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,15,15);

            src.setTo(new Scalar(255,255,255));
            copy.copyTo(src,mask);

            copy.release();
            mask.release();

            // special color threshold algorithm
            colorThresh(src,colorThresh);
        } else if (!colorMode) {
            Imgproc.cvtColor(src,src,Imgproc.COLOR_RGBA2GRAY);
            if (filterMode) {
                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
            }
        }
    }

    /**
     * When a pixel have any of its three elements above the threshold
     * value and the average of the three values are less than 80% of the
     * higher one, brings all three values to the max possible keeping
     * the relation between them, any absolute white keeps the value, all
     * others go to absolute black.
     *
     * src must be a 3 channel image with 8 bits per channel
     *
     * @param src
     * @param threshold
     */
    private void colorThresh(Mat src, int threshold) {
        Size srcSize = src.size();
        int size = (int) (srcSize.height * srcSize.width)*3;
        byte[] d = new byte[size];
        src.get(0,0,d);

        for (int i=0; i < size; i+=3) {

            // the "& 0xff" operations are needed to convert the signed byte to double

            // avoid unneeded work
            if ( (double) (d[i] & 0xff) == 255 ) {
                continue;
            }

            double max = Math.max(Math.max((double) (d[i] & 0xff), (double) (d[i + 1] & 0xff)),
                    (double) (d[i + 2] & 0xff));
            double mean = ((double) (d[i] & 0xff) + (double) (d[i + 1] & 0xff)
                    + (double) (d[i + 2] & 0xff)) / 3;

            if (max > threshold && mean < max * 0.8) {
                d[i] = (byte) ((double) (d[i] & 0xff) * 255 / max);
                d[i + 1] = (byte) ((double) (d[i + 1] & 0xff) * 255 / max);
                d[i + 2] = (byte) ((double) (d[i + 2] & 0xff) * 255 / max);
            } else {
                d[i] = d[i + 1] = d[i + 2] = 0;
            }
        }
        src.put(0,0,d);
    }

    private Mat fourPointTransform( Mat src , Point[] pts ) {

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB)*ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB)*ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x*ratio, tl.y*ratio, tr.x*ratio, tr.y*ratio, br.x*ratio, br.y*ratio, bl.x*ratio, bl.y*ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }

    private ArrayList<MatOfPoint> findContours(Mat src) {

        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width,height);
        Log.d(TAG, "processPicture Find Contours Size: " + height + " - " + width );

        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src,resizedImage,size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 75, 200);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs));
            }
        });

        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    private QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();



    public Result[] zxing( Mat inputImage ) throws ChecksumException, FormatException {

        int w = inputImage.width();
        int h = inputImage.height();

        Mat southEast;

        if (mBugRotate) {
            southEast = inputImage.submat(h-h/4 , h , 0 , w/2 - h/4 );
        } else {
            southEast = inputImage.submat(0, h / 4, w / 2 + h / 4, w);
        }

        Bitmap bMap = Bitmap.createBitmap(southEast.width(), southEast.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(southEast, bMap);
        southEast.release();
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result[] results = {};
        try {
            results = qrCodeMultiReader.decodeMultiple(bitmap);
        }
        catch (NotFoundException e) {
        }

        return results;

    }

    public void setBugRotate(boolean bugRotate) {
        mBugRotate = bugRotate;
    }

    /**
     * Private helper methods
     */

    /**
     * Alternative way of finding the document outline
     * @param src - the source matrix image data
     * @param fileName - the name of the file that originated the src image
     * @return - true of rectangle was found (and cropped); false otherwise
     * @throws Exception
     */
    public boolean findRectangle(Mat src, String fileName) throws Exception {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;
        MatOfPoint2f approxMaxCurve=null;


        double maxArea = 0;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                // Find the contours in the image
                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                // walk through all found contours and isolate the one most likely to be a rectangle
                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    // if we have a 4 corner contour compare its area to the largest found so far
                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                            approxMaxCurve = approxCurve;
                        }
                    }
                }
            }
        }

        //
        // if we have found a rectangle that outlines our potential document.
        // we process the image for deskewing and cropping
        //
        if (maxId >= 0) {

            // get the quad and sort it properly
            Point[] foundPoints = sortCropPoints(approxMaxCurve.toList());
            // attempt a crop of the rectangle out of the image as well as deskew the image
            //if it is in any way skewed off the 90 degree angle
            Mat doc = fourCropPointTransform(src, foundPoints);
            // create the new cropped document
            Mat endDoc = new Mat(Double.valueOf(doc.size().width).intValue(),
                    Double.valueOf(doc.size().height).intValue(), CvType.CV_8UC4);

            // flip the doc orientation
            Core.flip(doc.t(), endDoc, 1);

            // Process
            // Imgproc.drawContours(src, contours, maxId, new Scalar(255, 255, 150), 25);

            Bitmap processedImage = Bitmap.createBitmap(doc.cols(),
                    doc.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(doc, processedImage);

            Log.v(TAG, "Image PATH: " + fileName);
            File destFile = new File(fileName);
            destFile.delete();
            destFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(destFile);
            processedImage.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            // extract the OCR data

            outputStream.flush();
            outputStream.close();

            Log.d(TAG, "processPicture Find Rectangle <SUCCESS>: " + maxArea);

            // clean up
            doc.release();
            endDoc.release();
            blurred.release();
            gray0.release();
            src.release();
            processedImage.recycle();

            return true;

        }

        return false;
    }

    private double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private Quadrilateral getCropQuadrilateral( List<MatOfPoint> contours , Size srcSize ) {

        double ratio = 1;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width,height);

        for ( MatOfPoint c: contours ) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            // select biggest 4 angles polygon
            if (points.length == 4) {
                Point[] foundPoints = sortPoints(points);

                if (insideArea(foundPoints, size)) {
                    return new Quadrilateral( c , foundPoints );
                }
            }
        }

        return null;
    }

    /**
     *
     * @param src
     * @param pts
     * @return
     */
    private Mat fourCropPointTransform( Mat src , Point[] pts ) {

        double ratio = 1;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB)*ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB)*ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x*ratio, tl.y*ratio, tr.x*ratio, tr.y*ratio, br.x*ratio, br.y*ratio, bl.x*ratio, bl.y*ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }
}
