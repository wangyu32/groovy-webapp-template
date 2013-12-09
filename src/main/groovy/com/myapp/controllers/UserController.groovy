package com.myapp.controllers

import com.myapp.dao.GenericDao
import com.myapp.dao.InputException
import com.myapp.model.Role
import com.myapp.model.User
import com.myapp.service.DataService
import com.sun.security.auth.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Controller
@RequestMapping("/user")
class UserController extends BaseController {


    @RequestMapping(value="/login_redirect",method=RequestMethod.GET)
    String login_redirect(HttpServletRequest request) {

         User user = getCurrentUser(request)

        log.debug("logged in: ${user.name}")

        "redirect:/"

    }

    @RequestMapping(value="/current")
    @ResponseBody User current(HttpServletRequest request) {
       getCurrentUser(request)
    }

    User getCurrentUser(HttpServletRequest request) {

        def principal = request.getUserPrincipal()
        if(principal == null)
            return null

        return dao.find("from User u where u.email = :email",[email: principal.name])
    }

    @RequestMapping(value="/signout",method=RequestMethod.GET)
    String signout(HttpServletRequest request) {

        def principal = request.getUserPrincipal()

        log.debug("signout in: ${principal.name}")

        request.session.invalidate()

        "redirect:/"

    }



    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody User register(@RequestBody @Valid User user, BindingResult result) {

        if(result.hasErrors())
            throw new BindException(result)

        if(doesUserExist(user.email))
            throw new InputException("An account already exists for: ${user.email}")

        user.name = user.firstName + ' ' + user.lastName

        user.password = User.hash(user.password)
        user.roles = []
        user.addRole(Role.ROLE_USER)

        dao.save(user)

        return user
    }

    Boolean doesUserExist(String email) {

        User user = dao.find("from User u where u.email = :email", [email: email])
        user ? true : false

    }




}
