package com.example.clientSide.fxmlController;

import com.example.clientSide.helper.Helper;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AttendanceController implements Initializable {
    public AnchorPane page;
    public Label statusMessage;
    public ImageView imageView;
    private Mat frame;
    private CascadeClassifier faceDetector;
    private Helper helper = new Helper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load the Haar cascade file for face detection
        String xmlFile = "/home/omotola/opencv/data/haarcascades/haarcascade_frontalface_alt.xml";
        faceDetector = new CascadeClassifier(xmlFile);
        if (faceDetector.empty()) {
            System.err.println("Failed to load Haar cascade file for face detection.");
            return;
        }
        videoCapture();
    }

    private void videoCapture() {
        VideoCapture capture = new VideoCapture(0);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, imageView.getFitWidth());
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, imageView.getFitHeight());
        if (!capture.isOpened()) {
            System.err.println("Failed to open the camera");
            return;
        }

        Thread videoThread = new Thread(() -> {
            frame = new Mat();
            while (capture.read(frame)) {
                if (!frame.empty()) {
                    detectAndDrawFaces(frame); // Detect faces and draw rectangles
                    Image image = matToImage(frame);
                    Platform.runLater(() -> imageView.setImage(image));
                }
            }
            capture.release();
        });
        videoThread.setDaemon(true);
        videoThread.start();
        log.info("started");
    }

    private void detectAndDrawFaces(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame,
                    new org.opencv.core.Point(rect.x, rect.y),
                    new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }

    private Image matToImage(Mat frame) {
        BufferedImage bufferedImage = new BufferedImage(frame.width(), frame.height(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        frame.get(0, 0, data);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public void markAttendance(ActionEvent event) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, matOfByte);
        byte[] imBytes = matOfByte.toArray();
        helper.markAttendance(imBytes, "", "");
    }
}
