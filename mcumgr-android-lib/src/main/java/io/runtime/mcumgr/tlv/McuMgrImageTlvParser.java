/*
 * Copyright (c) Intellinium SAS, 2014-present
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.tlv;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;

import io.runtime.mcumgr.cfg.McuMgrConfig;
import io.runtime.mcumgr.def.McuMgrImageHeader;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.util.ByteUtil;

public class McuMgrImageTlvParser {

    private static final String TAG = McuMgrImageTlvParser.class.getSimpleName();
    private final byte[] mData;

    public McuMgrImageTlvParser(@NonNull byte[] mData) {
        this.mData = mData;
    }


    public byte[] hash() throws McuMgrException {
        boolean hashFound = false;
        byte[] hash = new byte[McuMgrConfig.IMAGE_HASH_LEN];

        McuMgrImageHeader header = McuMgrImageHeader.fromBytes(mData);

		/* Read the image's TLVs.  All images are required to have a hash TLV.  If
         * the hash is missing, the image is considered invalid.
     	 */
        int offset = header.getHdrSize() + header.getImgSize();
        int end;

        McuMgrImageTlvInfo info = findTlvs(offset);
        offset += McuMgrImageTlvInfo.sizeof();
        end = offset + info.getTotal();

        while (offset + McuMgrImageTlvTrailer.sizeof() <= end) {
            McuMgrImageTlvTrailer tlv = McuMgrImageTlvTrailer.fromBytes(mData, offset);

            if (tlv.getType() == 0xFF && tlv.getLen() == 0xFFFF) {
                throw new McuMgrException(/* TODO*/ "Error here!");
            }

            if (tlv.getType() != McuMgrConfig.IMAGE_TLV_SHA256 || tlv.getLen() != McuMgrConfig.IMAGE_HASH_LEN) {
                offset += McuMgrImageTlvTrailer.sizeof() + tlv.getLen();
                continue;
            }

            if (hashFound) {
                throw new McuMgrException("Multiple hashes found in image");
            }

            hashFound = true;
            offset += McuMgrImageTlvTrailer.sizeof();

            if (offset + McuMgrConfig.IMAGE_HASH_LEN > end) {
                throw new McuMgrException("The remaining data is to short to be a hash");
            }

            hash = Arrays.copyOfRange(mData, offset, McuMgrConfig.IMAGE_HASH_LEN + offset);
        }

        if (!hashFound) {
            throw new McuMgrException("No hash found for image");
        }

        Log.d(TAG, "hash found: " + ByteUtil.byteArrayToHex(hash));
        return hash;
    }

    private McuMgrImageTlvInfo findTlvs(int offset) throws McuMgrException {
        if(McuMgrConfig.HAS_TLV_INFO) {
            return McuMgrImageTlvInfo.fromBytes(mData, offset);
        }

        return McuMgrImageTlvInfo.absent();
    }
}
