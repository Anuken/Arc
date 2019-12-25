/* Copyright (c) 2008, Nathan Sweet
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package arc.net;

import java.net.DatagramPacket;

public interface ClientDiscoveryHandler{
    /**
     * Implementations of this method should return a new {@link DatagramPacket}
     * that the {@link Client} will use to fill with the incoming packet data
     * sent by the {@link ServerDiscoveryHandler}.
     * @return a new {@link DatagramPacket}
     */
    DatagramPacket newDatagramPacket();

    /**
     * Called when the {@link Client} discovers a host.
     * @param datagramPacket the same {@link DatagramPacket} from
     * {@link #newDatagramPacket()}, after being filled with
     * the incoming packet data.
     */
    void discoveredHost(DatagramPacket datagramPacket);

    /**
     * Called right before the {@link Client#discoverHost(int, int)} or
     * {@link Client#discoverHosts(int, int)} method exits. This allows the
     * implementation to clean up any resources used.
     */
    void finish();

}
