package org.integratedmodelling.klab.ide.components;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A JavaFX component that displays a timeline with alternating vertical sections of contrasting gray colors.
 * The timeline is configured with a start and end time (in milliseconds from epoch) and a temporal resolution
 * (time unit and multiplier). The end time can be changed, and the background will update accordingly.
 */
public class Timeline extends Components.BaseComponent {

    /**
     * Enum representing the type of event on the timeline.
     * Each type determines the icon that will be displayed.
     */
    public enum EventType {
        DEFAULT,    // Default event type (small magenta dot)
        INFO,       // Informational event
        WARNING,    // Warning event
        ERROR,      // Error event
        SUCCESS     // Success event
    }

    /**
     * Class representing an event on the timeline.
     */
    public static class Event {
        private final long timestamp;
        private final EventType type;
        private final Consumer<Event> onClick;
        private Node icon;

        /**
         * Creates a new event with the specified timestamp, type, and click handler.
         *
         * @param timestamp The timestamp of the event in milliseconds from epoch
         * @param type The type of the event
         * @param onClick The consumer to call when the event is clicked
         */
        public Event(long timestamp, EventType type, Consumer<Event> onClick) {
            this.timestamp = timestamp;
            this.type = type;
            this.onClick = onClick;
        }

        /**
         * Gets the timestamp of the event.
         *
         * @return The timestamp in milliseconds from epoch
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Gets the type of the event.
         *
         * @return The event type
         */
        public EventType getType() {
            return type;
        }

        /**
         * Gets the click handler for the event.
         *
         * @return The consumer to call when the event is clicked
         */
        public Consumer<Event> getOnClick() {
            return onClick;
        }

        /**
         * Sets the icon node for this event.
         *
         * @param icon The JavaFX node representing the icon
         */
        void setIcon(Node icon) {
            this.icon = icon;
        }

        /**
         * Gets the icon node for this event.
         *
         * @return The JavaFX node representing the icon
         */
        Node getIcon() {
            return icon;
        }
    }

    private long startTimeMs;
    private long endTimeMs;
    private TimeUnit timeUnit;
    private int multiplier;

    private Pane timelinePane;
    private Label startTimeLabel;
    private Label endTimeLabel;
    private Slider endTimeSlider;

    private List<Event> events = new ArrayList<>();

    private static final Color LIGHT_GREY = Color.rgb(240, 240, 240);
    private static final Color DARK_GREY = Color.rgb(220, 220, 220);
    private static final Color DEFAULT_EVENT_COLOR = Color.MAGENTA;
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Creates a new Timeline component with the specified configuration.
     *
     * @param startTimeMs The start time in milliseconds from epoch
     * @param endTimeMs The end time in milliseconds from epoch
     * @param timeUnit The time unit for the temporal resolution
     * @param multiplier The multiplier for the temporal resolution
     */
    public Timeline(long startTimeMs, long endTimeMs, TimeUnit timeUnit, int multiplier) {
        super(Components.Type.Object, "Timeline", false);
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.timeUnit = timeUnit;
        this.multiplier = multiplier;
        createContent();
    }

    @Override
    protected void createContent() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Create the timeline pane
        timelinePane = new Pane();
        timelinePane.setPrefHeight(100);
        timelinePane.setMinHeight(100);
        timelinePane.setMaxHeight(100);
        HBox.setHgrow(timelinePane, Priority.ALWAYS);

        // Create time labels
        startTimeLabel = new Label(formatTime(startTimeMs));
        endTimeLabel = new Label(formatTime(endTimeMs));

        HBox labelsBox = new HBox();
        labelsBox.setSpacing(10);
        HBox.setHgrow(labelsBox, Priority.ALWAYS);

        VBox startLabelBox = new VBox(startTimeLabel);
        startLabelBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox endLabelBox = new VBox(endTimeLabel);
        endLabelBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        HBox.setHgrow(startLabelBox, Priority.ALWAYS);
        HBox.setHgrow(endLabelBox, Priority.ALWAYS);

        labelsBox.getChildren().addAll(startLabelBox, endLabelBox);

        // Create end time slider
        endTimeSlider = new Slider();
        endTimeSlider.setMin(startTimeMs);
        endTimeSlider.setMax(startTimeMs + (endTimeMs - startTimeMs) * 2); // Allow extending beyond initial end time
        endTimeSlider.setValue(endTimeMs);

        endTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateEndTime(newVal.longValue());
        });

        // Add components to container
        container.getChildren().addAll(timelinePane, labelsBox, endTimeSlider);

        // Draw the initial timeline
        drawTimeline();

        this.getChildren().add(container);
    }

    /**
     * Updates the end time and redraws the timeline.
     *
     * @param newEndTimeMs The new end time in milliseconds from epoch
     */
    public void updateEndTime(long newEndTimeMs) {
        if (newEndTimeMs > startTimeMs) {
            this.endTimeMs = newEndTimeMs;
            endTimeLabel.setText(formatTime(endTimeMs));
            drawTimeline();
        }
    }

    /**
     * Inserts one or more events into the timeline.
     * If an event has a timestamp beyond the timeline's end time, the timeline's span is automatically extended.
     *
     * @param events The events to insert
     */
    public void insertEvents(Event... events) {
        for (Event event : events) {
            // If the event is beyond the end time, extend the timeline
            if (event.getTimestamp() > endTimeMs) {
                updateEndTime(event.getTimestamp());

                // Also update the slider's max value if needed
                if (event.getTimestamp() > endTimeSlider.getMax()) {
                    endTimeSlider.setMax(event.getTimestamp() + (endTimeMs - startTimeMs));
                }
            }

            this.events.add(event);
        }

        // Redraw the timeline to show the new events
        drawTimeline();
    }

    /**
     * Creates an icon for an event based on its type.
     *
     * @param event The event to create an icon for
     * @return The icon node
     */
    private Node createEventIcon(Event event) {
        Circle circle = new Circle(5);

        // Set the color based on the event type
        switch (event.getType()) {
            case INFO:
                circle.setFill(Color.BLUE);
                break;
            case WARNING:
                circle.setFill(Color.ORANGE);
                break;
            case ERROR:
                circle.setFill(Color.RED);
                break;
            case SUCCESS:
                circle.setFill(Color.GREEN);
                break;
            case DEFAULT:
            default:
                circle.setFill(DEFAULT_EVENT_COLOR);
                break;
        }

        // Add a tooltip showing the event timestamp
        Tooltip tooltip = new Tooltip(formatTime(event.getTimestamp()));
        Tooltip.install(circle, tooltip);

        // Add click handler
        circle.setOnMouseClicked(e -> {
            if (event.getOnClick() != null) {
                event.getOnClick().accept(event);
            }
        });

        // Make the circle slightly larger when hovered
        circle.setOnMouseEntered(e -> circle.setRadius(7));
        circle.setOnMouseExited(e -> circle.setRadius(5));

        return circle;
    }

    /**
     * Draws the timeline with alternating vertical sections of contrasting grey colors.
     * Also draws any events that have been added to the timeline.
     */
    private void drawTimeline() {
        timelinePane.getChildren().clear();

        double width = timelinePane.getWidth();
        double height = timelinePane.getHeight();

        if (width <= 0) {
            // If the pane hasn't been laid out yet, set a listener to redraw when it is
            timelinePane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    drawTimeline();
                }
            });
            return;
        }

        // Calculate the duration in the specified time unit
        long durationMs = endTimeMs - startTimeMs;
        long durationInUnit = timeUnit.convert(durationMs, TimeUnit.MILLISECONDS);

        // Calculate the number of intervals
        int numIntervals = (int) Math.ceil((double) durationInUnit / multiplier);

        // Calculate the width of each interval
        double intervalWidth = width / numIntervals;

        // Draw the alternating sections
        for (int i = 0; i < numIntervals; i++) {
            Rectangle section = new Rectangle(
                i * intervalWidth,
                0,
                intervalWidth,
                height
            );

            // Alternate between light and dark grey
            section.setFill(i % 2 == 0 ? LIGHT_GREY : DARK_GREY);

            timelinePane.getChildren().add(section);
        }

        // Draw the events
        for (Event event : events) {
            // Calculate the x position based on the event timestamp
            double position = ((double) (event.getTimestamp() - startTimeMs) / durationMs) * width;

            // Create the event icon if it doesn't exist
            if (event.getIcon() == null) {
                event.setIcon(createEventIcon(event));
            }

            Node icon = event.getIcon();

            // Position the icon
            icon.setLayoutX(position);
            icon.setLayoutY(height / 2);

            // Add the icon to the timeline
            timelinePane.getChildren().add(icon);
        }
    }

    /**
     * Formats a time in milliseconds from epoch as a human-readable string.
     *
     * @param timeMs The time in milliseconds from epoch
     * @return A formatted time string
     */
    private String formatTime(long timeMs) {
        return TIME_FORMATTER.format(Instant.ofEpochMilli(timeMs));
    }

    /**
     * Gets the current start time in milliseconds from epoch.
     *
     * @return The start time
     */
    public long getStartTimeMs() {
        return startTimeMs;
    }

    /**
     * Gets the current end time in milliseconds from epoch.
     *
     * @return The end time
     */
    public long getEndTimeMs() {
        return endTimeMs;
    }

    /**
     * Gets the current time unit for the temporal resolution.
     *
     * @return The time unit
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Gets the current multiplier for the temporal resolution.
     *
     * @return The multiplier
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Gets the list of events on the timeline.
     *
     * @return The list of events
     */
    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }
}
