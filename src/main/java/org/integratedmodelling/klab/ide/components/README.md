# Timeline Component

The Timeline component is a JavaFX UI component that displays a timeline with alternating vertical sections of contrasting grey colors. It is configured with a start and end time (in milliseconds from epoch) and a temporal resolution (time unit and multiplier).

## Features

- Configurable start and end times (in milliseconds from epoch)
- Configurable temporal resolution (time unit and multiplier)
- Alternating vertical sections of contrasting grey colors
- End time can be changed using a slider, and the background updates accordingly
- Time labels showing the start and end times in human-readable format

## Usage

### Basic Usage

```java
// Create a timeline with start time (now), end time (1 hour later), 
// and temporal resolution of 5 minutes
long startTimeMs = System.currentTimeMillis();
long endTimeMs = startTimeMs + 3600000; // 1 hour in milliseconds
Timeline timeline = new Timeline(startTimeMs, endTimeMs, TimeUnit.MINUTES, 5);

// Add the timeline to a container
myContainer.getChildren().add(timeline);
```

### Changing the End Time Programmatically

```java
// Update the end time to 2 hours later
timeline.updateEndTime(startTimeMs + 7200000);
```

### Using the TimelineDemo Component

The TimelineDemo component provides a complete example of how to use the Timeline component with various configurations.

```java
// Create a timeline demo
TimelineDemo demo = TimelineDemo.createDemo();

// Add the demo to a container
myContainer.getChildren().add(demo);
```

### Using the TimelineComponent from Components Class

The Timeline component is also accessible through the Components class.

```java
// Create a timeline component
Components.TimelineComponent timelineComponent = new Components.TimelineComponent();

// Add the component to a container
myContainer.getChildren().add(timelineComponent);
```

## Implementation Details

The Timeline component is implemented in the following files:

- `Timeline.java`: The main component implementation
- `TimelineDemo.java`: A demo component showcasing the Timeline with various configurations
- `Components.java`: Contains a TimelineComponent class that creates a TimelineDemo instance

The Timeline component extends `Components.BaseComponent` and follows the same patterns as other components in the application.

## Customization

The Timeline component can be customized by:

1. Changing the start and end times
2. Changing the temporal resolution (time unit and multiplier)
3. Subclassing and overriding methods to change the appearance or behavior

## Example: Creating a Timeline with Custom Colors

```java
// Subclass Timeline to customize colors
public class CustomColorTimeline extends Timeline {
    private static final Color CUSTOM_LIGHT = Color.rgb(230, 240, 255);
    private static final Color CUSTOM_DARK = Color.rgb(200, 220, 255);
    
    public CustomColorTimeline(long startTimeMs, long endTimeMs, TimeUnit timeUnit, int multiplier) {
        super(startTimeMs, endTimeMs, timeUnit, multiplier);
    }
    
    @Override
    protected void drawTimeline() {
        // Override to use custom colors
        // Implementation would be similar to the original but with custom colors
    }
}
```