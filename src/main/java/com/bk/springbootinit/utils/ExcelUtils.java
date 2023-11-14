package com.bk.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 读取的是excel文件，然后转为其他文件
 * @author bk
 * @data 2023/11/5 23:03
*/

@Slf4j
public class ExcelUtils
{
    /**
     * excel 转 csv
     *
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:test_excel.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        List<Map<Integer, String>> list ;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格读取错误");
            throw new RuntimeException(e);
        }
        System.out.println(list);
//        假设为空的话，直接返回一个null，下面是使用了工具类判断空
        if (CollUtil.isEmpty(list)) {
            return "";
        }
//        转化为csv
        StringBuilder builder = new StringBuilder();
//        读取标头，回去得看看人家的文档
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
//        然后过滤掉为null的数据，空的可能是有意义的。
//        其实我真得找个时间练习一下流的写法
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
//        加到builder里面
        builder.append(StringUtils.join(headerList,",")).append("\n");
//        最后是读取数据
//        第一个是一个表头
        for (int i = 1; i < list.size(); i++) {
//            为了保持顺序
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
//            过滤
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            builder.append(StringUtils.join(dataList,",")).append("\n");

        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.printf( ExcelUtils.excelToCsv(null));
    }
}
