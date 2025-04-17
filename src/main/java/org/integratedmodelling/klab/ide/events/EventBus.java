package org.integratedmodelling.klab.ide.events;


import javafx.scene.input.KeyCodeCombination;
import org.integratedmodelling.klab.ide.pages.Page;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Simple event bus implementation.
 *
 * <p>Subscribe and publish events. Events are published in channels distinguished by event type.
 * Channels can be grouped using an event type hierarchy.
 *
 * <p>You can use the default event bus instance {@link #getInstance}, which is a singleton,
 * or you can create one or multiple instances of {@link EventBus}.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class EventBus {

    public abstract static class Event {

        protected final UUID id = UUID.randomUUID();

        protected Event() {
        }

        public UUID getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Event event)) {
                return false;
            }
            return id.equals(event.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Event{"
                    + "id=" + id
                    + '}';
        }

        public static <E extends Event> void publish(E event) {
            EventBus.getInstance().publish(event);
        }
    }
    public final static class NavEvent extends Event {

        private final Class<? extends Page> page;

        public NavEvent(Class<? extends Page> page) {
            this.page = page;
        }

        public Class<? extends Page> getPage() {
            return page;
        }

        @Override
        public String toString() {
            return "NavEvent{"
                    + "page=" + page
                    + "} " + super.toString();
        }
    }

    public final static class BrowseEvent extends Event {

        private final URI uri;

        public BrowseEvent(URI uri) {
            this.uri = uri;
        }

        public URI getUri() {
            return uri;
        }

        @Override
        public String toString() {
            return "BrowseEvent{"
                    + "uri=" + uri
                    + "} " + super.toString();
        }

        public static void fire(String url) {
            Event.publish(new BrowseEvent(URI.create(url)));
        }
    }

    public final static class ThemeEvent extends Event {

        public enum EventType {
            // theme can change both, base font size and colors
            THEME_CHANGE,
            // font size or family only change
            FONT_CHANGE,
            // colors only change
            COLOR_CHANGE,
            // new theme added or removed
            THEME_ADD,
            THEME_REMOVE
        }

        private final EventType eventType;

        public ThemeEvent(EventType eventType) {
            this.eventType = eventType;
        }

        public EventType getEventType() {
            return eventType;
        }

        @Override
        public String toString() {
            return "ThemeEvent{"
                    + "eventType=" + eventType
                    + "} " + super.toString();
        }
    }

    public final static class PageEvent extends Event {

        public enum Action {
            SOURCE_CODE_ON,
            SOURCE_CODE_OFF
        }

        private final Action action;

        public PageEvent(Action action) {
            this.action = Objects.requireNonNull(action, "action");
        }

        public Action getAction() {
            return action;
        }

        @Override
        public String toString() {
            return "ActionEvent{"
                    + "action=" + action
                    + "} " + super.toString();
        }
    }

    public final static class HotkeyEvent extends Event {

        private final KeyCodeCombination keys;

        public HotkeyEvent(KeyCodeCombination keys) {
            this.keys = keys;
        }

        public KeyCodeCombination getKeys() {
            return keys;
        }

        @Override
        public String toString() {
            return "HotkeyEvent{"
                    + "keys=" + keys
                    + "} " + super.toString();
        }
    }

    public EventBus() {
    }

    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

//    @Override
    public <E extends Event> void subscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(subscriber);

        Set<Consumer> eventSubscribers = getOrCreateSubscribers(eventType);
        eventSubscribers.add(subscriber);
    }

    private <E> Set<Consumer> getOrCreateSubscribers(Class<E> eventType) {
        Set<Consumer> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers == null) {
            eventSubscribers = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, eventSubscribers);
        }
        return eventSubscribers;
    }

//    @Override
    public <E extends Event> void unsubscribe(Consumer<E> subscriber) {
        Objects.requireNonNull(subscriber);

        subscribers.values().forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

//    @Override
    public <E extends Event> void unsubscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(subscriber);

        subscribers.keySet().stream()
                   .filter(eventType::isAssignableFrom)
                   .map(subscribers::get)
                   .forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

//    @Override
    public <E extends Event> void publish(E event) {
        Objects.requireNonNull(event);

        Class<?> eventType = event.getClass();
        subscribers.keySet().stream()
                   .filter(type -> type.isAssignableFrom(eventType))
                   .flatMap(type -> subscribers.get(type).stream())
                   .forEach(subscriber -> publish(event, subscriber));
    }

    private <E extends Event> void publish(E event, Consumer<E> subscriber) {
        try {
            subscriber.accept(event);
        } catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class InstanceHolder {

        private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
