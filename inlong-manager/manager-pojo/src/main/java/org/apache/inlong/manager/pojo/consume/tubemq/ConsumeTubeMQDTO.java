/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.pojo.consume.tubemq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.inlong.manager.common.enums.ErrorCodeEnum;
import org.apache.inlong.manager.common.exceptions.BusinessException;

import javax.validation.constraints.NotNull;

/**
 * Inlong group info of TubeMQ
 */
@Data
@Builder
@AllArgsConstructor
@ApiModel("Inlong group info of TubeMQ")
public class ConsumeTubeMQDTO {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // no fields

    /**
     * Get the dto instance from the request
     */
    public static ConsumeTubeMQDTO getFromRequest(ConsumeTubeMQRequest request) {
        return ConsumeTubeMQDTO.builder().build();
    }

    /**
     * Get the dto instance from the JSON string.
     */
    public static ConsumeTubeMQDTO getFromJson(@NotNull String extParams) {
        try {
            return OBJECT_MAPPER.readValue(extParams, ConsumeTubeMQDTO.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.CONSUMER_INFO_INCORRECT.getMessage() + ": " + e.getMessage());
        }
    }

}
