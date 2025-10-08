package io.kite.Runtime.Decorators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tags {
    private Set<String> tags;
    private Map<String, Object> tagsWithValue;

    public Tags(Set<String> tags) {
        this.tags = tags;
    }

    public Tags(Map<String, Object> tagsWithValue) {
        this.tagsWithValue = tagsWithValue;
    }

    public void addTag(String tag) {
        getTags().add(tag);
    }

    public void addTag(String tag, Object value) {
        getTagsWithValue().put(tag, value);
    }

    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public Map<String, Object> getTagsWithValue() {
        if (tagsWithValue == null) {
            tagsWithValue = new HashMap<>();
        }
        return tagsWithValue;
    }
}
