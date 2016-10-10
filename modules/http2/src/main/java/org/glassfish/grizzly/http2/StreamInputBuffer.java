/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.http2;

import java.io.IOException;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.HttpContent;

/**
 *
 * @author oleksiys
 */
interface StreamInputBuffer {
    /**
     * The method will be invoked once upstream completes READ operation processing.
     * Here we have to simulate NIO OP_READ event re-registration.
     */
    void onReadEventComplete();

    /**
     * The method is called, when new input data arrives.
     */
    boolean offer(final Buffer data, final boolean isLast);

    /**
     * Retrieves available input buffer payload, waiting up to the
     * {@link Connection#getReadTimeout(java.util.concurrent.TimeUnit)}
     * wait time if necessary for payload to become available.
     * 
     * @throws IOException
     */
    HttpContent poll() throws IOException;

    /**
     * Graceful input buffer close.
     * 
     * Marks the input buffer as closed by adding Termination input element to the input queue.
     */
    void close(final Http2Stream.Termination termination);

    /**
     * Forcibly closes the input buffer.
     * 
     * All the buffered data will be discarded.
     */
    void terminate(final Http2Stream.Termination termination);
    
    /**
     * Returns <tt>true</tt> if the <tt>InputBuffer</tt> has been closed.
     */
    boolean isClosed();
}
