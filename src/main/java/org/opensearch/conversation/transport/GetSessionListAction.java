/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.transport;

import org.opensearch.action.ActionType;
import org.opensearch.conversation.response.GetSessionListResponse;

public class GetSessionListAction extends ActionType<GetSessionListResponse> {
    public static GetSessionListAction INSTANCE = new GetSessionListAction();
    public static final String NAME = "cluster:admin/opensearch/conversation/sessions";

    private GetSessionListAction() {
        super(NAME, GetSessionListResponse::new);
    }
}
