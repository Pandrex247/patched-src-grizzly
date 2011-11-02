/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.grizzly.http.ajp;

import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.util.buf.ByteChunk;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Response} implementation, which also contains AJP
 * related meta data.
 *
 * @author Alexey Stashok
 */
public final class AjpHttpResponse extends Response {
    private final static byte[] JK_AJP13_CPONG_REPLY_CONTENT =
            new byte[] {'A', 'B', 0, 1, AjpConstants.JK_AJP13_CPONG_REPLY};
    
    private boolean secure;
    
    final ByteChunk tmpHeaderByteChunk =
            new ByteChunk(AjpConstants.MAX_PACKET_SIZE);

    public void setSecure(final boolean secure) {
        this.secure = secure;
    }
    
    static void writeCPongReply(final OutputStream outputStream) throws IOException {
        outputStream.write(JK_AJP13_CPONG_REPLY_CONTENT);
    }

    @Override
    public void reset() throws IllegalStateException {
        tmpHeaderByteChunk.recycle();
        super.reset();
    }
    
    @Override
    public void recycle() {
        tmpHeaderByteChunk.recycle();
        super.recycle();
    }
    
    
}
