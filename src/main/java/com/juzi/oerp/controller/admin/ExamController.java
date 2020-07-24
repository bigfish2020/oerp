package com.juzi.oerp.controller.admin;

import com.juzi.oerp.model.dto.PageParamDTO;
import com.juzi.oerp.model.dto.UpdateExamDTO;
import com.juzi.oerp.model.po.ExamPO;
import com.juzi.oerp.model.vo.response.CreateResponseVO;
import com.juzi.oerp.model.vo.response.DeleteResponseVO;
import com.juzi.oerp.model.vo.response.ResponseVO;
import com.juzi.oerp.service.AddTestInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考试管理
 *
 * @author Juzi
 * @date 2020/7/15 22:45
 */
@RestController
@RequestMapping("/admin/exam")
public class ExamController {
    /**
     * 添加考试的service
     */
    @Autowired
    private AddTestInformationService addTestInformationService;


    /**
     * 获取考试_分页
     *
     * @param pageParamDTO 分页参数
     */
    @GetMapping
    public ResponseVO<ExamPO> getExamByPage(PageParamDTO pageParamDTO) {
        return null;
    }

    /**
     * 获取考试_根据考试 id
     *
     * @param examId 考试 id
     * @return 考试信息
     */
    @GetMapping("/{examId}")
    public ExamPO getExamById(@PathVariable Integer examId) {
        return null;
    }

    /**
     * 修改考试
     *
     * @param updateExamDTO 考试信息
     * @param examId        考试 id
     * @return 修改成功信息
     */
    @PutMapping("/{examId}")
    public ResponseVO<Object> updateExamById(@RequestBody UpdateExamDTO updateExamDTO, @PathVariable Integer examId) {
        return null;
    }



    /**
     * 创建考试
     *
     * @param updateExamDTO 考试信息
     * @return 创建成功信息
     */
    @PostMapping(value = "/createExam")
    @Transactional
    public ResponseVO<Object> createExam(@RequestPart UpdateExamDTO updateExamDTO,
                                         @RequestPart("file1") MultipartFile file1,
                                         @RequestPart("file2") MultipartFile file2) {
        return addTestInformationService.createExam(updateExamDTO,file1,file2);
    }










    /**
     * 删除考试
     *
     * @param examId 考试 id
     * @return 删除成功信息
     */
    @DeleteMapping("/{examId}")
    public ResponseVO<Object> deleteExamById(@PathVariable Integer examId) {
        return new DeleteResponseVO();
    }



    /*@RequestMapping("/ttttt")
    @ResponseBody
    public UpdateExamDTO ttttt(){
        UpdateExamDTO updateExamDTO = new UpdateExamDTO();
        updateExamDTO.setExamTime(LocalDateTime.now());
        updateExamDTO.setTitle("rrrr");
        updateExamDTO.setPeopleNumber(23);
        updateExamDTO.setPlace("sergser");
        updateExamDTO.setBeginTime(LocalDateTime.now());
        updateExamDTO.setDescription("fgsdfg");
        updateExamDTO.setEndTime(LocalDateTime.now());
        updateExamDTO.setPrice(new BigDecimal(0.1));
        return updateExamDTO;
    }*/

}
