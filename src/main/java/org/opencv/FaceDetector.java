package org.opencv;

import static com.google.common.collect.Lists.newArrayList;
import java.io.File;
import java.net.URL;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author spindizzy
 */
public class FaceDetector {

    private static final Logger LOG = LoggerFactory.getLogger(FaceDetector.class);

    protected static final Scalar COLOR = new Scalar(0, 255, 0);

    private static final List<String> CLASSIFIER_FILES = newArrayList(
            //        "haarcascade_eye.xml",
            //        "haarcascade_eye_tree_eyeglasses.xml",
            //        "haarcascade_frontalface_alt.xml",
            //        "haarcascade_frontalface_alt2.xml",
            "haarcascade_frontalface_alt_tree.xml",
            //        "haarcascade_frontalface_default.xml",
            "haarcascade_fullbody.xml",
            "haarcascade_profileface.xml"
    //        "haarcascade_upperbody.xml"
    );

    public FaceDetector() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
     public void markFaces(Mat image) {
        MatOfRect faceRect = findFace(image);
        if (!faceRect.empty()) {
            faceRect.toList().forEach((Rect rect) -> {
                Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), COLOR);
            });
        }
    }


    public MatOfRect findFace(Mat image) {
        MatOfRect faceRect = new MatOfRect();

        if (!image.empty()) {
            CLASSIFIER_FILES.forEach((cf) -> {
                if (faceRect.empty()) {
                    URL resource = getClass().getResource(cf);
                    File cFile = new File(resource.getPath());
                    String cPath = cFile.getPath();

                    CascadeClassifier classifier = new CascadeClassifier(cPath);
                    classifier.detectMultiScale(image, faceRect);

                    if (LOG.isDebugEnabled() && !faceRect.empty()) {
                        LOG.debug("Detected faces: {}, with: {}", faceRect.toList().size(), cf);
                    }
                }
            });
        }

        return faceRect;
    }

}
