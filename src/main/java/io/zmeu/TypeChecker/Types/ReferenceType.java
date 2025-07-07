package io.zmeu.TypeChecker.Types;

public sealed class ReferenceType extends Type permits ObjectType, ResourceType, SchemaType {
    public static ReferenceType Resource = new ReferenceType("Resource");
    public static ReferenceType Schema = new ReferenceType("Schema");

    public ReferenceType(String typeName) {
        super(typeName);
    }

    public static ReferenceType of(String typeName) {
        return new ReferenceType(typeName);
    }

    @Override
    public String toString() {
        return super.getValue();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
