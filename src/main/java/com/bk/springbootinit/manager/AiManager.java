package com.bk.springbootinit.manager;

import com.bk.springbootinit.common.ErrorCode;
import com.bk.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 对接ai的方法
 * @author bk
 * @data 2023/11/6 23:19
*/
@Service
public class AiManager
{

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI 对话
     *
     * @param modelId
     * @param message
     * @return
     */
    public String doChat(long modelId, String message) {
//        基本上就是使用一个http接口
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response==null)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"调用ai接口失败");
        }
        return response.getData().getContent();
    }



}
