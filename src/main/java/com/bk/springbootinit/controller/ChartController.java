package com.bk.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bk.springbootinit.bizmq.BiMessageProducer;
import com.bk.springbootinit.manager.AiManager;
import com.bk.springbootinit.manager.RedisLimiterManager;
import com.bk.springbootinit.model.enums.GenChartStatusEnum;
import com.bk.springbootinit.model.vo.BiResponse;
import com.google.gson.Gson;
import com.bk.springbootinit.annotation.AuthCheck;
import com.bk.springbootinit.common.BaseResponse;
import com.bk.springbootinit.common.DeleteRequest;
import com.bk.springbootinit.common.ErrorCode;
import com.bk.springbootinit.common.ResultUtils;
import com.bk.springbootinit.constant.CommonConstant;
import com.bk.springbootinit.constant.UserConstant;
import com.bk.springbootinit.exception.BusinessException;
import com.bk.springbootinit.exception.ThrowUtils;
import com.bk.springbootinit.model.dto.chart.*;
import com.bk.springbootinit.model.entity.Chart;
import com.bk.springbootinit.model.entity.User;
import com.bk.springbootinit.service.ChartService;
import com.bk.springbootinit.service.UserService;
import com.bk.springbootinit.utils.ExcelUtils;
import com.bk.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author bk
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {


    @Resource
    AiManager aiManager;
    @Resource
    private ChartService chartService;

    @Resource
    RedisLimiterManager redisLimiterManager;

    @Resource
    private UserService userService;

    @Resource
    BiMessageProducer biMessageProducer;


    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String name = chartQueryRequest.getName();
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 这个是同步生成图表
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //对于用户需要用乎有一定自由度的话，一定要校验
//        万一他给我传一个100g文件，我的oos不立刻蓝调吗
//对于校验文件我们先校验大小
        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();
//todo
//        将来这些都优化成一些final常量
        final long limitFileSize = 1024 * 1024l;//1mb
        ThrowUtils.throwIf(limitFileSize < size, ErrorCode.PARAMS_ERROR, "文件超过 1M");
//        接下来是校验文件的后缀
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> validFIleSuffix = Arrays.asList("xlsx", "xls");
//       允许分析的数据
        ThrowUtils.throwIf(!validFIleSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        User loginUser = userService.getLoginUser(request);
//        接下来是调用接口的限制
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
//        先对数据进行一些简单的校验

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
//        等下存储数据库是要记录用户的id
//        然后使用一下限流的方法就好了

        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        对应已经设好对应的一些题词的aiModelId
        long biModelId =CommonConstant.BI_MODEL_ID;

        String res = ExcelUtils.excelToCsv(multipartFile);
//    然后就是给ai加一下提示测，假设他是什么东西,这里是bk提供的模型
//    他已经设好promet
        StringBuilder userInput = new StringBuilder();
//拼接目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }

        userInput.append("分析目标：").append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(res).append("\n");
//接下来就是调用上aimanger
        String result = aiManager.doChat(biModelId, userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai生成错误");
        }
//        第一部分是空的
//        我们主要拿的是第二部分的代码和第三部分的结论，顺便去掉一些空格之类的
        String genChart = splits[1];
        String genResult = splits[2];
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setStatus(GenChartStatusEnum.SUCCEED.getStatus());
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //对于用户需要用乎有一定自由度的话，一定要校验
//        万一他给我传一个100g文件，我的oos不立刻蓝调吗
//对于校验文件我们先校验大小
        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();
//todo
//        将来这些都优化成一些final常量
        final long limitFileSize = 1024 * 1024l;//1mb
        ThrowUtils.throwIf(limitFileSize < size, ErrorCode.PARAMS_ERROR, "文件超过 1M");
//        接下来是校验文件的后缀
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> validFIleSuffix = Arrays.asList("xlsx", "xls");
//       允许分析的数据
        ThrowUtils.throwIf(!validFIleSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        User loginUser = userService.getLoginUser(request);
//        接下来是调用接口的限制
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
//        先对数据进行一些简单的校验

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
//        等下存储数据库是要记录用户的id
//        然后使用一下限流的方法就好了

        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        对应已经设好对应的一些题词的aiModelId
        long biModelId = CommonConstant.BI_MODEL_ID;

        String res = ExcelUtils.excelToCsv(multipartFile);
//    然后就是给ai加一下提示测，假设他是什么东西,这里是yupi提供的模型
//    他已经设好promet
        StringBuilder userInput = new StringBuilder();
//拼接目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }

        userInput.append("分析目标：").append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(res).append("\n");
//接下来就是调用上aimanger
        /**
         * 注意这里要开始线程池化，我们先插入数据库
         */

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setStatus(GenChartStatusEnum.WAITING.getStatus());
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        /**
         * 在线程池中调用
         */
        CompletableFuture.runAsync(()->
        {

//            修改状态先
            Chart runningChart = new Chart();
            runningChart.setId(chart.getId());
            runningChart.setStatus(GenChartStatusEnum.RUNNING.getStatus());
            boolean update = chartService.updateById(runningChart);
            if (!update)
            {
                handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
                return;
            }
            //        第一部分是空的
//        我们主要拿的是第二部分的代码和第三部分的结论，顺便去掉一些空格之类的

            String result = aiManager.doChat(biModelId, userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
//            修改为成功
            updateChart.setStatus(GenChartStatusEnum.SUCCEED.getStatus());
            boolean updateRes = chartService.updateById(updateChart);
            if (!updateRes)
            {
                handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
            }
        },threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 处理更新时候的错误
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId,String execMessage)
    {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(GenChartStatusEnum.FAILED.getStatus());
        updateChartResult.setExecMessage(execMessage);
        boolean update = chartService.updateById(updateChartResult);
        if (!update)
        {
            log.error("更新图表内容失败"+chartId+","+execMessage);
        }

    }

    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //对于用户需要用乎有一定自由度的话，一定要校验
//        万一他给我传一个100g文件，我的oos不立刻蓝调吗
//对于校验文件我们先校验大小
        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();
//todo
//        将来这些都优化成一些final常量
        final long limitFileSize = 1024 * 1024l;//1mb
        ThrowUtils.throwIf(limitFileSize < size, ErrorCode.PARAMS_ERROR, "文件超过 1M");
//        接下来是校验文件的后缀
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> validFIleSuffix = Arrays.asList("xlsx", "xls");
//       允许分析的数据
        ThrowUtils.throwIf(!validFIleSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        User loginUser = userService.getLoginUser(request);
//        接下来是调用接口的限制
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
//        先对数据进行一些简单的校验

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
//        等下存储数据库是要记录用户的id
//        然后使用一下限流的方法就好了

        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        对应已经设好对应的一些题词的aiModelId
        long biModelId = CommonConstant.BI_MODEL_ID;

        String res = ExcelUtils.excelToCsv(multipartFile);
//    然后就是给ai加一下提示测，假设他是什么东西,这里是yupi提供的模型
//    他已经设好promet
        StringBuilder userInput = new StringBuilder();
//拼接目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }

        userInput.append("分析目标：").append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(res).append("\n");
//接下来就是调用上aimanger
        /**
         * 注意这里要开始发信息，我们先插入数据库
         */

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setStatus(GenChartStatusEnum.WAITING.getStatus());
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
//       这里发个消息就好了，我其实该把每份都抽象出来的
        biMessageProducer.sentMessage(chart.getId()+"");
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }



}
