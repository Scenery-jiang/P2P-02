package com.bjpowernode.p2p.web;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.http.HttpClientUtils;
import com.bjpowernode.p2p.constant.Constants;
import com.bjpowernode.p2p.model.*;
import com.bjpowernode.p2p.model.vo.PaginationVO;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import com.bjpowernode.p2p.service.loan.FinanceAccountService;
import com.bjpowernode.p2p.service.loan.IncomeRecordService;
import com.bjpowernode.p2p.service.loan.RechargeRecordService;
import com.bjpowernode.p2p.service.user.UserService;
import com.bjpowernode.p2p.util.Result;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName:UserController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2020/1/31 9:18
 * @author:jiangjing
 */
//@RequestMapping(value = "/loan")
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FinanceAccountService financeAccountService;

    @Autowired
    private BidInfoService bidInfoService;

    @Autowired
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private IncomeRecordService incomeRecordService;


    /**
     * 验证手机号是否存在
     *
     * @param phone
     * @return
     */
    @RequestMapping(value = "/loan/checkPhone", method = RequestMethod.POST)
    //@PostMapping(value = "/checkPhone")
    public @ResponseBody
    Result checkPhone(@RequestParam(value = "phone", required = true) String phone) {
        User user = userService.queryUserByPhone(phone);
        if (ObjectUtils.allNotNull(user)) {
            //不为空，该手机号已经存在
            return Result.error("该手机号已经注册，请更换手机号");
        }
        return Result.success();
    }


    /**
     * 验证图形验证码是否正确
     */
    @GetMapping(value = "/loan/checkCaptcha")
    @ResponseBody
    public Result checkCaptcha(@RequestParam(value = "captcha", required = true) String captcha, HttpServletRequest request) {
        String sessionCaptcha = (String) request.getSession().getAttribute(Constants.CAPTCHA);
        if (StringUtils.equalsIgnoreCase(sessionCaptcha, captcha)) {
            return Result.success();
        }
        return Result.error("图形验证码错误");
    }

    /**
     * 实现注册功能，注册成功，返回User对象，然后添加到session域中，实现登录
     * 在注册完成时要生成一个financeAccount(账户)
     * 在controller层try  catch 异常，在service层new 对应的异常
     *
     * @param phone
     * @param loginPassword
     * @return
     */
    @RequestMapping(value = "/loan/register")
    @ResponseBody
    public Result register(@RequestParam(value = "phone", required = true) String phone, @RequestParam(value = "loginPassword", required = true) String loginPassword, HttpServletRequest request) {

        try {
            User user = userService.register(phone, loginPassword);
            request.getSession().setAttribute("user", user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败");
        }
        return Result.success();
    }

    @RequestMapping(value = "/loan/verifyIdentify")
    @ResponseBody
    public Result verifyIdentify(@RequestParam(value = "realName", required = true) String realName, @RequestParam(value = "idCard", required = true) String idCard, HttpServletRequest request) {

        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("appkey", "33586edf61409c13640655355a0d4c20");
            map.put("name", realName);
            map.put("certNo", idCard);
            //调用接口，返回的是json格式的字符串，需要该字符串进行解析（使用alibaba的fastJson进行解析）
            String jsonString = HttpClientUtils.doPost("https://way.jd.com/YOUYU365/jd_credit_two", map);
            /**
             * 一般工作的接口和实现类会有不同的负责，所以需要联合测试，负责写实现类的需要想写接口的人预约好时间并要有以邮件的方式进行存档，并备份该自己的领导，避免背黑锅。
             * 测试一般会有此时限制，所以需要使用模拟数据结果进行测试，先排除一些重大错误
             */
           /* String jsonString ="{\n" +
                    "    \"code\": \"10000\",\n" +
                    "    \"charge\": false,\n" +
                    "    \"remain\": 1305,\n" +
                    "    \"msg\": \"查询成功\",\n" +
                    "    \"result\": {\n" +
                    "        \"code\": \"000000\",\n" +
                    "        \"serialNo\": \"201707110956216762709752427036\",\n" +
                    "        \"success\": \"true\",\n" +
                    "        \"message\": \"一致\",\n" +
                    "        \"comfirm\": \"jd_credit_two\"\n" +
                    "    }\n" +
                    "}";*/
            //对json字符串进行解析，返回的是json对象
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String code = jsonObject.getString("code");
            if (!StringUtils.equals(code, "10000")) {
                return Result.error("通信异常");
            }
            /**
             *  获取json对象中的result的json对象，使用albaba的fastJson，进行解析即可
             *  这个result里面是调用远程方法时封装的 业务返回参数
             *  执行到下面一定有查询结果，true or false
             */
            JSONObject result = jsonObject.getJSONObject("result");
            //result里面有一个success参数
            Boolean success = result.getBoolean("success");
            if (!success) {
                return Result.error("姓名和身份号码不匹配");
            } else {
                User sessionUser = (User) request.getSession().getAttribute("user");
                //进入数据可更新user信息
                User user = new User();
                user.setIdCard(idCard);
                user.setName(realName);
                user.setId(sessionUser.getId());
                //只更新这两个数据，考虑到多字段的更新会降低效率，所以要更新什么就写入什么
                int modifyUserCount = userService.modifyUserInfo(user);
                if (modifyUserCount != 1) {
                    throw new Exception("实名认证更新失败");
                }
                //将数据更新数据库
                sessionUser.setName(realName);
                sessionUser.setIdCard(idCard);
                request.getSession().setAttribute("user", sessionUser);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.success();

    }

    /**
     * 执行登录操作，先查询该用户是否存在然会执行登录操作如果密码和账号验证成功，更新最近登录时间。返回的user放入session域空间。
     * 考虑到要返回的是上一次的登录时间，所以要返回更新之前的user
     *
     * @param loginPassword
     * @param phone
     * @param request
     * @return
     */
    @RequestMapping(value = "/loan/login")
    @ResponseBody
    public Result login(@RequestParam(value = "loginPassword", required = true) String loginPassword,
                        @RequestParam(value = "phone", required = true) String phone, @RequestParam(value = "captcha", required = true) String captcha, HttpServletRequest request) {
        try {
            String sessionCaptcha = (String) request.getSession().getAttribute(Constants.CAPTCHA);
            if (!StringUtils.equalsAnyIgnoreCase(sessionCaptcha, captcha)) {
                return Result.error("验证码错误");
            }

            User user = userService.queryUserByPhone(phone);
            if (!ObjectUtils.allNotNull(user)) {
                return Result.error("该账号不存在，请先注册");
            }
            User sessionUser = userService.login(loginPassword, phone);
            if (!ObjectUtils.allNotNull(sessionUser)) {
                return Result.error("账号或密码不正确");
            }
            //将更新上一次
            request.getSession().setAttribute("user", sessionUser);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统异常");
        }

        return Result.success();
    }


    @RequestMapping(value = "/loan/logout")
    public String logout(HttpServletRequest request) {
        //是session失效即可
        request.getSession().invalidate();
        //跳转到登录页面或者首页
        return "redirect:/index";

    }

    @RequestMapping(value = "/loan/myCenter")
    @ResponseBody
    public ModelAndView myCenter(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        //获取user信息
        User user = (User) request.getSession().getAttribute(Constants.SESSION_USER);
        mav.addObject(Constants.SESSION_USER, user);

        //获取账户信息
        FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUserId(user.getId());
        mav.addObject("financeAccount", financeAccount);

        /**
         * 一下三个默认都是只显示5条记录，然后会有查询更多的分页查询，所以以分页查询的方式进行查询
         *  默认：第一页  currentPage = 1 skipCount=0 pageSize=5  使用map集合封装参数
         */
        Integer skipCount = 0;
        Integer pageSize = 5;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("skipCount", skipCount);
        paramMap.put("pageSize", pageSize);
        paramMap.put("userId", user.getId());
        /**
         *  获取最近投资
         *
         *  bid表：只有 投资时间 投资金额 loanId 所以需要和loan表联合查咨询
         *  返回值：  1.一条 投资记录 对应 一个loan 所以可以在bid表提添加一个loan字段
         *           2.使用vo（value object）
         *           3.list<map>
         */
        List<rechargeRecordList> bidInfoList = bidInfoService.queryBidInfoListByUserId(paramMap);

        //获取最近充值   所需的字段recharge表里都有，所以使用单表查询即可
        List<RechargeRecord> rechargeRecordList = rechargeRecordService.queryRechargeRecordListByUserId(paramMap);
        //获取最近收益  需要项目名称，所以需要和loan表进行联查
        List<IncomeRecord> incomeRecordList = incomeRecordService.queryIncomeRecordListByUserId(paramMap);
        mav.addObject("bidInfoList", bidInfoList);
        mav.addObject("rechargeRecordList", rechargeRecordList);
        mav.addObject("incomeRecordList", incomeRecordList);
        mav.setViewName("myCenter");

        return mav;
    }

    @RequestMapping(value = "/loan/myInvest")
    @ResponseBody
    public ModelAndView myInvest(@RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        User user = (User) request.getSession().getAttribute("user");
        Integer pageSize = 6;
        Integer skipCount = (currentPage - 1) * pageSize;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageSize", pageSize);
        paramMap.put("userId", user.getId());
        paramMap.put("skipCount", skipCount);
        PaginationVO<rechargeRecordList> paginationVO = bidInfoService.queryBidInfoByPage(paramMap);
        Long total = paginationVO.getTotal();
        Long totalPage = total / pageSize;
        if (total % pageSize != 0) {
            totalPage++;
        }
        mav.addObject("totalPage", totalPage);
        mav.addObject("paginationVO", paginationVO);
        mav.addObject("currentPage", currentPage);
        mav.setViewName("myInvest");
        return mav;

    }

    @RequestMapping(value = "loan/myRecharge")
    @ResponseBody
    public ModelAndView myRecharge(@RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        User user = (User) request.getSession().getAttribute("user");
        Integer pageSize = 6;
        Integer skipCount = (currentPage - 1) * pageSize;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageSize", pageSize);
        paramMap.put("userId", user.getId());
        paramMap.put("skipCount", skipCount);
        PaginationVO<RechargeRecord> paginationVO = rechargeRecordService.queryRechargeByPage(paramMap);
        Long total = paginationVO.getTotal();
        Long totalPage = total / pageSize;
        if (total % pageSize != 0) {
            totalPage++;
        }
        mav.addObject("totalPage", totalPage);
        mav.addObject("paginationVO", paginationVO);
        mav.addObject("currentPage", currentPage);
        mav.setViewName("myRecharge");
        return mav;
    }


    @RequestMapping(value = "loan/myIncome")
    @ResponseBody
    public ModelAndView myIncome(@RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        User user = (User) request.getSession().getAttribute("user");
        Integer pageSize = 6;
        Integer skipCount = (currentPage - 1) * pageSize;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageSize", pageSize);
        paramMap.put("userId", user.getId());
        paramMap.put("skipCount", skipCount);
        PaginationVO<IncomeRecord> paginationVO = incomeRecordService.queryIncomerRecordByPage(paramMap);
        Long total = paginationVO.getTotal();
        Long totalPage = total / pageSize;
        if (total % pageSize != 0) {
            totalPage++;
        }
        mav.addObject("totalPage", totalPage);
        mav.addObject("paginationVO", paginationVO);
        mav.addObject("currentPage", currentPage);
        mav.setViewName("myIncome");
        return mav;
    }
}
