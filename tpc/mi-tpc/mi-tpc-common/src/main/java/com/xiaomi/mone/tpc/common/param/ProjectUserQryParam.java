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
public class ProjectUserQryParam extends BaseParam implements Serializable {

    private List<Long> projectIds;
    private Integer roleType;

    @Override
    public boolean argCheck() {
        if (projectIds == null || projectIds.isEmpty()) {
            return false;
        }
        return true;
    }
}
