package ma.tarik.democqrs.commonApi.events;

public abstract class BaseEvent<T> {
    private T id;

    public BaseEvent(T id) {
        this.id = id;
    }

    public T getId() {
        return id;
    }
}
