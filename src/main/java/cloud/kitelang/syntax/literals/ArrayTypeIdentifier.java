package cloud.kitelang.syntax.literals;

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

    public static ArrayTypeIdentifier arrayType(TypeIdentifier type) {
        return new ArrayTypeIdentifier(type);
    }

    public static ArrayTypeIdentifier arrayType(String type) {
        return new ArrayTypeIdentifier(type(type));
    }

    @Override
    public String string() {
        var res = new StringBuilder(super.string());
        res.append("[");
        for (Literal item : items) {
            res.append(item.getVal().toString());
            res.append(",");
        }
        res.append("]");
        return res.toString();
    }

    public void add(Literal literal) {
        this.items.add(literal);
    }
}
