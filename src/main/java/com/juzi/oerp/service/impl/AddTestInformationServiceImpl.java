package com.juzi.oerp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.juzi.oerp.mapper.ExamMapper;
import com.juzi.oerp.mapper.ExamPlaceMapper;
import com.juzi.oerp.mapper.ExamTimeMapper;
import com.juzi.oerp.model.dto.UpdateExamDTO;
import com.juzi.oerp.model.po.ExamPO;
import com.juzi.oerp.model.po.ExamPlacePO;
import com.juzi.oerp.model.po.ExamTimePO;
import com.juzi.oerp.model.vo.response.ResponseVO;
import com.juzi.oerp.service.AddTestInformationService;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;
@Service
public class AddTestInformationServiceImpl implements AddTestInformationService {
    /**
     * 装配一个关于考试exam的单表查询接口
     */
    @Autowired(required = false)
    private ExamMapper examMapper;
    /**
     * 装配一个关于exam_time的单表查询接口
     */
    @Autowired(required = false)
    private ExamTimeMapper examTimeMapper;
    /**
     * 装配一个关于exam_place的单表查询接口
     */
    @Autowired(required = false)
    private ExamPlaceMapper examPlaceMapper;



    /**
     * 上传文件，可以传递各种文件， 并且返回文件的url
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {
        String url = null;
        // 如果文件不为空，写入上传路径，进行文件上传
        if (!file.isEmpty()){
            /**
             * 构建上传之后的地址
             */
            String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
            path = path.replaceFirst("target/classes/","asserts");
            path = path.replaceFirst("/","");
            //通过时间戳来设置新名字，以此来保证文件名的唯一性
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fileName = null;
            try {
                fileName = dateToStamp(formatter.format(LocalDateTime.now())) + file.getOriginalFilename();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            url = path + "/" + fileName;


            /**
             * 判断路径是否存在，不存在则新创建一个
             */
            File filepath = new File(path, fileName);
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }

            /**
             * 将文件上传到指定的位置
             */
            try {
                file.transferTo(new File(path + File.separator + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return url;
        }else {
            return "文件为空，不能上传";
        }
    }


    /**
     * 读取word文件,返回字符串
     * @param word_url
     * @return
     */
    @Override
    public String getWordInformation(String word_url) {
        //获取输入流
        FileInputStream input = null;
        try {
            input = new FileInputStream(word_url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //创建一个 XWPFDocument 对象 传入 输入流
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建一个 XWPFWordExtractor 对象 传入 文档对象
        XWPFWordExtractor we = new XWPFWordExtractor(doc);
        /**
         * 获取文档中的字符
         */
        String text = we.getText();
        //释放资源
        try {
            we.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }


    /**从数据库中获取word文档信息，交给前端
     * @param id
     * @return
     */
    @Override
    public String returnInformationFromMysql(int id) {
        ExamPO examPO = examMapper.selectById(id);
        return examPO.getDescription();
    }


    /**向exam数据库中添加一条数据
     * @param examPO
     * @return
     */
    @Override
    @Transactional
    public boolean insertTest(ExamPO examPO) {
        //插入数据
        int a = examMapper.insert(examPO);
        //判断数据录入是不是成功的
        if (a==1){
            return true;
        }else {
            return false;
        }
    }


    /**
     * 通过考试的title获取到考试的id
     * @param updateExamDTO
     * @return
     */
    @Override
    public int getExamID_By_title(UpdateExamDTO updateExamDTO) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("title",updateExamDTO.getTitle());
       return examMapper.selectOne(queryWrapper).getId();
    }



    /**
     * 向exam_time表插入一条信息,并且返回最新一条数据的id
     * @param exam_id
     * @param updateExamDTO
     * @return
     */
    @Override
    @Transactional
    public int insert_exam_time(int exam_id, UpdateExamDTO updateExamDTO,int count){
        /**
         * 第一步先插入数据
         */
        ExamTimePO examTimePO =new ExamTimePO();
        examTimePO.setId(null);
        examTimePO.setExamId(exam_id);
        examTimePO.setExamTime(updateExamDTO.getExamTimeList().get(count));
        examTimePO.setUpdateTime(LocalDateTime.now());
        examTimePO.setCreateTime(LocalDateTime.now());
        examTimeMapper.insert(examTimePO);
        /**
         * 第二步再获取根据exam的id号查询出所有的时间id，在查询出来的id集合中取最大的id号
         * 这就是最新插入的exam_time的id
         */
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("exam_id",exam_id);
        List<ExamTimePO> timePOList = examTimeMapper.selectList(queryWrapper);
        List<Integer> idlist = new ArrayList<Integer>();
        for (ExamTimePO examTimepo:timePOList){
            idlist.add(examTimepo.getId());
        }
        //使用Collections包装类来获取list集合的最大值
        return Collections.max(idlist);
    }


    /**
     * 最终的接口，实现word文档的上传，图片的上传，向exam、exam_time、exam_place三个表中添加信息
     * @param updateExamDTO
     * @param file1
     * @param file2
     * @return
     */
    @Transactional
    public ResponseVO<Object> createExam(UpdateExamDTO updateExamDTO, MultipartFile file1,
                                         MultipartFile file2){
        /**
         * 获取word的url
         */
        String word_url = upload(file1);
        /**
         * 获取图片的url
         */
        String image_url = upload(file2);
        /**
         * 上传错误的判断
         */
        if (word_url!=null&&word_url!=""&&image_url!=null&&image_url!=""){
            /**
             * 读取文档
             */
            String word = getWordInformation(word_url);
            /**
             * 读取错误的判断
             */
            if (word!=null&&word!=""){
                //向exam表添加一条数据
                ExamPO examPO = new ExamPO(null,updateExamDTO.getTitle(),
                        updateExamDTO.getDescription(),word,word_url,
                        updateExamDTO.getBeginTime(),updateExamDTO.getEndTime(),
                        updateExamDTO.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP),LocalDateTime.now(),LocalDateTime.now());
                /**
                 * 判断向exam表插入数据是否成功
                 */
                boolean is1 = insertTest(examPO);
                if (is1){
                    int count = 0;//既用来表示第几次循环，也表示第几次List<LocalDateTime> examTimeList;的第几条数据
                    for (count = 0 ; count<updateExamDTO.getExamTimeList().size() ; count++){
                        //第一重循环，插入exam_time表
                        int exam_time_id = insert_exam_time(getExamID_By_title(updateExamDTO),updateExamDTO,count);
                        //判断这一次的exam_Time插入数据是不是成功的，如果不是，就要跳出循环
                        if (exam_time_id!=0){
                            insert_exam_place(exam_time_id,updateExamDTO);
                        }else {
                            break;
                        }
                    }
                    if (count == updateExamDTO.getExamTimeList().size()){
                        ResponseVO<Object> responseVO = new ResponseVO<>(examPO);
                        return responseVO;
                    }else {
                        //exam表单插入数据失败
                        ResponseVO<Object> responseVO = new ResponseVO<>(50004,"在第" + count + "次循环插入数据的时候失败");
                        return responseVO;
                    }

                }else {
                    //exam表单插入数据失败
                    ResponseVO<Object> responseVO = new ResponseVO<>(50003,"exam表单插入数据失败");
                    return responseVO;
                }

            }else {
                //报错的情况下，设置错误代码，以及错误消息
                ResponseVO<Object> responseVO = new ResponseVO<>(50002,"文件读取失败");
                return responseVO;
            }

        }else {
            //报错的情况下，设置错误代码，以及错误消息
            ResponseVO<Object> responseVO = new ResponseVO<>(50001,"文件上传失败");
            return responseVO;
        }
    };



    /**
     * 向exam_place表插入一条信息
     * @param exam_time_id
     * @param updateExamDTO
     * @return
     */
    public boolean insert_exam_place(int exam_time_id,UpdateExamDTO updateExamDTO){
        ExamPlacePO examPlacePO = new ExamPlacePO();
        examPlacePO.setId(null);
        examPlacePO.setExamTimeId(exam_time_id);
        examPlacePO.setExamPlace(updateExamDTO.getPlace());
        examPlacePO.setCreateTime(LocalDateTime.now());
        examPlacePO.setUpdateTime(LocalDateTime.now());
        examPlacePO.setPeopleNumber(updateExamDTO.getPeopleNumber());
        if (examPlaceMapper.insert(examPlacePO)==1){
            return true;
        }else {
            return false;
        }
    }






    /**
     * 将时间戳转换为时间
     * @param s
     * @return
     */
    public String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }


    /**
     * 将时间转换为时间戳
     * @param s
     * @return
     * @throws ParseException
     */
    public String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }






}
