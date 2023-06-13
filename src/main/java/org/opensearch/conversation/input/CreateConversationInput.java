package org.opensearch.conversation.input;

import lombok.Builder;
import lombok.Data;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

import java.io.IOException;

import static org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken;

@Data
public class CreateConversationInput implements ToXContentObject, Writeable {
    public static final String USER_ID_FIELD = "user_id";
    public static final String MODEL_ID_FIELD = "model_id";

    private String userId;
    private String modelId;

    @Builder(toBuilder = true)
    public CreateConversationInput(String userId, String modelId) {
        this.userId = userId;
        this.modelId = modelId;
    }

    public CreateConversationInput (StreamInput in) throws IOException {
        this.userId = in.readString();
        this.modelId = in.readString();
    }

    public static CreateConversationInput parse(XContentParser parser) throws IOException {
        String userId = null;
        String modelId = null;

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case USER_ID_FIELD:
                    userId = parser.text();
                    break;
                case MODEL_ID_FIELD:
                    modelId = parser.text();
                    break;
                default:
                    parser.skipChildren();
                    break;
            }
        }

        return new CreateConversationInput(userId, modelId);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(userId);
        out.writeString(modelId);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (userId != null) {
            builder.field(USER_ID_FIELD, userId);
        }
        if (modelId != null) {
            builder.field(MODEL_ID_FIELD, modelId);
        }
        builder.endObject();
        return builder;
    }
}
