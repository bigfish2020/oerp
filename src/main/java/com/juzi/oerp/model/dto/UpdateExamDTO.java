package com.juzi.oerp.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Juzi
 * @date 2020/7/15 22:48
 */
@Data
public class UpdateExamDTO {

    /**
     * 考试名称
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 报名开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 报名截止时间
     */
    private LocalDateTime endTime;

    /**
     * 考试时间
     */
    //private LocalDateTime examTime;
    private List<LocalDateTime> examTimeList;

    /**
     * 最多可报名多少人：-1 无限制
     */
    private Integer peopleNumber;

    /**
     * 考试费用
     */
    private BigDecimal price;

    /**
     * 考试地点
     */
    private String place;

}
