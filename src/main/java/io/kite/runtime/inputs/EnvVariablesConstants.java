package io.kite.runtime.inputs;

public class EnvVariablesConstants {
    /**
     * Variable used to filter System environment variables by prefix. All env variables
     * with this prefix will be considered as input variables.
     * The prefix is KITE_INPUT_<INPUT_NAME_UPPERCASE> followed by the variable name.
     */
    static final String KITE_INPUT = "KITE_INPUT_";
    static final String KITE_PROFILE = "KITE_PROFILE";
}
