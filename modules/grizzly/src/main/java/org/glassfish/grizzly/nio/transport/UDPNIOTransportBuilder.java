/*
 * Copyright (c) 2011, 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.grizzly.nio.transport;

import org.glassfish.grizzly.NIOTransportBuilder;
import org.glassfish.grizzly.nio.NIOTransport;


/**
 * {@link NIOTransportBuilder} implementation for <code>UDP</code>.
 *
 * @since 2.0
 */
public class UDPNIOTransportBuilder extends NIOTransportBuilder<UDPNIOTransportBuilder> {


    // ------------------------------------------------------------ Constructors


    protected UDPNIOTransportBuilder(Class<? extends UDPNIOTransport> transportClass) {
        super(transportClass);
    }


    // ---------------------------------------------------------- Public Methods

    public static UDPNIOTransportBuilder newInstance() {
            return new UDPNIOTransportBuilder(UDPNIOTransport.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UDPNIOTransport build() {
        return (UDPNIOTransport) super.build();
    }


    // ------------------------------------------------------- Protected Methods


    @Override
    protected UDPNIOTransportBuilder getThis() {
        return this;
    }

    @Override
    protected NIOTransport create(final String name) {
        return new UDPNIOTransport(name);
    }
}
