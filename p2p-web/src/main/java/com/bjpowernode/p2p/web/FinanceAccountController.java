package com.bjpowernode.p2p.web;

import com.bjpowernode.p2p.model.FinanceAccount;
import com.bjpowernode.p2p.model.User;
import com.bjpowernode.p2p.service.loan.FinanceAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName:FinanceAccountController
 * Package:com.bjpowernode.p2p.web
 * Description:
 * @RestController注解，相当于(@controler+@ResponseBody)
 * @date:2020/2/1 10:47
 * @author:jiangjing
 */
@RequestMapping(value = "/loan")
@RestController
public class FinanceAccountController {
    @Autowired
    private FinanceAccountService financeAccountService;

    //@PostMapping(value = "/getFinanceAccount")
    @RequestMapping(value = "/getFinanceAccount", method = RequestMethod.POST)
    public FinanceAccount getFinanceAccount(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        Integer userId = user.getId();
        FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUserId(userId);
        return financeAccount;
    }
}
