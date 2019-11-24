/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacv.face.detection;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.javacv.common.ImageSupplier;
import org.javacv.face.recognition.Predictable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static java.util.Collections.singletonList;
import static java.util.Optional.*;
import static org.bytedeco.javacpp.opencv_highgui.CV_FONT_NORMAL;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

/**
 * A detector for faces in an image.
 *
 * @author spindizzy
 */
public class Detector {

    private static final Logger LOG = LoggerFactory.getLogger(Detector.class);

    /**
     * Default cascade file. For others see the resource folder.
     */
    private static final String CASCADE_XML = "haarcascade_frontalface_alt_tree.xml";

    private final Scalar color;

    private final List<CascadeClassifier> classifiers;

    private Optional<Predictable<String>> prediction;

    private OpenCVFrameConverter.ToMat converterToMat;

    public Detector() {
        this(singletonList(CASCADE_XML));
    }

    public Detector(List<String> cascades) {
        this.prediction = empty();

        this.color = new Scalar(CvScalar.GREEN);
        this.converterToMat = new OpenCVFrameConverter.ToMat();
        this.classifiers = cascades.stream().map(ClassifierFactory.Instance::create)
                .filter(Optional::isPresent).map(Optional::get).collect(toList());
    }

    public void setPrediction(Predictable<String> prediction) {
        this.prediction = of(prediction);
    }

    public boolean hasFace(ImageSupplier provider) {
        return countFaces(provider) > 0;
    }

    public long countFaces(ImageSupplier provider) {
        final RectVector rect = findFaces(provider.get());
        return rect.size();
    }

    /**
     * Extracts all faces from the source image and saves it to a new folder.
     *
     * @param provider object which provides the image
     * @param targetFolder folder with extracted images
     * @return <code>true</code> if any face was extracted and saved in the
     * folder
     */
    public boolean extractFaces(ImageSupplier provider, File targetFolder) {
        Mat image = provider.get();
        RectVector rect = findFaces(image);
        long limit = rect.limit();
        for (int i = 0; i < limit; i++) {
            Mat face = extractFace(rect, i, image);
            saveFace(targetFolder, face, i);
        }
        return limit > 0;
    }

    private Mat extractFace(RectVector rect, int position, Mat image) {
        Rect pos = rect.get(position);
        return image.apply(pos);
    }

    private void saveFace(File targetFolder, Mat face, int position) {
        String name = position + ".jpg";
        File f = new File(targetFolder, name);
        imwrite(f.getPath(), face);
    }

    /**
     * Marks all faces which were found and save the result to a new image.
     *
     * @param provider object which provides the image
     * @param targetFile file with the marked faces
     * @return <code>true</code> if any face was marked and saved to the image
     */
    public boolean saveMarkedFaces(ImageSupplier provider, File targetFile) {
        Mat image = provider.get();
        long limit = markFaces(image);
        if (limit > 0) {
            imwrite(targetFile.getPath(), image);
        }
        return limit > 0;
    }
    
    public long markFaces(Frame img){
        return markFaces(converterToMat.convert(img));
    }

    /**
     * Marks the faces on the image.
     *
     * @param image
     * @return number of detected faces.
     */
    public long markFaces(Mat image) {
        RectVector rect = findFaces(image);
        long size = rect.size();
        for (int i = 0; i < size; i++) {
            Rect pos = rect.get(i);
            rectangle(image, pos, color);
            predict(image, pos);
        }
        return size;
    }

    private void predict(Mat image, Rect pos) {
        prediction.ifPresentOrElse(pred -> {
            //crop out the face
            Mat face = image.apply(pos);
            Point point = new Point(pos.x(), pos.y() - 3);
            putText(image, pred.predict(face), point, CV_FONT_NORMAL, 0.5, color);
        }, () -> LOG.info("No predictor set!"));
    }

    private RectVector findFaces(Mat image) {
        RectVector rect = new RectVector();
        for(CascadeClassifier classifier : classifiers) {
            classifier.detectMultiScale(image, rect);
            if (!rect.empty()) {
                break;
            }
        }
        return rect;
    }
}
