package com.bjpowernode.p2p.web;

import com.bjpowernode.p2p.constant.Constants;
import com.bjpowernode.p2p.model.LoanInfo;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import com.bjpowernode.p2p.service.loan.LoanInfoService;
import com.bjpowernode.p2p.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:IndexController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2020/1/28 12:20
 * @author:jiangjing
 */
@Controller
public class IndexController {
    @Autowired
    private LoanInfoService loanInfoService;
    @Autowired
    private UserService userService;
    @Autowired
    private BidInfoService bidInfoService;


    @RequestMapping(value = "/index")
    public ModelAndView index(){
        ModelAndView mav = new ModelAndView();


        /**
         * 模拟多线程，高并发的，请求。当线程的时间间隔足够短时(当第一个线程过来，并且还没有将查询的数据放入redis缓存的中，这期间的线程过来判断!ObjectUtils.allNotNull(historyAverageRate)都是为null，所以这旗期间的线程都是进入数据库里面查询)，这种现象为：缓存穿透现象。
         * 解决"缓存穿透"：使用两次判断和 synchronized锁 解决
         */

       /* ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i=0;i<10000;i++){
            executorService.submit(new Runnable() {
                @Override
                public void run() { //线程所要执行的任务
                    //查询平均年化利率
                    Double historyAverageRate  = loanInfoService.queryHistoryAverageRate();
                    mav.addObject(Constants.HISTORY_AVERAGE_RATE,historyAverageRate);
                }
            });
        }
         executorService.shutdown();
*/

        //查询平均年化利率
        Double historyAverageRate  = loanInfoService.queryHistoryAverageRate();
        mav.addObject(Constants.HISTORY_AVERAGE_RATE,historyAverageRate);

        //查询网站的用户注册的总人数
        Long allUserCount = userService.queryAllUserCount();
        mav.addObject(Constants.ALL_USER_COUNT,allUserCount);

        //获取平台累计投资金额
        Double allBidMoney = bidInfoService.queryAllBidMoney();
        mav.addObject(Constants.ALL_BID_MONEY, allBidMoney);

        /**
         * 所需要的参数：product_type pageSize currentPage=1  使用Map集合进行封装
         */
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("skipCount",0);

        //查询新手宝产品,产品类型：0 显示第一页，每页显示1条
        paramMap.put("pageSize",1);
        paramMap.put("product_type",Constants.PRODUCT_TYPE_X);
        List<LoanInfo> xLoanInfoList = loanInfoService.queryLoanInfoByProductType(paramMap);

        //查询优选产品，产品类型：1 每页显示4条
        paramMap.put("pageSize",4);
        paramMap.put("product_type",Constants.PRODUCT_TYPE_Y);
        List<LoanInfo> yLoanInfoList = loanInfoService.queryLoanInfoByProductType(paramMap);

        //查询散标产品，产品类型：2 每页显示8条
        paramMap.put("pageSize",8);
        paramMap.put("product_type",Constants.PRODUCT_TYPE_S);
        List<LoanInfo> sLoanInfoList = loanInfoService.queryLoanInfoByProductType(paramMap);


        mav.addObject("xLoanInfoList",xLoanInfoList);
        mav.addObject("yLoanInfoList",yLoanInfoList);
        mav.addObject("sLoanInfoList",sLoanInfoList);
        //跳转页面
        mav.setViewName("/index");
        return mav;


    }
}
