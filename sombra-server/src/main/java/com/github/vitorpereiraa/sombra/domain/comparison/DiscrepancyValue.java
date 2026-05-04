package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.util.List;

public sealed interface DiscrepancyValue {

    record Status(int code) implements DiscrepancyValue {

        public Status {
            checkArgument(code >= 0, "Status code cannot be negative, got: %s", code);
        }
    }

    record Headers(List<String> values) implements DiscrepancyValue {

        public Headers {
            checkArgument(values != null, "Headers values cannot be null");
            values = List.copyOf(values);
        }
    }

    record JsonBody(JsonValue value) implements DiscrepancyValue {

        public JsonBody {
            checkArgument(value != null, "JsonBody value cannot be null");
        }
    }

    record RawBody(String content) implements DiscrepancyValue {

        public RawBody {
            checkArgument(content != null, "RawBody content cannot be null");
        }
    }
}
