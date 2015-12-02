package com.feng.arch.sdk.mapper;

import com.feng.arch.sdk.po.FengUser;

public interface FengUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FengUser record);

    int insertSelective(FengUser record);

    FengUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FengUser record);

    int updateByPrimaryKey(FengUser record);
}