package com.bk.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author bk
 *
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 名称
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}