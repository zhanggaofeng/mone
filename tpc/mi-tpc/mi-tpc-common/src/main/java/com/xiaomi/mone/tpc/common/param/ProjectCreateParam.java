package com.xiaomi.mone.tpc.common.param;

import com.xiaomi.mone.application.api.bo.ProjectVo;
import com.xiaomi.mone.tpc.common.enums.OutIdTypeEnum;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:52
 */
@Data
@ToString(callSuper = true)
public class ProjectCreateParam extends BaseParam implements Serializable {

    private Long parentNodeId;
    private Long parentOutId;
    private Integer parentOutIdType;
    private String nodeName;
    private String desc;
    private OrgInfoParam orgParam;
    private ProjectVo projectVo;


    @Override
    public boolean argCheck() {
        if (parentNodeId == null && (parentOutId == null || parentOutId.equals(0L) || parentOutIdType == null)) {
            return false;
        }
        if (parentOutIdType != null && OutIdTypeEnum.getEnum(parentOutIdType) == null) {
            return false;
        }
        if (projectVo == null) {
            return false;
        }
        if (StringUtils.isBlank(projectVo.getName())) {
            return false;
        }
        if (StringUtils.isBlank(projectVo.getGitGroup()) || StringUtils.isBlank(projectVo.getGitName()) || StringUtils.isBlank(projectVo.getDomain())) {
            return false;
        }
        if (orgParam != null && !orgParam.argCheck()) {
            return false;
        }
        if (StringUtils.isBlank(projectVo.getGitAddress())) {
            StringBuilder addr = new StringBuilder();
            addr.append("https://").append(projectVo.getDomain())
                    .append("/").append(projectVo.getGitGroup())
                    .append("/").append(projectVo.getGitName());
            projectVo.setGitAddress(addr.toString());
        }
        return true;
    }
}
