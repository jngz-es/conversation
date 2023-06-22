/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.input;

import static org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken;
import static org.opensearch.conversation.common.CommonValue.ML_PARAMETERS_FIELD;
import static org.opensearch.conversation.common.CommonValue.MODEL_ID_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

@Data
public class ChatInput implements ToXContentObject, Writeable {

    private String sessionId;
    private String modelId;
    private Map<String, String> parameters;

    @Builder(toBuilder = true)
    public ChatInput(String sessionId, String modelId, Map<String, String> parameters) {
        this.sessionId = sessionId;
        this.modelId = modelId;
        this.parameters = parameters;
    }

    public ChatInput(StreamInput in) throws IOException {
        this.sessionId = in.readString();
        this.modelId = in.readString();
        this.parameters = in.readMap(s -> s.readString(), s -> s.readString());
    }

    public static ChatInput parse(XContentParser parser) throws IOException {
        String userId = null;
        String modelId = null;
        Map<String, String> parameters = new HashMap<>();

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case SESSION_ID_FIELD:
                    userId = parser.text();
                    break;
                case MODEL_ID_FIELD:
                    modelId = parser.text();
                    break;
                case ML_PARAMETERS_FIELD:
                    parameters = parser.mapStrings();
                    break;
                default:
                    parser.skipChildren();
                    break;
            }
        }

        return new ChatInput(userId, modelId, parameters);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(sessionId);
        out.writeString(modelId);
        out.writeMap(parameters, StreamOutput::writeString, StreamOutput::writeString);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (sessionId != null) {
            builder.field(SESSION_ID_FIELD, sessionId);
        }
        if (modelId != null) {
            builder.field(MODEL_ID_FIELD, modelId);
        }
        if (parameters != null) {
            builder.field(ML_PARAMETERS_FIELD, parameters);
        }
        builder.endObject();
        return builder;
    }
}
