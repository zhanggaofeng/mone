package com.xiaomi.mone.tpc.common.param;

import com.xiaomi.mone.tpc.common.enums.ProjectRoleType;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:52
 */
@Data
@ToString(callSuper = true)
public class ProjectMemberQryParam extends BaseParam implements Serializable {

    private Long nodeId;
    private Long outId;
    private Integer roleType;

    @Override
    public boolean argCheck() {
        if (nodeId == null && outId == null) {
            return false;
        }
        if (roleType != null && ProjectRoleType.getEnum(roleType) == null) {
            return false;
        }
        return true;
    }
}
