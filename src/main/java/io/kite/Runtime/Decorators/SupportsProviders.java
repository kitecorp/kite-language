package io.kite.Runtime.Decorators;

import java.util.Set;

public interface SupportsProviders {
    Set<String> getProviders();

    void setProviders(Set<String> providers);

    void addProvider(String provider);
}
