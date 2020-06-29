package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * Created by chenqc on 2019/6/20.
 * 目前ble的框架不支持多参数发送，为适配INECK3S，只有出此下策，待后面有时间更换ble命令体框架
 */

public class NeckAllDataCmd extends BaseNeck3sCmd {
    private List<Byte> datas;

    public NeckAllDataCmd(List<Byte> bytes) {
        super(bytes.get(0));
        this.datas = bytes;
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        for (int i =0;i<datas.size();i++){
            if (i==0){

            }else {
                EndianUtil.writeByte(dos, datas.get(i));
            }
        }


    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }
}
