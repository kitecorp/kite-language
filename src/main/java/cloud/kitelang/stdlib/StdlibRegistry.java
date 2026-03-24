package cloud.kitelang.stdlib;

import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.semantics.TypeEnvironment;
import cloud.kitelang.semantics.types.*;
import cloud.kitelang.stdlib.functions.PrintFunction;
import cloud.kitelang.stdlib.functions.PrintlnFunction;
import cloud.kitelang.stdlib.functions.cast.*;
import cloud.kitelang.stdlib.functions.collections.*;
import cloud.kitelang.stdlib.functions.datetime.*;
import cloud.kitelang.stdlib.functions.numeric.*;
import cloud.kitelang.stdlib.functions.objects.*;
import cloud.kitelang.stdlib.functions.string.*;
import cloud.kitelang.stdlib.functions.types.*;
import cloud.kitelang.stdlib.functions.utility.*;

import java.util.Set;

/**
 * Central registry for all Kite standard library builtin functions.
 * <p>
 * This is the single source of truth for stdlib function names. It handles both:
 * <ul>
 *   <li>Runtime registration: callable instances into the Interpreter's {@link Environment}</li>
 *   <li>Type registration: function type signatures into the TypeChecker's {@link TypeEnvironment}</li>
 * </ul>
 * <p>
 * Adding a new stdlib function requires updating both {@link #registerRuntime} (for execution)
 * and {@link #registerTypes} (for compile-time type checking).
 *
 * @see cloud.kitelang.execution.Interpreter
 * @see cloud.kitelang.semantics.TypeChecker
 */
public final class StdlibRegistry {

    /**
     * Functions that accept optional parameters (variable arity).
     * The type checker uses this to relax strict argument count validation.
     */
    public static final Set<String> FUNCTIONS_WITH_OPTIONAL_PARAMS = Set.of(
            "substring", "join", "slice", "range", "hash", "environment", "get"
    );

    /**
     * Complete set of all stdlib function names registered in the type environment.
     * Used by {@link cloud.kitelang.semantics.TypeEnvironment#initShadowingStdlib}
     * to allow user-defined variables and functions to shadow stdlib builtins.
     */
    public static final Set<String> STDLIB_FUNCTION_NAMES = Set.of(
            // I/O
            "print", "println",
            // Numeric
            "abs", "pow", "ceil", "floor", "min", "max", "round", "sqrt",
            "random", "clamp", "sign", "mod",
            // Collections
            "isEmpty", "contains", "first", "last", "join", "sort", "push", "pop",
            "reverse", "slice", "find", "distinct", "flatten", "take", "drop",
            "sum", "range", "zip", "average", "findIndex",
            // String
            "length", "substring", "toString", "toUpperCase", "toLowerCase", "trim",
            "replace", "split", "indexOf", "startsWith", "endsWith", "repeat",
            "padStart", "padEnd", "charAt", "matches", "format",
            // Datetime
            "now", "year", "month", "day", "hour", "minute", "second",
            "formatDate", "timestamp", "date", "addDays", "diffDays",
            "isLeapYear", "dayOfWeek", "parseDate", "toISOString",
            // Type checking
            "isString", "isNumber", "isBoolean", "isArray", "isObject", "isNull", "toNumber",
            // Object manipulation
            "keys", "values", "entries", "merge", "hasKey", "get",
            // Utility
            "uuid", "base64Encode", "base64Decode", "hash", "environment",
            "fileExists", "readFile", "fromJson", "toJson"
    );

    private StdlibRegistry() {
        // Utility class — not instantiable
    }

    /**
     * Registers all stdlib callable instances into the interpreter's runtime environment.
     * Each entry maps a function name to its {@link cloud.kitelang.execution.Callable} implementation.
     *
     * @param env the interpreter's environment to populate
     */
    public static void registerRuntime(Environment<Object> env) {
        // I/O
        env.init("print", new PrintFunction());
        env.init("println", new PrintlnFunction());

        // Casting
        env.init("int", new IntCastFunction());
        env.init("number", new NumberCastFunction());
        env.init("decimal", new DecimalCastFunction());
        env.init("string", new StringCastFunction());
        env.init("boolean", new BooleanCastFunction());
        env.init("any", new AnyCastFunction());

        // Numeric
        env.init("abs", new AbsFunction());
        env.init("pow", new PowFunction());
        env.init("ceil", new CeilFunction());
        env.init("floor", new FloorFunction());
        env.init("min", new MinFunction());
        env.init("max", new MaxFunction());
        env.init("round", new RoundFunction());
        env.init("sqrt", new SqrtFunction());
        env.init("random", new RandomFunction());
        env.init("clamp", new ClampFunction());
        env.init("sign", new SignFunction());
        env.init("mod", new ModFunction());

        // Collections
        env.init("isEmpty", new IsEmptyFunction());
        env.init("contains", new ContainsFunction());
        env.init("first", new FirstFunction());
        env.init("last", new LastFunction());
        env.init("join", new JoinFunction());
        env.init("sort", new SortFunction());
        env.init("push", new PushFunction());
        env.init("pop", new PopFunction());
        env.init("reverse", new ReverseFunction());
        env.init("slice", new SliceFunction());
        env.init("find", new FindFunction());
        env.init("distinct", new DistinctFunction());
        env.init("flatten", new FlattenFunction());
        env.init("take", new TakeFunction());
        env.init("drop", new DropFunction());
        env.init("sum", new SumFunction());
        env.init("range", new RangeFunction());
        env.init("zip", new ZipFunction());
        env.init("average", new AverageFunction());
        env.init("findIndex", new FindIndexFunction());

        // String
        env.init("length", new LengthFunction());
        env.init("substring", new SubstringFunction());
        env.init("toUpperCase", new ToUpperCaseFunction());
        env.init("toLowerCase", new ToLowerCaseFunction());
        env.init("trim", new TrimFunction());
        env.init("replace", new ReplaceFunction());
        env.init("split", new SplitFunction());
        env.init("indexOf", new IndexOfFunction());
        env.init("startsWith", new StartsWithFunction());
        env.init("endsWith", new EndsWithFunction());
        env.init("repeat", new RepeatFunction());
        env.init("padStart", new PadStartFunction());
        env.init("padEnd", new PadEndFunction());
        env.init("charAt", new CharAtFunction());
        env.init("matches", new MatchesFunction());
        env.init("format", new FormatFunction());

        // Datetime
        env.init("now", new NowFunction());
        env.init("year", new YearFunction());
        env.init("month", new MonthFunction());
        env.init("day", new DayFunction());
        env.init("hour", new HourFunction());
        env.init("minute", new MinuteFunction());
        env.init("second", new SecondFunction());
        env.init("formatDate", new FormatDateFunction());
        env.init("timestamp", new TimestampFunction());
        env.init("date", new DateFunction());
        env.init("addDays", new AddDaysFunction());
        env.init("diffDays", new DiffDaysFunction());
        env.init("isLeapYear", new IsLeapYearFunction());
        env.init("dayOfWeek", new DayOfWeekFunction());
        env.init("parseDate", new ParseDateFunction());
        env.init("toISOString", new ToISOStringFunction());

        // Type checking
        env.init("isString", new IsStringFunction());
        env.init("isNumber", new IsNumberFunction());
        env.init("isBoolean", new IsBooleanFunction());
        env.init("isArray", new IsArrayFunction());
        env.init("isObject", new IsObjectFunction());
        env.init("isNull", new IsNullFunction());
        env.init("toNumber", new ToNumberFunction());

        // Object manipulation
        env.init("keys", new KeysFunction());
        env.init("values", new ValuesFunction());
        env.init("entries", new EntriesFunction());
        env.init("merge", new MergeFunction());
        env.init("hasKey", new HasKeyFunction());
        env.init("get", new GetFunction());

        // Utility
        env.init("uuid", new UuidFunction());
        env.init("base64Encode", new Base64EncodeFunction());
        env.init("base64Decode", new Base64DecodeFunction());
        env.init("hash", new HashFunction());
        env.init("environment", new EnvironmentFunction());
        env.init("fileExists", new FileExistsFunction());
        env.init("readFile", new ReadFileFunction());
        env.init("fromJson", new FromJsonFunction());
        env.init("toJson", new ToJsonFunction());
    }

    /**
     * Registers type signatures for all stdlib functions into the type checker's environment.
     * This allows the type checker to validate function calls at compile time (argument count,
     * argument types, and return types).
     * <p>
     * Type abbreviations used:
     * <ul>
     *   <li>{@code any} - accepts any type</li>
     *   <li>{@code string|array} - union of string and array (for polymorphic functions)</li>
     *   <li>{@code array(any)} - array of any element type</li>
     * </ul>
     *
     * @param env the type checker's environment to populate
     */
    public static void registerTypes(TypeEnvironment env) {
        var anyArray = ArrayType.arrayType(AnyType.INSTANCE);
        var numberArray = ArrayType.arrayType(ValueType.Number);
        var stringOrArray = UnionType.unionType("string|array", ValueType.String, anyArray);

        // I/O: (any) -> void
        env.init("print", TypeFactory.add(FunType.fun(ValueType.Void, AnyType.INSTANCE)));
        env.init("println", TypeFactory.add(FunType.fun(ValueType.Void, AnyType.INSTANCE)));

        // Casting functions (int, number, decimal, string, boolean, any) are intentionally
        // NOT registered here. Their names collide with primitive type names (ValueType/ReferenceType)
        // already present in the TypeEnvironment. The type checker handles casting through
        // its type system rather than function-call validation.

        // Numeric: (number) -> number (mostly)
        env.init("abs", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("pow", TypeFactory.fromString("(%s,%s)->%s".formatted(
                ValueType.Number.getValue(), ValueType.Number.getValue(), ValueType.Number.getValue())));
        env.init("ceil", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("floor", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("min", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number, ValueType.Number)));
        env.init("max", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number, ValueType.Number)));
        env.init("round", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("sqrt", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("random", TypeFactory.add(FunType.fun(ValueType.Number)));
        env.init("clamp", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number, ValueType.Number, ValueType.Number)));
        env.init("sign", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number)));
        env.init("mod", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.Number, ValueType.Number)));

        // Collections
        env.init("isEmpty", TypeFactory.add(FunType.fun(ValueType.Boolean, stringOrArray)));
        env.init("contains", TypeFactory.add(FunType.fun(ValueType.Boolean, stringOrArray, AnyType.INSTANCE)));
        env.init("first", TypeFactory.add(FunType.fun(AnyType.INSTANCE, stringOrArray)));
        env.init("last", TypeFactory.add(FunType.fun(AnyType.INSTANCE, stringOrArray)));
        env.init("join", TypeFactory.add(FunType.fun(ValueType.String, anyArray)));
        env.init("sort", TypeFactory.add(FunType.fun(anyArray, anyArray)));
        env.init("push", TypeFactory.add(FunType.fun(anyArray, anyArray, AnyType.INSTANCE)));
        env.init("pop", TypeFactory.add(FunType.fun(anyArray, anyArray)));
        env.init("reverse", TypeFactory.add(FunType.fun(anyArray, anyArray)));
        env.init("slice", TypeFactory.add(FunType.fun(anyArray, anyArray, ValueType.Number)));
        env.init("find", TypeFactory.add(FunType.fun(AnyType.INSTANCE, anyArray, AnyType.INSTANCE)));
        env.init("distinct", TypeFactory.add(FunType.fun(anyArray, anyArray)));
        env.init("flatten", TypeFactory.add(FunType.fun(anyArray, anyArray)));
        env.init("take", TypeFactory.add(FunType.fun(anyArray, anyArray, ValueType.Number)));
        env.init("drop", TypeFactory.add(FunType.fun(anyArray, anyArray, ValueType.Number)));
        env.init("sum", TypeFactory.add(FunType.fun(ValueType.Number, numberArray)));
        env.init("range", TypeFactory.add(FunType.fun(anyArray, ValueType.Number)));
        env.init("zip", TypeFactory.add(FunType.fun(anyArray, anyArray, anyArray)));
        env.init("average", TypeFactory.add(FunType.fun(ValueType.Number, numberArray)));
        env.init("findIndex", TypeFactory.add(FunType.fun(ValueType.Number, anyArray, AnyType.INSTANCE)));

        // String
        env.init("length", TypeFactory.add(FunType.fun(ValueType.Number, stringOrArray)));
        env.init("substring", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number)));
        env.init("toString", TypeFactory.fromString("(%s)->%s".formatted(
                ValueType.Number.getValue(), ValueType.String.getValue())));
        env.init("toUpperCase", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("toLowerCase", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("trim", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("replace", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.String, ValueType.String)));
        env.init("split", TypeFactory.add(FunType.fun(anyArray, ValueType.String, ValueType.String)));
        env.init("indexOf", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String, ValueType.String)));
        env.init("startsWith", TypeFactory.add(FunType.fun(ValueType.Boolean, ValueType.String, ValueType.String)));
        env.init("endsWith", TypeFactory.add(FunType.fun(ValueType.Boolean, ValueType.String, ValueType.String)));
        env.init("repeat", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number)));
        env.init("padStart", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number, ValueType.String)));
        env.init("padEnd", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number, ValueType.String)));
        env.init("charAt", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number)));
        env.init("matches", TypeFactory.add(FunType.fun(ValueType.Boolean, ValueType.String, ValueType.String)));
        env.init("format", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, AnyType.INSTANCE)));

        // Datetime: all accept/return strings (datetime represented as ISO string)
        env.init("now", TypeFactory.add(FunType.fun(ValueType.String)));
        env.init("year", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("month", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("day", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("hour", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("minute", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("second", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String)));
        env.init("formatDate", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.String)));
        env.init("timestamp", TypeFactory.add(FunType.fun(ValueType.Number)));
        env.init("date", TypeFactory.add(FunType.fun(ValueType.String, ValueType.Number, ValueType.Number, ValueType.Number)));
        env.init("addDays", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number)));
        env.init("diffDays", TypeFactory.add(FunType.fun(ValueType.Number, ValueType.String, ValueType.String)));
        env.init("isLeapYear", TypeFactory.add(FunType.fun(ValueType.Boolean, ValueType.Number)));
        env.init("dayOfWeek", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("parseDate", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.String)));
        env.init("toISOString", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));

        // Type checking: (any) -> boolean
        env.init("isString", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("isNumber", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("isBoolean", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("isArray", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("isObject", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("isNull", TypeFactory.add(FunType.fun(ValueType.Boolean, AnyType.INSTANCE)));
        env.init("toNumber", TypeFactory.add(FunType.fun(ValueType.Number, AnyType.INSTANCE)));

        // Object manipulation
        env.init("keys", TypeFactory.add(FunType.fun(anyArray, ObjectType.INSTANCE)));
        env.init("values", TypeFactory.add(FunType.fun(anyArray, ObjectType.INSTANCE)));
        env.init("entries", TypeFactory.add(FunType.fun(anyArray, ObjectType.INSTANCE)));
        env.init("merge", TypeFactory.add(FunType.fun(ObjectType.INSTANCE, ObjectType.INSTANCE, ObjectType.INSTANCE)));
        env.init("hasKey", TypeFactory.add(FunType.fun(ValueType.Boolean, ObjectType.INSTANCE, AnyType.INSTANCE)));
        env.init("get", TypeFactory.add(FunType.fun(AnyType.INSTANCE, ObjectType.INSTANCE, AnyType.INSTANCE)));

        // Utility
        env.init("uuid", TypeFactory.add(FunType.fun(ValueType.String)));
        env.init("base64Encode", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("base64Decode", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("hash", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("environment", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("fileExists", TypeFactory.add(FunType.fun(ValueType.Boolean, ValueType.String)));
        env.init("readFile", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String)));
        env.init("fromJson", TypeFactory.add(FunType.fun(AnyType.INSTANCE, ValueType.String)));
        env.init("toJson", TypeFactory.add(FunType.fun(ValueType.String, AnyType.INSTANCE)));
    }
}
