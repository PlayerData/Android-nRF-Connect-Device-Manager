/*
 * Copyright (c) Intellinium SAS, 2014-present
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class McuMgrCoapBaseResponse extends McuMgrSimpleResponse {
    public byte[] _h;

    /* TODO */
    @Override
    public boolean isSuccess() {
        return false;
    }
}
