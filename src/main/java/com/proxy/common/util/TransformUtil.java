package com.proxy.common.util;

import com.proxy.common.constant.ProxyConstans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @PACKAGE_NAME: com.proxy.common.util
 * @Description
 * @Author 周志成
 * @DATE: 2020/8/21 11:23
 * @PROJECT_NAME: proxy
 **/

public class TransformUtil {
    public static byte[] replace(byte[] data,int indexLengthHead,int indexLengthEnd,int lengthLength) {
        boolean flag = false;
        //System.out.println("报文字节：" + HexUtil.bytesToHexString(data));
        //System.out.println("报文字节：" + Arrays.toString(data));
        //报文中数据起始位置
        int index = 0;
        byte[] bytes = new byte[4];
        //获取报文中数据起始位置
        for (int i = 0; i < data.length; i += 1) {
            bytes[0] = data[i];
            bytes[1] = data[i + 1];
            bytes[2] = data[i + 2];
            bytes[3] = data[i + 3];
            String flagIndex = HexUtil.bytesToHexString(bytes);
            index = i;
            if (ProxyConstans.PROXY_HEAD_END.equals(flagIndex)) {
                flag = true;
                break;
            }
        }
        //如果没拿到起始位置则不处理
        if (flag) {
            index += 4;
            //System.out.println("获取到数据位置：" + index);
            //报文中数据的大小字节数组
            byte[] trans = new byte[data.length - index];
            //将报文中的数据拷贝到数组
            System.arraycopy(data, index, trans, 0, data.length - index);
            //System.out.println("数据长度：" + (data.length - index));
            //解压缩的字符串，可以用来脱敏
            String uncompress = uncompress(trans);
            //脱敏操作
            String replace = uncompress.replace("张", "*");
            //将脱敏后的字符串进行压缩
            byte[] compress = compress(replace);
            //System.out.println("改造后的字节："+Arrays.toString(compress));
            /*System.out.println("*****替换后的字节数组：" + compress.length + "----" + HexUtil.bytesToHexString2(compress));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("@@@@@解析前的字节数组：" + trans.length + "----" + Arrays.toString(trans));
            System.out.println("*****替换后的字节数组：" + compress.length + "----" + Arrays.toString(compress));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("@@@@@解析前的字符串：" + trans.length + "----" + uncompress);
            System.out.println("*****替换后的字符串：" + compress.length + "----" + uncompress1);
            System.out.println("trans长度:" + trans.length + "&&" + trans[trans.length - 1]);
            System.out.println("compress长度：:" + compress.length + "&&" + compress[compress.length - 1]);
            System.out.println("----------------------------------------------------------------------------------------------------");*/

            String dateLength = Integer.toString(compress.length);
            //脱敏后的数据
            byte[] transData = null;
            if ((lengthLength)>dateLength.length()){
                 transData = new byte[index + compress.length+((lengthLength)-dateLength.length())];
            }else {
                 transData = new byte[index + compress.length+(dateLength.length()-(lengthLength))];
            }
            //拷贝报文中长度信息之前的数据，因为脱敏后报文长度会发生变化
            System.arraycopy(data, 0, transData, 0, indexLengthHead);
            //System.out.println("1改造后的："+indexLengthHead+"---"+Arrays.toString(transData));
            //脱敏后的数据长度
            byte[] dateLengthBytes = dateLength.getBytes();
            //将脱敏后的数据长度放入报文
            for (int i = 0; i < dateLengthBytes.length; i++) {
                transData[indexLengthHead+i] = dateLengthBytes[i];
            }
            //将报文中长度之后的信息放入报文
            System.arraycopy(data,indexLengthEnd,transData,indexLengthHead+dateLengthBytes.length,index-indexLengthEnd);
            //System.out.println("2改造后的："+Arrays.toString(transData));
            //将脱敏后的数据放入报文
            System.arraycopy(compress,0,transData,(index-indexLengthEnd)+indexLengthHead+dateLengthBytes.length,compress.length);
            //System.out.println("3改造后的："+Arrays.toString(transData));
            //System.out.println("改造前的："+Arrays.toString(data));
            data = transData;
            //System.out.println("改造后的："+Arrays.toString(transData));
        }
        return data;
    }

    /**
     * 使用gzip进行解压缩
     */
    public static String uncompress(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = data;
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        return decompressed;
    }

    /**
     * 使用gzip进行压缩
     */
    public static byte[] compress(String primStr) {
        if (primStr == null || primStr.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(primStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out.toByteArray();
    }
}
