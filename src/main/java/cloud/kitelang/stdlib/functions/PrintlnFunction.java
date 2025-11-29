package cloud.kitelang.stdlib.functions;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.values.ResourceValue;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PrintlnFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() == 1) {
            var result = args.get(0);
            if (result instanceof ResourceValue value) {
                System.out.printf("resource %s %s {\n", value.getSchema().getType(), value.getName()); // todo improve this to use the printer
                for (Object o : value.getProperties().getVariables().entrySet()) {
                    System.out.println("\t" + o);
                }
                System.out.println("}");
            } else {
                System.out.println(result);
            }
            return result;
        }
        var result = StringUtils.join(args);
        System.out.println(result);
        return result;
    }
}
