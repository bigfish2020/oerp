package com.juzi.oerp;

import com.juzi.oerp.mapper.ExamMapper;
import com.juzi.oerp.model.dto.UpdateExamDTO;
import com.juzi.oerp.model.po.ExamPO;
import com.juzi.oerp.service.AddTestInformationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootTest
public class test {
    @Autowired(required = false)
    private ExamMapper examMapper;

    @Autowired(required = false)
    private AddTestInformationService addTestInformationService;

    @Test
    void test1(){
        List<ExamPO> list = examMapper.selectList(null);
        for (ExamPO examPO:list){
            System.out.println(examPO.toString());
        }
    }

    @Test
    void test2(){
        String is = addTestInformationService.getWordInformation("C:\\Users\\FACK_YOU\\Desktop\\承诺书（学生）.docx");
        System.out.println(is);
        System.out.println(is.length());
    }

    @Test
    void test3(){
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println(date.format(formatter));
    }

    @Test
    void test4(){
        System.out.println(addTestInformationService.returnInformationFromMysql(1));
    }

    @Test
    void test5(){
        //测试插入一条数据
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        BigDecimal a =new BigDecimal(0.1);
        ExamPO examPO = new ExamPO(null,"sd","sd","sd","sd",date,date,a,date,date);
        //System.out.println(examMapper.insert(new ExamPO(3,"sd","sd","sd","sd",date,date,a,date,date)));
        System.out.println(examMapper.insert(examPO));

    }

    @Test
    void test6(){
        UpdateExamDTO updateExamDTO = new UpdateExamDTO();
        updateExamDTO.setTitle("证券从业资格考试");

        System.out.println(addTestInformationService.getExamID_By_title(updateExamDTO));
    }

    /*@Test
    void test7(){
        UpdateExamDTO updateExamDTO = new UpdateExamDTO();
        updateExamDTO.setExamTime(LocalDateTime.now());
        System.out.println(addTestInformationService.insert_exam_time(21,updateExamDTO));
    }*/

    @Test
    void test8(){
        UpdateExamDTO updateExamDTO = new UpdateExamDTO();
        updateExamDTO.setPlace("遵义");
        updateExamDTO.setPeopleNumber(200);
        System.out.println(addTestInformationService.insert_exam_place(2,updateExamDTO));
    }

   /* @Test
    void test9(){
        UpdateExamDTO updateExamDTO = new UpdateExamDTO();
        updateExamDTO.setExamTime(LocalDateTime.now());
        System.out.println(addTestInformationService.insert_exam_time(17,updateExamDTO));
    }*/



}
