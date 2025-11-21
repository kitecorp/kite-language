package io.kite.runtime.decorators;

import java.util.Set;

public interface ProviderSupport {
    Set<String> getProviders();

    void setProviders(Set<String> providers);

    void addProvider(String provider);
}
