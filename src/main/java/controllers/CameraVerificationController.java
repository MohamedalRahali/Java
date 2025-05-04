package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraVerificationController {
    @FXML private ImageView cameraView;
    @FXML private Button startButton;
    @FXML private Button verifyButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private ScheduledExecutorService executor;
    private VideoCapture capture;
    private boolean cameraActive = false;
    private CascadeClassifier faceCascade;
    private boolean personVerified = false;

    public void initialize() {
        try {
            // Load OpenCV
            OpenCV.loadLocally();
            
            // Initialize face detection
            faceCascade = new CascadeClassifier();
            // Extract the cascade file from resources
            InputStream is = getClass().getResourceAsStream("/haarcascades/haarcascade_frontalface_alt.xml");
            if (is == null) {
                throw new IOException("Could not load face cascade classifier");
            }
            
            // Create a temporary file for the cascade classifier
            File tempFile = File.createTempFile("cascade", ".xml");
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            
            // Load the cascade classifier
            if (!faceCascade.load(tempFile.getAbsolutePath())) {
                throw new IOException("Could not load face cascade classifier");
            }
            
            tempFile.deleteOnExit();
            
            // Don't initialize camera here, do it when the start button is pressed
            startButton.setDisable(false);
            statusLabel.setText("Cliquez sur 'Démarrer la Caméra' pour commencer");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur: Impossible d'initialiser le système de détection faciale");
            startButton.setDisable(true);
            return;
        }

        startButton.setOnAction(event -> {
            if (!cameraActive) {
                initializeCamera();
            } else {
                stopCamera();
            }
        });

        verifyButton.setOnAction(event -> verifyPerson());
        cancelButton.setOnAction(event -> closeCamera());
    }
    
    private void initializeCamera() {
        try {
            // Initialize camera
            capture = new VideoCapture(0);
            Thread.sleep(1000); // Give the camera time to initialize
            
            if (!capture.isOpened()) {
                throw new IOException("Could not open camera");
            }
            
            startCamera();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur: Impossible d'accéder à la caméra");
            startButton.setDisable(false);
        }
    }

    private void startCamera() {
        if (!cameraActive) {
            if (capture == null || !capture.isOpened()) {
                statusLabel.setText("Erreur: Caméra non disponible");
                return;
            }

            // Start the camera feed
            cameraActive = true;
            startButton.setText("Arrêter la Caméra");
            
            // Start acquisition thread
            Runnable frameGrabber = () -> {
                Mat frame = new Mat();
                while (cameraActive) {
                    try {
                        // Read frame from camera
                        if (capture.read(frame)) {
                            // Detect faces
                            MatOfRect faces = new MatOfRect();
                            faceCascade.detectMultiScale(frame, faces);
                            
                            // Draw rectangles around detected faces
                            for (Rect rect : faces.toArray()) {
                                Imgproc.rectangle(frame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 255, 0));
                            }
                            
                            // Convert frame to image
                            MatOfByte buffer = new MatOfByte();
                            Imgcodecs.imencode(".png", frame, buffer);
                            Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
                            
                            // Update UI
                            Platform.runLater(() -> {
                                cameraView.setImage(image);
                                if (faces.toArray().length > 0) {
                                    statusLabel.setText("Visage détecté");
                                    verifyButton.setDisable(false);
                                } else {
                                    statusLabel.setText("Aucun visage détecté");
                                    verifyButton.setDisable(true);
                                }
                            });
                        } else {
                            Platform.runLater(() -> statusLabel.setText("Erreur: Impossible de lire l'image de la caméra"));
                            stopCamera();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> statusLabel.setText("Erreur: Problème avec la caméra"));
                        stopCamera();
                        break;
                    }
                    
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            };

            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
        } else {
            stopCamera();
        }
    }

    private void verifyPerson() {
        if (!cameraActive) {
            statusLabel.setText("Démarrez d'abord la caméra");
            return;
        }

        Mat frame = new Mat();
        if (capture.read(frame)) {
            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayFrame, faces);

            if (faces.toArray().length > 0) {
                personVerified = true;
                statusLabel.setText("Personne vérifiée avec succès!");
                closeCamera();
            } else {
                statusLabel.setText("Aucun visage détecté. Veuillez réessayer.");
            }
        }
    }

    private void stopCamera() {
        if (cameraActive) {
            cameraActive = false;
            startButton.setText("Démarrer la Caméra");
            verifyButton.setDisable(true);
            
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            if (capture != null && capture.isOpened()) {
                capture.release();
            }
            
            // Clear the camera view
            Platform.runLater(() -> {
                cameraView.setImage(null);
                statusLabel.setText("Caméra arrêtée");
            });
        }
    }

    private void closeCamera() {
        stopCamera();
        if (capture != null) {
            capture.release();
        }
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public boolean isPersonVerified() {
        return personVerified;
    }
}
