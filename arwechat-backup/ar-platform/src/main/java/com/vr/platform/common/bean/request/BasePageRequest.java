package com.vr.platform.common.bean.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BasePageRequest implements Serializable {

    private static final long serialVersionUID = -8527907895568561467L;

    //条数
    @ApiModelProperty(value = "条数")
    private Integer pageSize = 10;

    //当前页
    @ApiModelProperty(value = "当前页")
    private Integer pageNum = 1;

    private Integer pageIndex;

    private Integer startRow;
}
