package com.xiaomi.mone.tpc.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 17:12
 */
@ToString
@Data
public class ProjectOrgsVo implements Serializable {

    private Long id;
    private String name;
    private List<OrgInfoVo> orgVos;
}
