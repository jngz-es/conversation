/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.transport;

import org.opensearch.action.ActionType;
import org.opensearch.conversation.response.GetSessionHistoryResponse;

public class GetSessionHistoryAction extends ActionType<GetSessionHistoryResponse> {
    public static GetSessionHistoryAction INSTANCE = new GetSessionHistoryAction();
    public static final String NAME = "cluster:admin/opensearch/conversation/history";

    private GetSessionHistoryAction() {
        super(NAME, GetSessionHistoryResponse::new);
    }
}
