package com.xiaomi.mone.tpc.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 17:12
 */
@ToString
@Data
public class ProjectVoV2 implements Serializable {

    private ProjectVo projectVo;
    private NodeVo nodeVo;

}
