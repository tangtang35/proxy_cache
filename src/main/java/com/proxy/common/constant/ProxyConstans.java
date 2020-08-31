package com.proxy.common.constant;

/**
 * @PACKAGE_NAME: com.proxy.common.constant
 * @Description
 * @Author 周志成
 * @DATE: 2020/8/24 9:25
 * @PROJECT_NAME: proxy
 **/

public class ProxyConstans {
    /**
     * @Description: http的16进制表示
     * @Author: 周志成
     * @Date: 2020/8/24 9:27
     **/
    public final static String PROXY_HTTP="48545450";
    /**
     * @Description: 判断请求头是否包含长度字段
     * @Author: 周志成
     * @Date: 2020/8/24 10:09
     **/
    public final static String PROXY_CONTENT_LENGTH="CONTENT-LENGTH: ";
    /**
     * @Description: 判断请求头中类型是否为application/x-csp-hyperevent
     * @Author: 周志成
     * @Date: 2020/8/24 10:10
     **/
    public final static String PROXY_CONTENT_TYPE="application/x-csp-hyperevent";
    /**
     * @Description: 结束标识符
     * @Author: 周志成
     * @Date: 2020/8/24 10:17
     **/
    public final static String PROXY_END="0d0a";
    /**
     * @Description: 请求头结束标识符
     * @Author: 周志成
     * @Date: 2020/8/24 10:17
     **/
    public final static String PROXY_HEAD_END="0d0a0d0a";
}
