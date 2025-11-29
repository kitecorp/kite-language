package cloud.kitelang.execution.decorators;

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
    private Map<String, String> tagsMap;

    public Tags(Set<String> tags) {
        this.tags = tags;
    }

    public Tags(Map<String, String> tagsWithValue) {
        this.tagsMap = tagsWithValue;
    }

    public void addTag(String tag) {
        getTags().add(tag);
    }

    public void addTag(String tag, String value) {
        getTagsMap().put(tag, value);
    }

    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public Map<String, String> getTagsMap() {
        if (tagsMap == null) {
            tagsMap = new HashMap<>();
        }
        return tagsMap;
    }

    public static Tags tags(String... tags) {
        return new Tags(Set.of(tags));
    }
    public static Tags tags(Map<String, String> tags) {
        return new Tags(tags);
    }
}
