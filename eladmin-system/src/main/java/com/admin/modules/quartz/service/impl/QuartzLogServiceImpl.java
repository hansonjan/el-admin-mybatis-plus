/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.admin.modules.quartz.service.impl;

import com.admin.modules.quartz.domain.QuartzLog;
import com.admin.modules.quartz.repository.QuartzLogMapper;
import com.admin.modules.quartz.service.QuartzLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Zheng Jie
 * @date 2019-01-07
 */
@RequiredArgsConstructor
@Service(value = "quartzLogService")
public class QuartzLogServiceImpl extends ServiceImpl<QuartzLogMapper, QuartzLog> implements QuartzLogService {

}
