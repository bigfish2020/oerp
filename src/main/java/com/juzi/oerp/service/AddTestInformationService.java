package com.juzi.oerp.service;

import com.juzi.oerp.model.dto.UpdateExamDTO;
import com.juzi.oerp.model.po.ExamPO;
import com.juzi.oerp.model.vo.response.ResponseVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;

public interface AddTestInformationService {

    //从硬盘读取word文件并返回内容
    public String getWordInformation(String word_url);


    //从数据库中获取word文档信息，交给前端
    public String returnInformationFromMysql(int id);


    //将ExamPO录入到exam数据库中
    public boolean insertTest(ExamPO examPO);


    //通过考试的title获取到考试的id
    public int getExamID_By_title(UpdateExamDTO updateExamDTO);


    //向exam_time表插入一条信息,并且返回最新一条数据的id
    public int insert_exam_time(int exam_id, UpdateExamDTO updateExamDTO,int count);


    //向exam_place表插入一条信息
    public boolean insert_exam_place(int exam_time_id,UpdateExamDTO updateExamDTO);


    //上传文件， 并且返回文件的url
    public String upload(MultipartFile file);


    //最终的接口，实现word文档的上传，图片的上传，向exam、exam_time、exam_place三个表中添加信息
    public ResponseVO<Object> createExam(UpdateExamDTO updateExamDTO, MultipartFile file1,
                                         MultipartFile file2);

    //将时间戳转换为时间
    public String stampToDate(String s);

    //将时间转换为时间戳
    public String dateToStamp(String s) throws ParseException;

    //将文件转换为HTML格式



}
