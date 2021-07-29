package com.admin.convert;

import cn.hutool.core.util.ObjectUtil;
import com.admin.utils.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * @author hansonjan
 * @date 2021-07-06 10:22
 */
public class PageResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isAssignedFrom = IPage.class.isAssignableFrom(parameter.getParameterType());
        boolean isPageFrom = Page.class.isAssignableFrom(parameter.getParameterType());
        boolean isPage = Page.class.equals(parameter.getParameterType());
        boolean result = isAssignedFrom || isPage || isPageFrom;
        return result;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return getParameterPage(webRequest);
    }

    private static final int DEFAULT_MAX_PAGE_SIZE = 2000;
    private static int maxPageSize = DEFAULT_MAX_PAGE_SIZE;
    public static Page getParameterPage(NativeWebRequest webRequest){
        String currentStr = webRequest.getParameter("current");
        if(StringUtils.isBlank(currentStr)){
            currentStr = webRequest.getParameter("page");
        }
        String pageSizeStr = webRequest.getParameter("pageSize");
        if(StringUtils.isBlank(pageSizeStr)){
            pageSizeStr = webRequest.getParameter("size");
        }
        int number1 = 1;
        int current = 1;
        int pageSize = 30;
        if(NumberUtils.isCreatable(currentStr)){
            BigDecimal dec = new BigDecimal(currentStr);
            current = dec.intValue();
        }
        if(NumberUtils.isCreatable(pageSizeStr)){
            BigDecimal dec = new BigDecimal(pageSizeStr);
            pageSize = dec.intValue();
        }
        if(current < number1){
            current = number1;
        }
        if(pageSize > maxPageSize){
            pageSize = maxPageSize;
        }


        Page page = new Page(current,pageSize);

        //排序字段处理
        //第一种格式：[{column:options.idField,asc:false},{column:"dictCode",asc:true}]
        String[] columns = webRequest.getParameterValues("orders[column]");
        String[] ascs = webRequest.getParameterValues("orders[asc]");
        String underLine = "_";
        if(ObjectUtil.isNotEmpty(columns) && ObjectUtil.isNotEmpty(ascs)){
            if(columns.length == ascs.length){
                List<OrderItem> orders = new ArrayList<>();
                for (int i = 0; i < columns.length; i++) {
                    OrderItem item = new OrderItem();
                    String column = columns[i].trim();
                    String asc = ascs[i].trim();

                    //将dictId转换成dict_Id。前端可以传dictId也可以传dict_id也可以传dict_Id
                    if(!column.contains(underLine)){
                        //找到大写字母位置插入下划线
                        StringBuffer sb = new StringBuffer(column);
                        for (int index = sb.length()-1; index >=1 ; index--) {
                            if(Character.isUpperCase (sb.charAt(index))){
                                sb.insert(index,underLine);
                            }
                        }
                        column = sb.toString();
                    }

                    item.setColumn(column);
                    if("false".equalsIgnoreCase(asc)){
                        item.setAsc(false);
                    }else {
                        item.setAsc(true);
                    }
                    orders.add(item);
                }
                if(ObjectUtil.isNotEmpty(page.getOrders())){
                    page.getOrders().addAll(orders);
                }else{
                    page.setOrders(orders);
                }
            }
        }

        return page;
    }
}
