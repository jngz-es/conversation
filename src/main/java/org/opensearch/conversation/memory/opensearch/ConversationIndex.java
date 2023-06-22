/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.memory.opensearch;

import static org.opensearch.conversation.common.CommonValue.MESSAGE_INDEX;
import static org.opensearch.conversation.common.CommonValue.MESSAGE_INDEX_SCHEMA_VERSION;
import static org.opensearch.conversation.common.CommonValue.SESSION_METADATA_INDEX_SCHEMA_VERSION;
import static org.opensearch.conversation.common.CommonValue.SESSION_METADATA_INDEX;

public enum ConversationIndex {
    METADATA(SESSION_METADATA_INDEX, false, SESSION_METADATA_INDEX, SESSION_METADATA_INDEX_SCHEMA_VERSION),
    MESSAGE(MESSAGE_INDEX, false, MESSAGE_INDEX, MESSAGE_INDEX_SCHEMA_VERSION);

    private final String indexName;
    // whether we use an alias for the index
    private final boolean alias;
    private final String mapping;
    private final Integer version;

    ConversationIndex(String name, boolean alias, String mapping, Integer version) {
        this.indexName = name;
        this.alias = alias;
        this.mapping = mapping;
        this.version = version;
    }

    public String getIndexName() {
        return indexName;
    }

    public boolean isAlias() {
        return alias;
    }

    public String getMapping() {
        return mapping;
    }

    public Integer getVersion() {
        return version;
    }
}
