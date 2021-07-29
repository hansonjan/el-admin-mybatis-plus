 package com.admin.base;

import java.sql.Timestamp;

 /**
  * @author hansonjan
  * @date 2021-07-05 15:24
  */
 public interface CreateInfo {
     /**
      * 获取 创建人
      * @return createBy
      */
     public String getCreateBy();


     /**
      * 设置 创建人
      * @param createBy 创建人
      */
     public void setCreateBy(String createBy);

     /**
      * 获取 创建时间
      * @return createTime
      */
     public Timestamp getCreateTime();


     /**
      * 设置 创建时间
      * @param createTime 创建时间
      */
     public void setCreateTime(Timestamp createTime);
 }
