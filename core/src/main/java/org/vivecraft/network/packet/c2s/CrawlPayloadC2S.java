package org.vivecraft.network.packet.c2s;

import org.vivecraft.network.packet.PayloadIdentifier;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * holds if the client started or stopped crawling
 *
 */
public final class CrawlPayloadC2S implements VivecraftPayloadC2S {
    public final boolean crawling;

    /**
     * @param crawling if the player started or stopped crawling
     */
    public CrawlPayloadC2S(boolean crawling) {
        this.crawling = crawling;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CRAWL;
    }

    public static CrawlPayloadC2S read(DataInputStream buffer) throws IOException {
        return new CrawlPayloadC2S(buffer.readBoolean());
    }
}
