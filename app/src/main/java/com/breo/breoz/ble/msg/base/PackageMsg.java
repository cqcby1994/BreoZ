package com.breo.breoz.ble.msg.base;


import com.breo.baseble.breoble.msg.Protocol;
import com.breo.baseble.utils.HexUtil;
import com.breo.breoz.ble.msg.EndianUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * 创建者： gtt
 * 创建时间：2017/4/27
 * 说明：
 */
public abstract class PackageMsg implements Serializable, Cloneable {
//    起始	协议版本	设备类别	命令编号	数据包号	应答标志	命令体										                                校验	    结束
//    1Byte	1Byte	1Byte	1Byte	1Byte	1Byte	参数名1	长度	数值	参数名2	长度	数值	……	参数名N	长度	数值                      	1Byte	1Byte
//    0x4E	0x01	0x01	0x02	0x01	0xFF	0x03	0x01	0x01	0x04	0x01	0x01	……	0xFF	0x01	0x01	0xF6	0x4E

    public static final byte ESCAPE_CODE = 0x4E;//转义字符
    public static final byte ESCAPED_CODE_1 = 0x5E;//将转义字符转义的第一个字符，转义后的第一个字符进行再次转义的字符1
    public static final byte ESCAPED_CODE_2 = 0X4D;//将转义字符转义的第二个字符
    public static final byte ESCAPED_CODE_1_ESCAPED_2 = 0X5D;//将转义后的第一个字符进行再次转义的字符1
    public static final byte COMM_UPLOAD = 0x03;//设备上报
    public static final int COMM_SET = 0X02;//设置命令
    public static final byte COMM_READ=0X01;//读取
    public static int staticPackageId = 0;
    public byte pStart = 0x4E;
    public byte pProtocol = 0x01;
    public byte pDeviceType;
    public byte pComm;
    public byte pPackageId;
    public byte pRespontionFlag;

    public byte pVerify = (byte) 0xF6;
    public byte pEnd = 0x4E;

    public PackageMsg(byte deviceType) {
        pDeviceType = deviceType;
    }

    protected abstract void packsSensor(DataOutputStream dos) throws IOException;

    protected abstract void unpacksSensor(DataInputStream dis) throws IOException;

    public final byte[] packsPackage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            EndianUtil.writeByte(dos, pStart = Protocol.STX);
            EndianUtil.writeByte(dos, pProtocol = Protocol.VER);
            EndianUtil.writeByte(dos, pDeviceType);
            EndianUtil.writeByte(dos, pComm = COMM_SET);
            EndianUtil.writeByte(dos, pPackageId = generatePackageId());
            EndianUtil.writeByte(dos, pRespontionFlag = (byte) 0xFF);
            packsSensor(dos);
            byte[] localBytes = baos.toByteArray();
            EndianUtil.writeByte(dos, Protocol.getChksum(localBytes, 1, localBytes.length - 1));
            EndianUtil.writeByte(dos, pEnd = Protocol.EOI);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




    public final void unpacksPackage(byte[] cmd) throws IOException {
        //1.判断头部是否正确
        Protocol.checkHex(pStart = cmd[0], Protocol.STX);

        byte[] subMsg = Protocol.unescapeMsg(cmd, 1, cmd.length - 2);//截取数组并反转义

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(subMsg));
        //2.读协议版本
        pProtocol = EndianUtil.readByte(dis);
        //3.判断设备类型是否相符
        Protocol.checkHex(EndianUtil.readByte(dis), pDeviceType);
        Protocol.checkHex(pComm = EndianUtil.readByte(dis), COMM_UPLOAD);
        pPackageId = EndianUtil.readByte(dis);
        pRespontionFlag = EndianUtil.readByte(dis);
        //3.读数据字段
        unpacksSensor(dis);

        byte chksum = Protocol.getChksum(subMsg, 0, subMsg.length - 1);//计算校验码（最后一位是校验码，故不校验）
        //4.读校验码
        Protocol.checkHex(pVerify = subMsg[subMsg.length - 1], chksum);//判断校验码是否正确
        //5.读尾部
        Protocol.checkHex(pEnd = cmd[cmd.length - 1], Protocol.EOI);//判断尾部是否正确
    }

    //发送握手指令的上报解析
    public final void unpacksPackageWithShakeHand(byte[] cmd) throws IOException {
        //1.判断头部是否正确
        Protocol.checkHex(pStart = cmd[0], Protocol.STX);

        byte[] subMsg = Protocol.unescapeMsg(cmd, 1, cmd.length - 2);//截取数组并反转义

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(subMsg));
        //2.读协议版本
        pProtocol = EndianUtil.readByte(dis);
        //3.判断设备类型是否相符
        Protocol.checkHex(EndianUtil.readByte(dis), pDeviceType);
        Protocol.checkHex(pComm = EndianUtil.readByte(dis), COMM_READ);
        pPackageId = EndianUtil.readByte(dis);
        pRespontionFlag = EndianUtil.readByte(dis);
        //3.读数据字段
        unpacksSensor(dis);

        byte chksum = Protocol.getChksum(subMsg, 0, subMsg.length - 1);//计算校验码（最后一位是校验码，故不校验）
        //4.读校验码
        Protocol.checkHex(pVerify = subMsg[subMsg.length - 1], chksum);//判断校验码是否正确
        //5.读尾部
        Protocol.checkHex(pEnd = cmd[cmd.length - 1], Protocol.EOI);//判断尾部是否正确
    }

    public byte generatePackageId() {
        return (byte) ((staticPackageId++) & 0xFF);
    }

    @Override
    public String toString() {
        return "PackageMsg{" +
                "pStart=" + HexUtil.toHex16(pStart) +
                ", pProtocol=" + HexUtil.toHex16(pProtocol) +
                ", pDeviceType=" + HexUtil.toHex16(pDeviceType) +
                ", pComm=" + HexUtil.toHex16(pComm) +
                ", pPackageId=" + HexUtil.toHex16(pPackageId) +
                ", pRespontionFlag=" + HexUtil.toHex16(pRespontionFlag) +
                ", pVerify=" + HexUtil.toHex16(pVerify) +
                ", pEnd=" + HexUtil.toHex16(pEnd) +
                '}';
    }

    public void setpDeviceType(byte deviceType){
        this.pDeviceType = deviceType;

    }

    @Override
    public PackageMsg clone() {
        try {
            return (PackageMsg) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }
}
