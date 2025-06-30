package org.integratedmodelling.klab.ide.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A JavaFX component that provides a drag-and-drop upload box for files and URLs.
 * Features:
 * - Gray background with dashed, curved borders
 * - Configurable prompt text
 * - Progress bar during upload
 * - Error handling and display
 * - Callback on successful upload
 */
public class UploadBox extends StackPane {

    private final String targetDirectory;
    private final String promptText;
    private final Consumer<File> onUploadComplete;
    private final BiConsumer<String, Throwable> onError;

    private Label promptLabel;
    private Label statusLabel;
    private ProgressBar progressBar;
    private VBox contentBox;
    private ExecutorService executorService;

    // CSS styles
    private static final String DEFAULT_STYLE =
            "-fx-background-color: #f5f5f5;" +
                    "-fx-border-color: #999999;" +
                    "-fx-border-width: 3;" +
                    "-fx-border-style: dashed;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-min-height: 150;" +
                    "-fx-min-width: 300;";

    private static final String DRAG_OVER_STYLE =
            "-fx-background-color: #e8f4fd;" +
                    "-fx-border-color: #2196F3;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-style: dashed;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-min-height: 150;" +
                    "-fx-min-width: 300;";

    private static final String ERROR_STYLE =
            "-fx-background-color: #ffebee;" +
                    "-fx-border-color: #f44336;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-style: dashed;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-min-height: 150;" +
                    "-fx-min-width: 300;";

    /**
     * Creates a new UploadBox component.
     *
     * @param targetDirectory The directory where uploaded files will be copied
     * @param promptText The text to display in the center of the box
     * @param onUploadComplete Callback invoked when upload completes successfully
     * @param onError Callback invoked when an error occurs (message, exception)
     */
    public UploadBox(String targetDirectory, String promptText,
                     Consumer<File> onUploadComplete,
                     BiConsumer<String, Throwable> onError) {
        this.targetDirectory = targetDirectory;
        this.promptText = promptText;
        this.onUploadComplete = onUploadComplete;
        this.onError = onError;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "UploadBox-Worker");
            t.setDaemon(true);
            return t;
        });

        initializeComponent();
        setupDragAndDrop();
    }

    private void initializeComponent() {
        // Create content elements
        promptLabel = new Label(promptText);
        promptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        promptLabel.setWrapText(true);
        promptLabel.setMaxWidth(250);
        promptLabel.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");
        statusLabel.setVisible(false);

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);

        // Create main content container
        contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        contentBox.getChildren().addAll(promptLabel, statusLabel, progressBar);

        getChildren().add(contentBox);
        setStyle(DEFAULT_STYLE);

        // Ensure target directory exists
        try {
            Files.createDirectories(Paths.get(targetDirectory));
        } catch (IOException e) {
            showError("Failed to create target directory", e);
        }
    }

    private void setupDragAndDrop() {
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragEntered(e -> setStyle(DRAG_OVER_STYLE));
        setOnDragExited(e -> resetStyle());
    }

    private void handleDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles() || dragboard.hasUrl() || dragboard.hasString()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;

        try {
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                if (!files.isEmpty()) {
                    uploadFile(files.get(0));
                    success = true;
                }
            } else if (dragboard.hasUrl()) {
                String urlString = dragboard.getUrl();
                uploadFromUrl(urlString);
                success = true;
            } else if (dragboard.hasString()) {
                String content = dragboard.getString();
                if (isValidUrl(content)) {
                    uploadFromUrl(content);
                    success = true;
                }
            }
        } catch (Exception e) {
            showError("Failed to process dropped content", e);
        }

        event.setDropCompleted(success);
        event.consume();
        resetStyle();
    }

    private void uploadFile(File sourceFile) {
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            showError("Invalid file: " + sourceFile.getName(), null);
            return;
        }

        showProgress("Uploading " + sourceFile.getName() + "...");

        Task<File> uploadTask = new Task<File>() {
            @Override
            protected File call() throws Exception {
                Path sourcePath = sourceFile.toPath();
                Path targetPath = Paths.get(targetDirectory, sourceFile.getName());

                // Update progress
                updateProgress(0, 1);

                // Copy file with progress tracking
                long fileSize = Files.size(sourcePath);

                // For demonstration, we'll simulate progress updates
                // In a real implementation, you might want to implement custom copy with progress
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                updateProgress(1, 1);
                return targetPath.toFile();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideProgress();
                    File uploadedFile = getValue();
                    showSuccess("Upload completed: " + uploadedFile.getName());
                    if (onUploadComplete != null) {
                        onUploadComplete.accept(uploadedFile);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    Throwable exception = getException();
                    showError("Upload failed", exception);
                });
            }
        };

        progressBar.progressProperty().bind(uploadTask.progressProperty());
        executorService.submit(uploadTask);
    }

    private void uploadFromUrl(String urlString) {
        showProgress("Downloading from URL...");

        Task<File> downloadTask = new Task<File>() {
            @Override
            protected File call() throws Exception {
                URL url = new URL(urlString);
                String fileName = extractFileNameFromUrl(urlString);
                Path targetPath = Paths.get(targetDirectory, fileName);

                updateProgress(0, 1);

                // Download file from URL
                try (var inputStream = url.openStream()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                updateProgress(1, 1);
                return targetPath.toFile();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideProgress();
                    File downloadedFile = getValue();
                    showSuccess("Download completed: " + downloadedFile.getName());
                    if (onUploadComplete != null) {
                        onUploadComplete.accept(downloadedFile);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    Throwable exception = getException();
                    showError("Download failed", exception);
                });
            }
        };

        progressBar.progressProperty().bind(downloadTask.progressProperty());
        executorService.submit(downloadTask);
    }

    private void showProgress(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.BLUE);
        statusLabel.setVisible(true);
        progressBar.setVisible(true);
        promptLabel.setVisible(false);
    }

    private void hideProgress() {
        progressBar.setVisible(false);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setVisible(true);

        // Hide success message after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    promptLabel.setVisible(true);
                    resetStyle();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showError(String message, Throwable throwable) {
        String errorMessage = message;
        if (throwable != null) {
            errorMessage += ": " + throwable.getMessage();
        }

        statusLabel.setText(errorMessage);
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(true);
        promptLabel.setVisible(false);
        setStyle(ERROR_STYLE);

        if (onError != null) {
            onError.accept(message, throwable);
        }

        // Hide error message after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    promptLabel.setVisible(true);
                    resetStyle();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void resetStyle() {
        setStyle(DEFAULT_STYLE);
    }

    private boolean isValidUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private String extractFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = "downloaded_file_" + System.currentTimeMillis();
            }

            return fileName;
        } catch (Exception e) {
            return "downloaded_file_" + System.currentTimeMillis();
        }
    }

    /**
     * Clean up resources when the component is no longer needed.
     */
    public void dispose() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
