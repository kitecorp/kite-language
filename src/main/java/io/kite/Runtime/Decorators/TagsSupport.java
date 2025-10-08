package io.kite.Runtime.Decorators;

public interface TagsSupport {
    Tags getTags();

    void setTag(Tags tags);

    default void addTag(String provider) {
        if (getTags() == null) {
            setTag(new Tags());
        }
        var tags = getTags();
        tags.addTag(provider);
    }

    default void addTag(String key, String value) {
        if (getTags() == null) {
            setTag(new Tags());
        }
        var tags = getTags();
        tags.addTag(key, value);
    }
}
