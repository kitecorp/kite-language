package io.zmeu.Runtime.Functions;

import io.zmeu.Runtime.Callable;
import io.zmeu.Runtime.Interpreter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PrintlnFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() == 1) {
            var result = args.get(0);
            System.out.println(result);
            return result;
        }
        var result = StringUtils.join(args);
        System.out.println(result);
        return result;
    }
}
