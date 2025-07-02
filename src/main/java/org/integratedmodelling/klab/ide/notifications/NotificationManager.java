package org.integratedmodelling.klab.ide.notifications;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.integratedmodelling.klab.ide.KlabIDEApplication;
import org.integratedmodelling.klab.ide.Theme;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton class for managing notifications in the application.
 * Notifications appear in the bottom-right corner of the scene and support stacking.
 * After a set duration, notifications will fade out automatically.
 */
public class NotificationManager {

    /**
     * Notification types with corresponding icons and styles
     */
    public enum NotificationType {
        INFORMATION(Material2AL.LIGHTBULB, "notification-info"),
        SUCCESS(Material2AL.CHECK, "notification-success"),
        WARNING(Material2AL.DEVELOPER_BOARD, "notification-warning"),
        ERROR(Material2AL.BUILD_CIRCLE, "notification-error");

        private final Ikon icon;
        private final String styleClass;

        NotificationType(Ikon icon, String styleClass) {
            this.icon = icon;
            this.styleClass = styleClass;
        }

        public Ikon getIcon() {
            return icon;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    /**
     * Custom notification node
     */
    public class Notification extends HBox {
        private final String title;
        private final String message;
        private final NotificationType type;

        public Notification(String title, String message, NotificationType type) {
            super(10);
            this.title = title;
            this.message = message;
            this.type = type;

            // Set up the notification UI
            setupUI();
        }

        private void setupUI() {
            // Set padding and style
            setPadding(new Insets(15));
            setMaxWidth(400);
            setMinWidth(300);
            getStyleClass().add("card");
            getStyleClass().add(type.getStyleClass());

            // Create icon
            FontIcon icon = new FontIcon(type.getIcon());
            icon.getStyleClass().add("notification-icon");

            // Create content
            VBox content = new VBox(5);

            // Create title
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("title-4");

            // Create message
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);

            content.getChildren().addAll(titleLabel, messageLabel);
            HBox.setHgrow(content, Priority.ALWAYS);

            // Create close button
            Button closeButton = new Button();
            closeButton.getStyleClass().addAll("button-icon", "flat");
            closeButton.setGraphic(new FontIcon(Material2MZ.REMOVE_CIRCLE_OUTLINE));
            closeButton.setOnAction(e -> close());

            // Add all components to the notification
            getChildren().addAll(icon, content, closeButton);
        }

        public void close() {
            // Remove from container and active notifications
            notificationContainer.getChildren().remove(this);
            activeNotifications.remove(this);
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public NotificationType getType() {
            return type;
        }
    }

//    private static NotificationManager instance;
    private final VBox notificationContainer;
    private final List<Notification> activeNotifications = new ArrayList<>();
    private static final int DEFAULT_DURATION_SECONDS = 5;
    private static final int MAX_NOTIFICATIONS = 5;

    /**
     * Private constructor to enforce singleton pattern.
     */
    public NotificationManager(Scene scene) {

        notificationContainer = new VBox(10); // 10px spacing between notifications
        notificationContainer.setAlignment(Pos.BOTTOM_RIGHT);
        notificationContainer.setMouseTransparent(false);
        notificationContainer.setPickOnBounds(false);
        notificationContainer.setPadding(new Insets(20));
        notificationContainer.setMaxWidth(Region.USE_PREF_SIZE);

        // Set explicit size and position
        notificationContainer.setMinWidth(400);
        notificationContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // Set style to ensure visibility
        notificationContainer.setStyle("-fx-background-color: transparent;");

        // Make sure it's visible in the scene
        notificationContainer.toFront();

        // Set ID for debugging
        notificationContainer.setId("notification-container");

        initialize(scene);
    }

//    /**
//     * Get the singleton instance of NotificationManager.
//     * @return The NotificationManager instance
//     */
//    public static synchronized NotificationManager getInstance() {
//        if (instance == null) {
//            instance = new NotificationManager();
//        }
//        return instance;
//    }

    /**
     * Initialize the notification manager with the main scene.
     * This should be called once from the main application.
     * @param scene The main application scene
     */
    public void initialize(Scene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene cannot be null");
        }

        // Add the notification container to the scene's root
        if (scene.getRoot() instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane root = (javafx.scene.layout.Pane) scene.getRoot();

            // Remove if already added (in case of re-initialization)
            root.getChildren().remove(notificationContainer);

            // Create a StackPane to hold the notification container
            javafx.scene.layout.StackPane notificationPane = new javafx.scene.layout.StackPane();
            notificationPane.setPrefSize(scene.getWidth(), scene.getHeight());
            notificationPane.setPickOnBounds(false);
            notificationPane.setMouseTransparent(true);

            // Position the notification container at the bottom-right
            notificationPane.setAlignment(Pos.BOTTOM_RIGHT);
            notificationPane.getChildren().add(notificationContainer);

            // Add the notification pane to the root
            root.getChildren().add(notificationPane);

            // Ensure the notification container is always on top
            notificationPane.setViewOrder(-1000);
            notificationPane.toFront();
            notificationContainer.toFront();

            // Make sure it's visible
            notificationPane.setVisible(true);
            notificationPane.setManaged(true);
            notificationContainer.setVisible(true);
            notificationContainer.setManaged(true);

            // Update size when scene size changes
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                notificationPane.setPrefWidth(newVal.doubleValue());
            });

            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                notificationPane.setPrefHeight(newVal.doubleValue());
            });

            // Debug
            System.out.println("Notification container initialized and added to scene with StackPane");
        } else {
            throw new IllegalArgumentException("Scene root must be a Pane");
        }
    }

    /**
     * Show an information notification.
     * @param title The notification title
     * @param message The notification message
     */
    public void showInformation(String title, String message) {
        showNotification(title, message, NotificationType.INFORMATION);
    }

    /**
     * Show a success notification.
     * @param title The notification title
     * @param message The notification message
     */
    public void showSuccess(String title, String message) {
        showNotification(title, message, NotificationType.SUCCESS);
    }

    /**
     * Show a warning notification.
     * @param title The notification title
     * @param message The notification message
     */
    public void showWarning(String title, String message) {
        showNotification(title, message, NotificationType.WARNING);
    }

    /**
     * Show an error notification.
     * @param title The notification title
     * @param message The notification message
     */
    public void showError(String title, String message) {
        showNotification(title, message, NotificationType.ERROR);
    }

    /**
     * Show a notification with the specified type.
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type
     */
    private void showNotification(String title, String message, NotificationType type) {
        Platform.runLater(() -> {
            // Debug
            System.out.println("Showing notification: " + title + " - " + message);

            notificationContainer.toFront();

            // Create the notification
            Notification notification = new Notification(title, message, type);

            // Add to active notifications
            activeNotifications.add(notification);

            // Limit the number of notifications
            if (activeNotifications.size() > MAX_NOTIFICATIONS) {
                Notification oldestNotification = activeNotifications.remove(0);
                notificationContainer.getChildren().remove(oldestNotification);
            }

            // Ensure container is visible and at the front
            notificationContainer.setVisible(true);
            notificationContainer.toFront();
            notification.toFront();

            // Make sure the container is in the scene
            if (notificationContainer.getScene() == null) {
                System.err.println("ERROR: Notification container is not in scene!");
            } else {
                System.out.println("Notification container is in scene");

                // If the parent is a StackPane, make sure it's visible and at the front
                if (notificationContainer.getParent() instanceof javafx.scene.layout.StackPane) {
                    javafx.scene.layout.StackPane parent = (javafx.scene.layout.StackPane) notificationContainer.getParent();
                    parent.setVisible(true);
                    parent.toFront();
                    System.out.println("Parent StackPane is visible and at the front");
                }
            }

            // Set up fade out animation
            setupFadeOutAnimation(notification, DEFAULT_DURATION_SECONDS);

            // Add to container
            notificationContainer.getChildren().add(notification);

        });
    }

    /**
     * Set up the fade out animation for a notification.
     * @param notification The notification to animate
     * @param durationSeconds The duration in seconds before the notification fades out
     */
    private void setupFadeOutAnimation(Node notification, int durationSeconds) {
        // Ensure the notification is fully visible initially
        notification.setOpacity(1.0);

        // Create timeline for fade out
        Timeline fadeOutTimeline = new Timeline(
            new KeyFrame(Duration.seconds(durationSeconds), e -> {
                // Start fade out
                Timeline fade = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(notification.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1), event -> {
                        // Remove notification after fade out
                        notificationContainer.getChildren().remove(notification);
                        activeNotifications.remove(notification);
                    }, new KeyValue(notification.opacityProperty(), 0.0))
                );
                fade.setCycleCount(1);
                fade.setOnFinished(event -> {
                    // Ensure notification is removed even if animation fails
                    if (notificationContainer.getChildren().contains(notification)) {
                        notificationContainer.getChildren().remove(notification);
                    }
                    if (activeNotifications.contains(notification)) {
                        activeNotifications.remove(notification);
                    }
                });
                fade.play();
            })
        );
        fadeOutTimeline.setCycleCount(1);
        fadeOutTimeline.play();
    }

    /**
     * Clear all active notifications.
     */
    public void clearAll() {
        Platform.runLater(() -> {
            notificationContainer.getChildren().clear();
            activeNotifications.clear();
        });
    }
}
