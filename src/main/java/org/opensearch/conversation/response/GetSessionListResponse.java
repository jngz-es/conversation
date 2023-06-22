/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.response;

import static org.opensearch.conversation.common.CommonValue.CREATED_TIME_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSIONS_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_TITLE_FIELD;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import org.opensearch.action.ActionResponse;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;

@Getter
@ToString
public class GetSessionListResponse extends ActionResponse implements ToXContentObject {

    private List<Element> sessions;

    @Builder
    public GetSessionListResponse(List<Element> sessions) {
        this.sessions = sessions;
    }

    public GetSessionListResponse(StreamInput in) throws IOException {
        this.sessions = in.readList(Element::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeList(sessions);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(SESSIONS_FIELD, sessions);
        builder.endObject();
        return builder;
    }

    @Data
    public static class Element implements ToXContentObject, Writeable {

        private String sessionId;
        private String title;
        private Instant createdTime;

        @Builder(toBuilder = true)
        public Element(String sessionId, String title, Instant createdTime) {
            this.sessionId = sessionId;
            this.title = title;
            this.createdTime = createdTime;
        }

        public Element(StreamInput input) throws IOException {
            this.sessionId = input.readString();
            this.title = input.readString();
            this.createdTime = input.readInstant();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeString(sessionId);
            out.writeString(title);
            out.writeInstant(createdTime);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(SESSION_ID_FIELD, sessionId);
            builder.field(SESSION_TITLE_FIELD, sessionId);
            builder.field(CREATED_TIME_FIELD, createdTime.toEpochMilli());
            builder.endObject();
            return builder;
        }
    }
}
