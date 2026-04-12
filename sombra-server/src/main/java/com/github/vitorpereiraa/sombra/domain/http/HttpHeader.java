package com.github.vitorpereiraa.sombra.domain.http;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public record HttpHeader(String name, List<String> values) {

    public HttpHeader {
        checkArgument(name != null, "HttpHeader name cannot be null");
        checkArgument(!name.isBlank(), "HttpHeader name cannot be blank");
        checkArgument(values != null, "HttpHeader values cannot be null");
        checkArgument(!values.isEmpty(), "HttpHeader values cannot be empty");
        values = List.copyOf(values);
    }
}
