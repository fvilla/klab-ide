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
            closeButton.setGraphic(new FontIcon(Material2MZ.WORK_OUTLINE));
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

    private static NotificationManager instance;
    private final VBox notificationContainer;
    private final List<Notification> activeNotifications = new ArrayList<>();
    private static final int DEFAULT_DURATION_SECONDS = 5;
    private static final int MAX_NOTIFICATIONS = 5;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private NotificationManager() {
        notificationContainer = new VBox(10); // 10px spacing between notifications
        notificationContainer.setAlignment(Pos.BOTTOM_RIGHT);
        notificationContainer.setMouseTransparent(false);
        notificationContainer.setPickOnBounds(false);
        notificationContainer.setPadding(new Insets(20));
        notificationContainer.setMaxWidth(Region.USE_PREF_SIZE);
    }

    /**
     * Get the singleton instance of NotificationManager.
     * @return The NotificationManager instance
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

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
            ((javafx.scene.layout.Pane) scene.getRoot()).getChildren().add(notificationContainer);
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
            // Create the notification
            Notification notification = new Notification(title, message, type);

            // Add to active notifications
            activeNotifications.add(notification);

            // Limit the number of notifications
            if (activeNotifications.size() > MAX_NOTIFICATIONS) {
                Notification oldestNotification = activeNotifications.remove(0);
                notificationContainer.getChildren().remove(oldestNotification);
            }

            // Add to container
            notificationContainer.getChildren().add(notification);

            // Set up fade out animation
            setupFadeOutAnimation(notification, DEFAULT_DURATION_SECONDS);
        });
    }

    /**
     * Set up the fade out animation for a notification.
     * @param notification The notification to animate
     * @param durationSeconds The duration in seconds before the notification fades out
     */
    private void setupFadeOutAnimation(Node notification, int durationSeconds) {
        // Create timeline for fade out
        Timeline fadeOutTimeline = new Timeline(
            new KeyFrame(Duration.seconds(durationSeconds), e -> {
                // Start fade out
                Timeline fade = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(notification.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1), event -> {
                        // Remove notification after fade out
                        notificationContainer.getChildren().remove(notification);
                        activeNotifications.remove(notification);
                    }, new KeyValue(notification.opacityProperty(), 0.0))
                );
                fade.play();
            })
        );
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
