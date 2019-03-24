package com.zxin.apigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

///**
// * 测试可以返回消息，不是项目内容
// */
//@Component
//public class AddResponseHeaderFilter extends ZuulFilter{
//    @Override
//    public String filterType() {
//        return POST_TYPE;
//    }
//
//    @Override
//    public int filterOrder() {
//        return SEND_RESPONSE_FILTER_ORDER - 1;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//
//    @Override
//    public Object run() {
//        RequestContext requestContext = RequestContext.getCurrentContext();
//        HttpServletResponse response = requestContext.getResponse();
//        response.setHeader("X-Foo", UUID.randomUUID().toString()); // 返回到Response
//        return null;
//    }
//}
