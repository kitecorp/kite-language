package io.kite.tool;

import lombok.Data;

@Data
public class ClassDef{
    private String[] fields;
    private String className;
    private String basename;
    public ClassDef(String definition){
        this.className = definition.split(":")[0].trim();
        // Store parameters in fields.
        this.fields = definition.split(":")[1]
                .trim()
                .split(", ");
    }

}
