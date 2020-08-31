package com.proxy.client.handler;

import com.proxy.client.service.ClientBeanManager;
import com.proxy.common.constant.ProxyConstans;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.util.HexUtil;
import com.proxy.common.util.ProxyMessageUtil;
import com.proxy.common.util.TransformUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TCPHandler extends ChannelInboundHandlerAdapter {
    //存储完整的报文
    private byte[] fullData = new byte[0];
    //判断报文是否完整
    private boolean flag = false;
    //请求头中报文标识数据的长度
    private int lengthNum = 0;
    //报文中数据长度
    private int lengthData = 0;
    //请求头中报文长度位置开始
    private int indexLengthHead = 0;
    //请求头中报文长度位置开始
    private int indexLengthEnd = 0;
    //请求头中报文长度字符串长度
    private int lengthLength = 0;

    private static Logger logger = LoggerFactory.getLogger(TCPHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {

        Channel realServerChannel = ctx.channel();


        int proxyType = ClientBeanManager.getProxyService().getProxyType(realServerChannel);
        //获取代理客户端和代理服务器之间的通道
        Channel channel = ClientBeanManager.getProxyService().getChannel();

        if (channel == null) {
            // 代理客户端连接断开
            logger.debug("客户端和代理服务器失去连接");
            ctx.channel().close();
            ReferenceCountUtil.release(msg);
        } else {
            //http 消息
            if (proxyType == CommonConstant.ProxyType.HTTP) {
                //向上传递
                ctx.fireChannelRead(msg);
            } else {
                logger.debug("转发TCP消息到代理服务器");
                ByteBuf buf = (ByteBuf) msg;
                //获取的报文(字节数组)
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                buf.release();
                //获取报文前四个字节判断是否包含http关键字
                System.out.println(new String(data));
                byte[] bytes = new byte[4];
                bytes[0] = data[0];
                bytes[1] = data[1];
                bytes[2] = data[2];
                bytes[3] = data[3];
                //判断请求头是否包含http
                String head = HexUtil.bytesToHexString(bytes);
                System.out.println(head);
                if (head.equalsIgnoreCase(ProxyConstans.PROXY_HTTP) && data.length > 468) {
                    byte[] dataBack = new byte[data.length];
                    System.arraycopy(data, 0, dataBack, 0, data.length);
                    String dataStr = new String(dataBack);
                    //System.out.println("------------------"+dataStr);
                    if (dataStr != "" && dataStr.contains(ProxyConstans.PROXY_CONTENT_LENGTH) && dataStr.contains(ProxyConstans.PROXY_CONTENT_TYPE)) {
                        //获取包含数据长度的起始下标
                        indexLengthHead = dataStr.indexOf(ProxyConstans.PROXY_CONTENT_LENGTH) + 16;
                        indexLengthEnd = 0;
                        boolean lengthFlag = false;
                        byte[] bytes2 = new byte[2];
                        //判断长度是否到结束标识符，结束标识符之前的字符串就是报文长度信息
                        for (int i = 0; i < dataBack.length - indexLengthHead; i++) {
                            bytes2[0] = data[indexLengthHead+i];
                            bytes2[1] = data[indexLengthHead + i+1];
                            String endStr = HexUtil.bytesToHexString(bytes2);
                            if (ProxyConstans.PROXY_END.equals(endStr)){
                                indexLengthEnd = indexLengthHead+i;
                                //修改报文长度标识符，如果没有拿到长度则不进行报文解析处理
                                lengthFlag = true;
                                break;
                            }
                        }
                        //如果没有拿到数据长度则不处理
                        if (lengthFlag){
                            lengthLength = indexLengthEnd - indexLengthHead;
                            byte[] lengthBytes = new byte[lengthLength];
                            for (int i = 0; i < lengthLength; i++) {
                                lengthBytes[i] = data[indexLengthHead + i];
                            }
                            String length = new String(lengthBytes);
                            lengthNum = Integer.valueOf(length.trim());
                            String string = HexUtil.bytesToHexString(dataBack);
                            String tansDataStr = string.substring(string.indexOf(ProxyConstans.PROXY_HEAD_END) + 8);
                            lengthData = tansDataStr.length()/2;
                            //System.out.println("数据长度lengthNum:" + lengthNum + "||||" + "当前报文数据长度lengthData:" + lengthData);
                            flag = true;
                            fullData = data;
                            //System.out.println("++++++++++++++++++++"+indexLengthHead+"--"+indexLengthEnd+"--"+lengthLength);
                            //处理报文
                            data = handlerData(fullData,indexLengthHead,indexLengthEnd,lengthLength);
                        }
                    }
                }
                //判断报文是否完整，不完整则继续取下一次报文
                if (!head.equalsIgnoreCase("48545450") && flag) {
                    //报文不完整无法解析，继续获取下一次报文进行拼接
                    //System.out.println("拼接报文");
                    lengthData = data.length + lengthData;
                    //System.out.println("拼接后报文长度："+lengthData);
                    byte[] tmp = new byte[fullData.length + data.length];
                    //拼接报文
                    System.arraycopy(fullData, 0, tmp, 0, fullData.length);
                    System.arraycopy(data, 0, tmp, fullData.length, data.length);
                    fullData = tmp;
                    data = handlerData(fullData,indexLengthHead,indexLengthEnd,lengthLength);
                }
                //一次完整的处理报文后，初始化各个参数
                if (data.length>0){
                    fullData = new byte[0];
                    flag = false;
                    lengthNum = 0;
                    lengthData = 0;
                    indexLengthHead = 0;
                    indexLengthEnd = 0;
                    lengthLength = 0;
                }
                Long sessionID = ClientBeanManager.getProxyService().getRealServerChannelSessionID(realServerChannel);
                ProxyMessage proxyMessage = ProxyMessageUtil.buildMsg(sessionID, CommonConstant.MessageType.TYPE_TRANSFER, null, null, null, data);
                channel.writeAndFlush(proxyMessage);
            }
        }
    }
    /**
     * @Description: 处理报文
     * @Author: 周志成
     * @Date: 2020/8/22 16:34
     * @param fullData: 完整报文
     * @param indexLengthHead: 报文长度起始index
     * @param indexLengthEnd: 报文长度结束index
     * @param lengthLength: 报文长度在字节数据占用字节数
     * @return: byte[]
     **/
    private byte[] handlerData(byte[] fullData,int indexLengthHead,int indexLengthEnd,int lengthLength) {
        byte[] data = new byte[0];
        //数据长度
        if (lengthNum == lengthData) {
            //logger.info("长度相等拼接完成："+lengthNum+"----"+lengthData);
            byte[] replace = TransformUtil.replace(fullData, indexLengthHead, indexLengthEnd, lengthLength);
            data = replace;
            flag = false;

        }
        return data;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("异常:与真实服务器连接断开:" + cause.getMessage());
        removeConnect(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("与真实服务器连接断开");
        removeConnect(ctx);
    }

    private void removeConnect(ChannelHandlerContext ctx) {
        Long sessionID = ClientBeanManager.getProxyService().getRealServerChannelSessionID(ctx.channel());
        ClientBeanManager.getProxyService().removeRealServerChannel(sessionID);

        //
        ProxyMessage proxyMessage = ProxyMessageUtil.buildReConnect(sessionID, null);
        Channel channel = ClientBeanManager.getProxyService().getChannel();
        channel.writeAndFlush(proxyMessage);
    }

}
