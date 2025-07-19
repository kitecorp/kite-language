package io.zmeu.Frontend.Parse.Literals;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ArrayTypeIdentifier extends TypeIdentifier {
    private List<Literal> items;

    public ArrayTypeIdentifier(TypeIdentifier type) {
        super(type.getType(), type.getPath());
        this.items = new ArrayList<>();
    }

    @Override
    public String string() {
        StringBuilder res= new StringBuilder(super.string());
        for (Literal item : items) {
            res.append(item.getVal().toString());
            res.append(",");
        }
        return res.toString();
    }


    public void add(Literal literal) {
        this.items.add(literal);
    }
}
