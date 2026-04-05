package com.github.vitorpereiraa.sombra.agent.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record RequestPath(String value) {

    public RequestPath {
        checkNotNull(value, "RequestPath value cannot be null");
        checkArgument(!value.isBlank(), "RequestPath value cannot be blank");
    }

    public static RequestPath from(String uri, String queryString) {
        checkNotNull(uri, "URI cannot be null");
        if (queryString != null && !queryString.isBlank()) {
            return new RequestPath(uri + "?" + queryString);
        }
        return new RequestPath(uri);
    }
}
