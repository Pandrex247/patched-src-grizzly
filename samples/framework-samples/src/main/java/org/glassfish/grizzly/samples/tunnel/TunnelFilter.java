/*
 * Copyright (c) 2009, 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.grizzly.samples.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.SocketConnectorHandler;
import org.glassfish.grizzly.WriteHandler;
import org.glassfish.grizzly.asyncqueue.AsyncQueueWriter;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

/**
 * Simple tunneling filter, which maps input of one connection to the output of
 * another and vise versa.
 *
 * @author Alexey Stashok
 */
public class TunnelFilter extends BaseFilter {
    private static final Logger logger = Grizzly.logger(TunnelFilter.class);
    
    private final Attribute<Connection> peerConnectionAttribute =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("TunnelFilter.peerConnection");

    // Transport, which will be used to create peer connection
    private final SocketConnectorHandler transport;

    // Destination address for peer connections
    private final SocketAddress redirectAddress;

    public TunnelFilter(SocketConnectorHandler transport, String host, int port) {
        this(transport, new InetSocketAddress(host, port));
    }

    public TunnelFilter(SocketConnectorHandler transport, SocketAddress redirectAddress) {
        this.transport = transport;
        this.redirectAddress = redirectAddress;
    }
    
    /**
     * This method will be called, once {@link Connection} has some available data
     */
    @SuppressWarnings("unchecked")
    @Override
    public NextAction handleRead(final FilterChainContext ctx)
            throws IOException {
        logger.log(Level.FINEST, "Connection: {0} handleRead: {1}",
                new Object[]{ctx.getConnection(), ctx.getMessage()});
        
        final Connection connection = ctx.getConnection();
        final Connection peerConnection = peerConnectionAttribute.get(connection);

        // if connection is closed - stop the execution
        if (!connection.isOpen()) {
            return ctx.getStopAction();
        }

        final NextAction suspendNextAction = ctx.getSuspendAction();
        
        // if peerConnection wasn't created - create it (usually happens on first connection request)
        if (peerConnection == null) {
            // "Peer connect" phase could take some time - so execute it in non-blocking mode

            // Connect peer connection and register completion handler
            transport.connect(redirectAddress, new ConnectCompletionHandler(ctx));

            // return suspend status
            return suspendNextAction;
        }

        final Object message = ctx.getMessage();

        // if peer connection is already created - just forward data to peer
        redirectToPeer(ctx, peerConnection, message);

        final AsyncQueueWriter writer =
                (AsyncQueueWriter) connection.getTransport().getWriter(false);
        
        if (writer.canWrite(peerConnection)) {
            return ctx.getStopAction();
        }
        
        // Make sure we don't overload peer's output buffer and do not cause OutOfMemoryError
        ctx.suspend();
        writer.notifyWritePossible(peerConnection, new WriteHandler() {

            @Override
            public void onWritePossible() throws Exception {
                finish();
            }

            @Override
            public void onError(Throwable t) {
                finish();
            }

            private void finish() {
                ctx.resumeNext();
            }
        });

//        return ctx.getStopAction();
        return suspendNextAction;
    }

    /**
     * This method will be called, to notify about {@link Connection} closing.
     */
    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        final Connection connection = ctx.getConnection();
        final Connection peerConnection = peerConnectionAttribute.get(connection);

        // Close peer connection as well, if it wasn't closed before
        if (peerConnection != null && peerConnection.isOpen()) {
            peerConnection.closeSilently();
        }

        return ctx.getInvokeAction();
    }

    /**
     * Redirect data from {@link Connection} to its peer.
     *
     * @param context {@link FilterChainContext}
     * @param peerConnection peer {@link Connection}
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private static void redirectToPeer(final FilterChainContext context,
            final Connection peerConnection, Object message) throws IOException {

        final Connection srcConnection = context.getConnection();
        logger.log(Level.FINE, "Redirecting from {0} to {1} message: {2}",
                new Object[]{srcConnection.getPeerAddress(), peerConnection.getPeerAddress(), message});

        peerConnection.write(message);
    }
    
    /**
     * Peer connect {@link CompletionHandler}
     */
    private class ConnectCompletionHandler implements CompletionHandler<Connection> {
        private final FilterChainContext context;
        
        private ConnectCompletionHandler(FilterChainContext context) {
            this.context = context;
        }

        @Override
        public void cancelled() {
            context.getConnection().closeSilently();
            resumeContext();
        }

        @Override
        public void failed(Throwable throwable) {
            context.getConnection().closeSilently();
            resumeContext();
        }

        /**
         * If peer was successfully connected - map both connections to each other.
         */
        @Override
        public void completed(Connection peerConnection) {
            final Connection connection = context.getConnection();

            // Map connections
            peerConnectionAttribute.set(connection, peerConnection);
            peerConnectionAttribute.set(peerConnection, connection);

            // Resume filter chain execution
            resumeContext();
        }

        @Override
        public void updated(Connection peerConnection) {
        }

        /**
         * Resume {@link org.glassfish.grizzly.filterchain.FilterChain} execution on stage, where it was
         * earlier suspended.
         */
        private void resumeContext() {
            context.resume();
        }
    }
}
