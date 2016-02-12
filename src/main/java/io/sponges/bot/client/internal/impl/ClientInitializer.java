package io.sponges.bot.client.internal.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.sponges.bot.client.internal.Client;
import io.sponges.bot.client.Bot;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private final Bot bot;
    private final Client client;
    private final SslContext context;

    public ClientInitializer(Bot bot, Client client, SslContext context) {
        this.bot = bot;
        this.client = client;
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        pipeline.addLast(context.newHandler(channel.alloc(), ClientImpl.HOST, ClientImpl.PORT));

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // and then business logic.
        pipeline.addLast(new ClientHandler(bot, client));
    }
}