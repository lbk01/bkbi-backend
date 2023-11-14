package com.bk.springbootinit.model.enums;

public enum GenChartStatusEnum
{
//wait,running,succeed,failed
    SUCCEED("succeed"),
    RUNNING("running"),
    WAITING("waiting"),
    FAILED("failed");

    private final String status;

    private GenChartStatusEnum(String status)
    {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
