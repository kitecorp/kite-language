package io.kite.Runtime.Providers;

import java.util.Set;

public interface SupportsProviders {
    Set<String> getProviders();

    void setProviders(Set<String> providers);

    void addProvider(String provider);
}
