/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.entitywriter;

import com.google.common.io.Files;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.mime.MimeMapper;
import org.wso2.msf4j.internal.mime.MimeMappingException;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.ws.rs.core.MediaType;

/**
 * EntityWriter for entity of type File.
 */
public class FileEntityWriter implements EntityWriter<File> {

    public static final int DEFAULT_CHUNK_SIZE = 1024;

    /**
     * Supported entity type.
     */
    @Override
    public Class<File> getType() {
        return File.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(CarbonMessage carbonMessage, File file, String mediaType, int chunkSize) throws Exception {
        if (mediaType == null || mediaType.equals(MediaType.WILDCARD)) {
            try {
                mediaType = MimeMapper.getMimeType(Files.getFileExtension(file.getName()));
            } catch (MimeMappingException e) {
                mediaType = MediaType.WILDCARD;
            }
        }
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        if (chunkSize == Response.NO_CHUNK || chunkSize == Response.DEFAULT_CHUNK_SIZE) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        }
        ByteBuffer buffer = ByteBuffer.allocate(chunkSize);
        while (fileChannel.read(buffer) != -1) {
            buffer.flip();
            carbonMessage.addMessageBody(buffer);
            buffer.clear();
        }
        fileChannel.close();
        carbonMessage.setEndOfMsgAdded(true);
        carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
        carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, mediaType);
    }
}
