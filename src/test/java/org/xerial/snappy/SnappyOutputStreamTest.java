/*--------------------------------------------------------------------------
 *  Copyright 2011 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// XerialJ
//
// SnappyOutputStreamTest.java
// Since: 2011/03/31 18:26:31
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.snappy;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.xerial.util.FileResource;
import org.xerial.util.log.Logger;

public class SnappyOutputStreamTest
{
    private static Logger _logger = Logger.getLogger(SnappyOutputStreamTest.class);

    @Test
    public void test() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        SnappyOutputStream sout = new SnappyOutputStream(buf);

        BufferedInputStream input = new BufferedInputStream(FileResource.find(SnappyOutputStreamTest.class,
                "alice29.txt").openStream());
        assertNotNull(input);

        ByteArrayOutputStream orig = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        for (int readBytes = 0; (readBytes = input.read(tmp)) != -1;) {
            sout.write(tmp, 0, readBytes);
            orig.write(tmp, 0, readBytes); // preserve the original data
        }
        input.close();
        sout.flush();
        orig.flush();

        int compressedSize = buf.size();
        _logger.debug("compressed size: " + compressedSize);

        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] compressed = buf.toByteArray();
        // decompress
        for (int cursor = SnappyCodec.headerSize(); cursor < compressed.length;) {
            int chunkSize = SnappyOutputStream.readInt(compressed, cursor);
            cursor += 4;
            byte[] tmpOut = new byte[Snappy.uncompressedLength(compressed, cursor, chunkSize)];
            int decompressedSize = Snappy.uncompress(compressed, cursor, chunkSize, tmpOut, 0);
            cursor += chunkSize;

            decompressed.write(tmpOut);
        }
        decompressed.flush();
        assertEquals(orig.size(), decompressed.size());
        assertArrayEquals(orig.toByteArray(), decompressed.toByteArray());
    }

}