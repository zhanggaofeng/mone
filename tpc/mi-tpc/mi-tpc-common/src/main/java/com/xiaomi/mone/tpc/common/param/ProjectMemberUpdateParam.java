package com.xiaomi.mone.tpc.common.param;

import com.xiaomi.mone.tpc.common.enums.ProjectRoleType;
import com.xiaomi.mone.tpc.common.enums.UserTypeEnum;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:52
 */
@Data
@ToString(callSuper = true)
public class ProjectMemberUpdateParam extends BaseParam implements Serializable {

    private Long nodeId;
    private Long outId;
    private Integer roleType;
    private List<NullParam> userParams;

    @Override
    public boolean argCheck() {
        if (nodeId == null && outId == null) {
            return false;
        }
        if (roleType == null || ProjectRoleType.getEnum(roleType) == null) {
            return false;
        }
        if (userParams != null && !userParams.isEmpty()) {
            for (NullParam param : userParams) {
                if ( UserTypeEnum.getEnum(param.getUserType()) == null || StringUtils.isEmpty(param.getAccount())) {
                    return false;
                }
            }
        }
        return true;
    }
}
