package com.bjpowernode.p2p.web;

import com.bjpowernode.p2p.model.rechargeRecordList;
import com.bjpowernode.p2p.model.LoanInfo;
import com.bjpowernode.p2p.model.vo.PaginationVO;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import com.bjpowernode.p2p.service.loan.LoanInfoService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName:LoanController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2020/1/30 0:08
 * @author:jiangjing
 */
@RequestMapping(value = "/loan")
@Controller
public class LoanController {
    @Autowired
    private LoanInfoService loanInfoService;
    @Autowired
    private BidInfoService bidInfoService;

    /**
     * 实现分页查询：需要的参数：pageSize=9(已经规定好了) currentPage(pageNo)(必须的，默认值为1就可) 起始下标skipCount=(currentPage-1)*pageSize 产品的类型：pageType（不是必须的，可以根据类型查询，也可以查全部）
     * <p>
     * <p>
     * 返回值：可以定义一个VO对象作为返回值类型。List</T> ,total
     * 还需要以一个总页数pageTotal，根据total和 pageSize计算
     *
     * @return
     */
    @RequestMapping(value = "/loan")
    public ModelAndView loan(@RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage, @RequestParam(value = "ptype", required = false) String product_type) {
        ModelAndView mav = new ModelAndView();
        //使用Map集合封装参数
        HashMap<String, Object> paramMap = new HashMap<>();
        Integer pageSize = 9;
        Integer skipCount = (currentPage - 1) * pageSize;
        paramMap.put("pageSize", pageSize);
        paramMap.put("skipCount", skipCount);
        if (ObjectUtils.allNotNull(product_type)) {
            //如果有productType的参数就添加到map集合中
            paramMap.put("product_type", product_type);
        }

        PaginationVO<LoanInfo> paginationVO = loanInfoService.queryLoanInfoByPage(paramMap);

        Long totalPage = paginationVO.getTotal() / pageSize;
        if (paginationVO.getTotal() % pageSize != 0) {
            totalPage = totalPage + 1;
        }

        mav.addObject("paginationVO",paginationVO);
        mav.addObject("totalPage",totalPage);
        mav.addObject("currentPage",currentPage);
        mav.addObject("product_type",product_type);
        mav.setViewName("/loan");
        return mav;
        //TODO
        //投资排行榜

    }


    @RequestMapping(value = "/loanInfo")
    public ModelAndView loanIf(@RequestParam(value = "id",required = true)Integer loanId){
        ModelAndView mav = new ModelAndView();
        /**
         * 根据产品的id，查询投资信息和对应的用户信息。一条投资信息只属于一个用户，所以可以在bid的类添加一个user属性，作为返回值类型
         * 展示前十条信息，类似于分页查询，实现解耦和，所以使用分页查询的参数
         */
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("skipCount",0);
        paramMap.put("pageSize",10);
        paramMap.put("loanId",loanId);
        List<rechargeRecordList> bidInfoList = bidInfoService.queryBidInfoAndUserByLoanId(paramMap);
        mav.addObject("bidInfoList",bidInfoList);

        /**
         * 根据loanId获取该loan_info的信息
         */
        LoanInfo loanInfo = loanInfoService.queryLoanInfoByLoanId(loanId);
        mav.addObject("loanInfo",loanInfo);
        mav.setViewName("/loanInfo");
        return mav;
    }
}
