package io.kite.Runtime.Decorators;

public interface TagsSupport {
    Tags getTags();

    void setTags(Tags tags);

    default void addTag(String provider) {
        if (getTags() == null) {
            setTags(new Tags());
        }
        var tags = getTags();
        tags.addTag(provider);
    }

    default void addTag(String key, String value) {
        if (getTags() == null) {
            setTags(new Tags());
        }
        var tags = getTags();
        tags.addTag(key, value);
    }
}
