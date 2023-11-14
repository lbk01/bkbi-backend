package com.bk.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bk.springbootinit.model.entity.Chart;
import com.bk.springbootinit.service.ChartService;
import com.bk.springbootinit.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




