/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.response;

import static org.opensearch.conversation.common.CommonValue.ANSWER_FIELD;
import static org.opensearch.conversation.common.CommonValue.CREATED_TIME_FIELD;
import static org.opensearch.conversation.common.CommonValue.QUESTION_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;
import static org.opensearch.conversation.common.CommonValue.STEPS_FIELD;

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
public class GetSessionHistoryResponse extends ActionResponse implements ToXContentObject {

    private String sessionId;
    private List<Element> steps;

    @Builder
    public GetSessionHistoryResponse(String sessionId, List<Element> steps) {
        this.sessionId = sessionId;
        this.steps = steps;
    }

    public GetSessionHistoryResponse(StreamInput in) throws IOException {
        this.sessionId = in.readString();
        this.steps = in.readList(Element::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(sessionId);
        out.writeList(steps);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(SESSION_ID_FIELD, sessionId);
        builder.field(STEPS_FIELD, steps);
        builder.endObject();
        return builder;
    }

    @Data
    public static class Element implements ToXContentObject, Writeable {

        private String question;
        private String answer;
        private Instant createdTime;

        @Builder(toBuilder = true)
        public Element(String question, String answer, Instant createdTime) {
            this.question = question;
            this.answer = answer;
            this.createdTime = createdTime;
        }

        public Element(StreamInput input) throws IOException {
            this.question = input.readString();
            this.answer = input.readString();
            this.createdTime = input.readInstant();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeString(question);
            out.writeString(answer);
            out.writeInstant(createdTime);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(QUESTION_FIELD, question);
            builder.field(ANSWER_FIELD, answer);
            builder.field(CREATED_TIME_FIELD, createdTime.toEpochMilli());
            builder.endObject();
            return builder;
        }
    }
}
