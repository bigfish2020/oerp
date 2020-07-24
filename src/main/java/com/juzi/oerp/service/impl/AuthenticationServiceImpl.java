package com.juzi.oerp.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juzi.oerp.common.exception.AuthenticationException;
import com.juzi.oerp.common.exception.CaptchaException;
import com.juzi.oerp.mapper.UserInfoMapper;
import com.juzi.oerp.mapper.UserMapper;
import com.juzi.oerp.model.dto.SMSCaptchaParamDTO;
import com.juzi.oerp.model.dto.UserLoginDTO;
import com.juzi.oerp.model.dto.UserRegistionDTO;
import com.juzi.oerp.model.po.UserInfoPO;
import com.juzi.oerp.model.po.UserPO;
import com.juzi.oerp.model.vo.CaptchaVO;
import com.juzi.oerp.model.vo.UserLoginVO;
import com.juzi.oerp.service.AuthenticationService;
import com.juzi.oerp.util.CacheUtils;
import com.juzi.oerp.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author Juzi
 * @date 2020/7/14 15:17
 */
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private IAcsClient iAcsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        UserPO userPO = new UserPO();
        userPO
                .setUsername(userLoginDTO.getUsername())
                .setPassword(SecureUtil.md5(userLoginDTO.getPassword()));

        UserPO user = userMapper.selectOne(new QueryWrapper<>(userPO));

        if (user == null || user.getStatus() == 1) {
            throw new AuthenticationException(40002);
        }

        String token = JWTUtils.createToken(user.getId());
        UserInfoPO userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>().eq(UserInfoPO::getUserId, user.getId()));
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO
                .setToken(token)
                .setUserInfo(userInfo);
        return userLoginVO;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public UserLoginVO registion(UserRegistionDTO userRegistionDTO) {
        // 检查手机号是否可用
        this.checkPhoneNumber(userRegistionDTO.getPhoneNumber());

        // 插入用户账号
        UserPO newUserPO = new UserPO();
        newUserPO
                .setPhoneNumber(userRegistionDTO.getPhoneNumber())
                .setUsername(userRegistionDTO.getUsername())
                .setPassword(SecureUtil.md5(userRegistionDTO.getPassword()));
        userMapper.insert(newUserPO);

        // 插入用户信息
        UserInfoPO newUserInfoPO = new UserInfoPO();
        newUserInfoPO
                .setUserId(newUserPO.getId());
        userInfoMapper.insert(newUserInfoPO);

        // 进行登录操作
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO
                .setUsername(userRegistionDTO.getUsername())
                .setPassword(userRegistionDTO.getPassword());
        return this.login(userLoginDTO);
    }

    @Override
    public CaptchaVO getImageCaptcha() {
        // 验证码对应 id
        String captchaId = IdUtil.objectId();
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 70);

        // 将验证码存入缓存
        Cache imageCaptchaCache = CacheUtils.getCache(cacheManager, "IMAGE_CAPTCHA_CACHE");
        imageCaptchaCache.put(captchaId, lineCaptcha.getCode());

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO
                .setCaptchaId(captchaId)
                .setCaptchaImageBase64(lineCaptcha.getImageBase64());
        return captchaVO;
    }

    @Override
    public void getSMSCaptcha(SMSCaptchaParamDTO smsCaptchaParamDTO) throws JsonProcessingException {
        // 检查手机号是否可用
        this.checkPhoneNumber(smsCaptchaParamDTO.getPhoneNumber());
        // 生成短信验证码
        String smsCaptcha = RandomUtil.randomNumbers(6);

        Cache imageCaptchaCache = CacheUtils.getCache(cacheManager, "IMAGE_CAPTCHA_CACHE");
        String imageCaptcha = imageCaptchaCache.get(smsCaptchaParamDTO.getCaptchaId(), String.class);
        if (StringUtils.isEmpty(imageCaptcha)) {
            throw new CaptchaException(40000);
        }

        Cache smsCaptchaCache = CacheUtils.getCache(cacheManager, "SMS_CAPTCHA_CACHE");
        smsCaptchaCache.put(smsCaptchaParamDTO.getCaptchaId(), smsCaptcha);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", smsCaptchaParamDTO.getPhoneNumber());
        request.putQueryParameter("SignName", "ABC商城");
        request.putQueryParameter("TemplateCode", "SMS_196641887");
        request.putQueryParameter("TemplateParam", objectMapper.writeValueAsString(MapUtil.builder("code", smsCaptcha).build()));
        try {
            CommonResponse response = iAcsClient.getCommonResponse(request);
            log.info(response.toString());
        } catch (ClientException e) {
            log.error("阿里云验证码服务异常", e);
        }
    }

    /**
     * 检测手机号是否已经注册
     *
     * @param phoneNumber 手机号
     * @return 手机号是否可用
     */
    private void checkPhoneNumber(String phoneNumber) {
        UserPO user = userMapper.selectOne(new LambdaQueryWrapper<UserPO>().eq(UserPO::getPhoneNumber, phoneNumber));
        // 手机号已被注册
        if (user != null) {
            throw new AuthenticationException(40001);
        }
    }
}
