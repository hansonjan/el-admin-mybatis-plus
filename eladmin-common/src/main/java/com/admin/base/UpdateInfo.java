package com.admin.base;

import java.sql.Timestamp;

/**
 * @author hansonjan
 * @date 2021-07-05 15:24
 */
public interface UpdateInfo {
    /**
     * 获取 修改人
     * 
     * @return updateBy
     */
    public String getUpdateBy();

    /**
     * 设置 修改人
     * 
     * @param updateBy
     *            修改人
     */
    public void setUpdateBy(String updateBy);

    /**
     * 获取 修改时间
     * 
     * @return updateTime
     */
    public Timestamp getUpdateTime();

    /**
     * 设置 修改时间
     * 
     * @param updateTime
     *            修改时间
     */
    public void setUpdateTime(Timestamp updateTime);

}
