package com.xiaomi.mone.tpc.common.param;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:52
 */
@Data
@ToString(callSuper = true)
public class ProjectQryParam extends BaseParam implements Serializable {


    private String name;
    private String type;
    private Long projectId;
    private boolean my = true;
    private List<String> names;
    private List<Long> projectIds;
    private String gitAddr;
    private boolean needParent = false;
    private boolean needTopSort = false;

    @Override
    public boolean argCheck() {
        return true;
    }
}
