/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.request;

import static org.opensearch.action.ValidateActions.addValidationError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.InputStreamStreamInput;
import org.opensearch.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class GetSessionHistoryRequest extends ActionRequest {
    private String sessionId;
    private int from;
    private int size;

    @Builder
    public GetSessionHistoryRequest(String sessionId, int from, int size) {
        this.sessionId = sessionId;
        this.from = from;
        this.size = size;
    }

    public GetSessionHistoryRequest(StreamInput in) throws IOException {
        super(in);
        this.sessionId = in.readString();
        this.from = in.readInt();
        this.size = in.readInt();
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException exception = null;
        if (sessionId.isEmpty()) {
            exception = addValidationError("session id can not be empty", exception);
        } else if (from < 0 || size <= 0) {
            exception = addValidationError("from can not be less than 0, size can not be less than or equal to 0", exception);
        }

        return exception;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(sessionId);
        out.writeInt(from);
        out.writeInt(size);
    }

    public static GetSessionHistoryRequest fromActionRequest(ActionRequest actionRequest) {
        if (actionRequest instanceof GetSessionHistoryRequest) {
            return (GetSessionHistoryRequest) actionRequest;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); OutputStreamStreamOutput osso = new OutputStreamStreamOutput(baos)) {
            actionRequest.writeTo(osso);
            try (StreamInput input = new InputStreamStreamInput(new ByteArrayInputStream(baos.toByteArray()))) {
                return new GetSessionHistoryRequest(input);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse ActionRequest into GetSessionHistoryRequest", e);
        }
    }
}
