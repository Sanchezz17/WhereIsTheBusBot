package com.github.telegram.mvc.handler;

import com.github.telegram.mvc.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.core.MethodParameter;

public class BotBaseRequestMethodProcessor
        implements BotHandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return BaseRequest.class.isAssignableFrom(paramType);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception {
        Class<?> paramType = returnType.getParameterType();
        if (BaseRequest.class.isAssignableFrom(paramType)) {
            if (!paramType.isInstance(returnValue)) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest);
            }
            telegramRequest.setBaseRequest((BaseRequest) returnValue);
        }
    }
}